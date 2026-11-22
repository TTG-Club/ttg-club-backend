import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Проверки базы PostgreSQL для migrate.ps1. Подключение: SPRING_DATASOURCE_URL / _USERNAME / _PASSWORD.
 *
 * <ul>
 *   <li>{@code preflight} — PostgreSQL 18+, схема public пуста, пользователь может менять
 *   session_replication_role (нужно загрузчику); при нарушении — код выхода 1;</li>
 *   <li>{@code stats} — число записей в основных таблицах и последний применённый changeset.</li>
 * </ul>
 *
 * Запуск: java -cp <classpath приложения> db/pg-migration/load/PgTool.java preflight|stats
 */
public class PgTool {
	private static final String[] TABLES = {
		"spells", "creatures", "classes", "archetypes", "races", "backgrounds", "traits",
		"artifactes", "equipments", "books", "users"
	};

	public static void main(String[] a) throws Exception {
		try (Connection c = DriverManager.getConnection(System.getenv("SPRING_DATASOURCE_URL"),
			System.getenv("SPRING_DATASOURCE_USERNAME"), System.getenv("SPRING_DATASOURCE_PASSWORD"))) {
			switch (a.length > 0 ? a[0] : "") {
				case "preflight" -> System.exit(preflight(c) ? 0 : 1);
				case "stats" -> stats(c);
				default -> throw new IllegalArgumentException("ожидается preflight или stats");
			}
		}
	}

	private static boolean preflight(Connection c) throws SQLException {
		boolean ok = true;
		int version = c.getMetaData().getDatabaseMajorVersion();
		System.out.println("PostgreSQL " + c.getMetaData().getDatabaseProductVersion()
			+ ", база " + value(c, "SELECT current_database()") + ", пользователь " + value(c, "SELECT current_user"));
		if (version < 18) {
			System.out.println("ОШИБКА: нужен PostgreSQL 18+ (collation ci с LIKE)");
			ok = false;
		}
		long tables = Long.parseLong(value(c,
			"SELECT count(*) FROM pg_tables WHERE schemaname = 'public'"));
		if (tables > 0) {
			System.out.println("ОШИБКА: в схеме public уже " + tables + " таблиц — нужна пустая база");
			ok = false;
		}
		try (Statement s = c.createStatement()) {
			s.execute("SET session_replication_role = replica");
			s.execute("RESET session_replication_role");
		} catch (SQLException e) {
			System.out.println("ОШИБКА: нет права менять session_replication_role — нужен суперпользователь или "
				+ "GRANT SET ON PARAMETER session_replication_role TO " + value(c, "SELECT current_user"));
			ok = false;
		}
		if (ok) {
			System.out.println("проверка базы: ok");
		}
		return ok;
	}

	private static void stats(Connection c) throws SQLException {
		for (String table : TABLES) {
			System.out.printf("%-12s %s%n", table, value(c, "SELECT count(*) FROM \"" + table + "\""));
		}
		System.out.println("последний changeset: " + value(c,
			"SELECT id || ' (' || filename || ')' FROM databasechangelog ORDER BY orderexecuted DESC LIMIT 1"));
	}

	private static String value(Connection c, String sql) throws SQLException {
		try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
			return rs.next() ? rs.getString(1) : null;
		}
	}
}
