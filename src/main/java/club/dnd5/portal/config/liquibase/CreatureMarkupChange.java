package club.dnd5.portal.config.liquibase;

import club.dnd5.portal.util.MarkdownUtil;
import club.dnd5.portal.util.markup.HtmlToMarkupConverter;
import club.dnd5.portal.util.markup.MarkupHtmlRenderer;
import club.dnd5.portal.util.markup.MarkupReport;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Перевод описаний умений ({@code creatures.feats}) и действий ({@code creatures.actions}) существ —
 * элементов jsonb-массивов — между HTML и разметкой core-api (этап 3), на месте.
 * <p>
 * Параметры: {@code column} — {@code feats} или {@code actions}; {@code direction} — {@code toMarkup}
 * или {@code toHtml} (для rollback).
 * <ul>
 * <li>{@code toMarkup}: шаблон с {@code markdown: true} ({@code {@atk mw}}, {@code {@hit 4}},
 * {@code {@beast_name}}…) сначала разворачивается в HTML так же, как его показывал API
 * ({@link MarkdownUtil}), затем HTML переводится в массив блоков; ключ {@code markdown} удаляется.
 * Описание {@code null} становится {@code []}.</li>
 * <li>{@code toHtml}: блоки собираются в HTML ({@link MarkupHtmlRenderer}), {@code markdown: false} —
 * шаблоны после отката не восстанавливаются, старый код показывает развёрнутый HTML как есть.</li>
 * </ul>
 */
@Getter
@Setter
public class CreatureMarkupChange implements CustomTaskChange {
	private static final ObjectMapper JSON = new ObjectMapper();
	private static final Set<String> COLUMNS = Set.of("feats", "actions");

	private String column;
	private String direction = "toMarkup";

	private int creatures;
	private int converted;

	@Override
	public void execute(Database database) throws CustomChangeException {
		Connection connection = ((JdbcConnection) database.getConnection()).getUnderlyingConnection();
		String select = "SELECT id, name, race_id, " + column + "::text FROM creatures";
		String update = "UPDATE creatures SET " + column + " = CAST(? AS jsonb) WHERE id = ?";
		try (Statement st = connection.createStatement();
			 ResultSet rs = st.executeQuery(select);
			 PreparedStatement ps = connection.prepareStatement(update)) {
			while (rs.next()) {
				String json = rs.getString(4);
				if (json == null) {
					continue;
				}
				ArrayNode items = (ArrayNode) JSON.readTree(json);
				for (JsonNode node : items) {
					if (node instanceof ObjectNode item) {
						if (toMarkup()) {
							item.set("description", JSON.valueToTree(markup(item, rs.getString(2), (Integer) rs.getObject(3))));
							item.remove("markdown");
						} else {
							item.put("description", html(item.get("description")));
							item.put("markdown", false);
						}
						converted++;
					}
				}
				ps.setString(1, JSON.writeValueAsString(items));
				ps.setObject(2, rs.getObject(1));
				ps.addBatch();
				if (++creatures % 500 == 0) {
					ps.executeBatch();
				}
			}
			ps.executeBatch();
		} catch (SQLException e) {
			throw new CustomChangeException("Перевод creatures." + column + " не выполнен", e);
		}
	}

	private boolean toMarkup() {
		return !"toHtml".equals(direction);
	}

	private List<String> markup(ObjectNode item, String beastName, Integer raceId) {
		JsonNode value = item.get("description");
		if (value == null || value.isNull()) {
			return new ArrayList<>();
		}
		String description = value.asString();
		if (item.path("markdown").asBoolean(false)) {
			description = "feats".equals(column)
				? MarkdownUtil.featHtml(description, beastName, raceId)
				: MarkdownUtil.toHtml(description, beastName);
		}
		return HtmlToMarkupConverter.convert(description, new MarkupReport());
	}

	private static String html(JsonNode blocks) {
		if (blocks == null || !blocks.isArray()) {
			return "";
		}
		List<String> list = new ArrayList<>();
		blocks.forEach(block -> list.add(block.asString()));
		String rendered = MarkupHtmlRenderer.render(list);
		return rendered == null ? "" : rendered;
	}

	@Override
	public String getConfirmationMessage() {
		return "creatures." + column + ": " + converted + " описаний у " + creatures + " существ (" + direction + ")";
	}

	@Override
	public void setUp() {
		// параметры задаются сеттерами из changelog
	}

	@Override
	public void setFileOpener(ResourceAccessor resourceAccessor) {
		// файлы не нужны
	}

	@Override
	public ValidationErrors validate(Database database) {
		ValidationErrors errors = new ValidationErrors();
		if (!COLUMNS.contains(column)) {
			errors.addError("CreatureMarkupChange: column — feats или actions");
		}
		return errors;
	}
}
