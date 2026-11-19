package club.dnd5.portal.config.liquibase;

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
import tools.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Перевод текстовых колонок между HTML и разметкой core-api (этап 3) — для changeset'ов Liquibase.
 * <p>
 * Параметры: {@code table}, {@code idColumn} (по умолчанию {@code id}), {@code columns} — пары
 * {@code источник>приёмник} через запятую, {@code direction} — {@code toMarkup} (HTML → jsonb-массив
 * блоков, {@link HtmlToMarkupConverter}) или {@code toHtml} (обратно, {@link MarkupHtmlRenderer}; для
 * rollback). {@code NULL} остаётся {@code NULL}.
 */
@Getter
@Setter
public class MarkupColumnsChange implements CustomTaskChange {
	private static final ObjectMapper JSON = new ObjectMapper();

	private String table;
	private String idColumn = "id";
	private String columns;
	private String direction = "toMarkup";

	private int converted;

	@Override
	public void execute(Database database) throws CustomChangeException {
		Connection connection = ((JdbcConnection) database.getConnection()).getUnderlyingConnection();
		List<String[]> pairs = Arrays.stream(columns.split(","))
			.map(pair -> pair.trim().split(">"))
			.toList();
		String select = "SELECT " + idColumn + ", " + String.join(", ", pairs.stream().map(p -> p[0].trim()).toList())
			+ " FROM " + table;
		List<String> sets = new ArrayList<>();
		for (String[] pair : pairs) {
			sets.add(pair[1].trim() + " = " + (toMarkup() ? "CAST(? AS jsonb)" : "?"));
		}
		String update = "UPDATE " + table + " SET " + String.join(", ", sets) + " WHERE " + idColumn + " = ?";
		try (Statement st = connection.createStatement();
			 ResultSet rs = st.executeQuery(select);
			 PreparedStatement ps = connection.prepareStatement(update)) {
			while (rs.next()) {
				for (int i = 0; i < pairs.size(); i++) {
					String value = rs.getString(i + 2);
					String result = value == null ? null : (toMarkup() ? markup(value) : html(value));
					ps.setString(i + 1, result);
				}
				ps.setObject(pairs.size() + 1, rs.getObject(1));
				ps.addBatch();
				if (++converted % 500 == 0) {
					ps.executeBatch();
				}
			}
			ps.executeBatch();
		} catch (SQLException e) {
			throw new CustomChangeException("Перевод " + table + " (" + columns + ") не выполнен", e);
		}
	}

	private boolean toMarkup() {
		return !"toHtml".equals(direction);
	}

	private static String markup(String html) {
		return JSON.writeValueAsString(HtmlToMarkupConverter.convert(html, new MarkupReport()));
	}

	private static String html(String json) {
		String rendered = MarkupHtmlRenderer.render(Arrays.asList(JSON.readValue(json, String[].class)));
		return rendered == null ? "" : rendered;
	}

	@Override
	public String getConfirmationMessage() {
		return table + ": " + converted + " строк переведено (" + direction + ", " + columns + ")";
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
		if (table == null || columns == null) {
			errors.addError("MarkupColumnsChange: нужны параметры table и columns");
		}
		return errors;
	}
}
