package club.dnd5.portal.config.liquibase;

import club.dnd5.portal.config.liquibase.MechanicsBackfill.RaceBonus;
import club.dnd5.portal.model.AbilityType;
import club.dnd5.portal.model.SkillType;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Заполнение {@code mechanics} из уже структурированных колонок (db/changelog 0045). Правила
 * сборки — {@link MechanicsBackfill}; здесь только чтение строк и запись jsonb.
 *
 * <p>Параметр {@code target}: {@code classes}, {@code races}, {@code race_features},
 * {@code backgrounds} или {@code traits}. Пишутся только строки, где {@code mechanics} ещё
 * {@code NULL} и где собирать есть из чего; откат — {@code SET mechanics = NULL} в changelog.</p>
 */
@Getter
@Setter
public class MechanicsBackfillChange implements CustomTaskChange {
	private static final ObjectMapper JSON = new ObjectMapper();
	private static final Set<String> TARGETS = Set.of("classes", "races", "race_features", "backgrounds", "traits");
	private static final TypeReference<List<String>> STRINGS = new TypeReference<>() {
	};

	private String target;

	private int rows;
	private int filled;

	@Override
	public void execute(Database database) throws CustomChangeException {
		Connection connection = ((JdbcConnection) database.getConnection()).getUnderlyingConnection();
		try {
			switch (target) {
				case "classes" -> fill(connection,
					"SELECT id, saving_throws, available_skills::text, skill_available_count FROM classes WHERE mechanics IS NULL",
					rs -> MechanicsBackfill.classMechanics(rs.getString(2), enums(rs.getString(3), SkillType::valueOf), rs.getInt(4)));
				case "races" -> fillRaces(connection);
				case "race_features" -> fill(connection,
					"SELECT id, skills::text FROM race_features WHERE mechanics IS NULL",
					rs -> MechanicsBackfill.raceFeatureMechanics(enums(rs.getString(2), SkillType::valueOf)));
				case "backgrounds" -> fill(connection,
					"SELECT id, skills::text FROM backgrounds WHERE mechanics IS NULL",
					rs -> MechanicsBackfill.backgroundMechanics(enums(rs.getString(2), SkillType::valueOf)));
				case "traits" -> fill(connection,
					"SELECT id, abilities::text, description::text FROM traits WHERE mechanics IS NULL",
					rs -> MechanicsBackfill.featMechanics(enums(rs.getString(2), AbilityType::valueOf), strings(rs.getString(3))));
				default -> throw new CustomChangeException("Неизвестная цель " + target);
			}
		} catch (SQLException e) {
			throw new CustomChangeException("Заполнение " + target + ".mechanics не выполнено", e);
		}
	}

	private void fillRaces(Connection connection) throws SQLException {
		Map<Integer, List<RaceBonus>> bonuses = new HashMap<>();
		try (Statement st = connection.createStatement();
			 ResultSet rs = st.executeQuery("SELECT race_id, ability, bonus FROM race_bonuses WHERE race_id IS NOT NULL ORDER BY race_id, id")) {
			while (rs.next()) {
				AbilityType ability = parse(rs.getString(2), AbilityType::valueOf);
				Integer bonus = rs.getObject(3, Integer.class);
				bonuses.computeIfAbsent(rs.getInt(1), id -> new ArrayList<>()).add(new RaceBonus(ability, bonus));
			}
		}
		fill(connection,
			"SELECT id, parent_id, darkvision, fly, climb, swim FROM races WHERE mechanics IS NULL",
			rs -> {
				Integer parentId = rs.getObject(2, Integer.class);
				return MechanicsBackfill.raceMechanics(
					bonuses.getOrDefault(rs.getInt(1), List.of()),
					parentId == null ? List.of() : bonuses.getOrDefault(parentId, List.of()),
					rs.getObject(3, Integer.class), rs.getObject(4, Integer.class),
					rs.getObject(5, Integer.class), rs.getObject(6, Integer.class));
			});
	}

	@FunctionalInterface
	private interface RowMapper {
		Object map(ResultSet rs) throws SQLException;
	}

	private void fill(Connection connection, String select, RowMapper mapper) throws SQLException {
		String update = "UPDATE " + target + " SET mechanics = CAST(? AS jsonb) WHERE id = ? AND mechanics IS NULL";
		try (Statement st = connection.createStatement();
			 ResultSet rs = st.executeQuery(select);
			 PreparedStatement ps = connection.prepareStatement(update)) {
			while (rs.next()) {
				rows++;
				Object mechanics = mapper.map(rs);
				if (mechanics == null) {
					continue;
				}
				ps.setString(1, JSON.writeValueAsString(mechanics));
				ps.setInt(2, rs.getInt(1));
				ps.addBatch();
				if (++filled % 500 == 0) {
					ps.executeBatch();
				}
			}
			ps.executeBatch();
		}
	}

	private static <E> List<E> enums(String json, Function<String, E> valueOf) {
		return strings(json).stream()
			.map(value -> parse(value, valueOf))
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}

	/** Значение enum или {@code null}, если в базе имя, которого enum не знает. */
	private static <E> E parse(String value, Function<String, E> valueOf) {
		if (value == null) {
			return null;
		}
		try {
			return valueOf.apply(value);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	private static List<String> strings(String json) {
		if (json == null || json.isBlank()) {
			return List.of();
		}
		List<String> values = JSON.readValue(json, STRINGS);
		return values == null ? List.of() : values;
	}

	@Override
	public String getConfirmationMessage() {
		return target + ".mechanics: заполнено " + filled + " из " + rows;
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
		if (!TARGETS.contains(target)) {
			errors.addError("MechanicsBackfillChange: target — одно из " + TARGETS);
		}
		return errors;
	}
}
