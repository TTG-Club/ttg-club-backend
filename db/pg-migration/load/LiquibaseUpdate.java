import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * liquibase update по changelog приложения из classpath — без запуска приложения.
 *
 * Аргументы: [метка]. С меткой применяет changeset'ы только до неё (update-to-tag),
 * например {@code data-load}: схема 1:1 с MySQL, в которую грузит dump_to_pg.py.
 * Подключение: SPRING_DATASOURCE_URL / _USERNAME / _PASSWORD.
 *
 * Запуск: java -cp <classpath приложения> db/pg-migration/load/LiquibaseUpdate.java data-load
 */
public class LiquibaseUpdate {
	public static void main(String[] a) throws Exception {
		try (Connection c = DriverManager.getConnection(System.getenv("SPRING_DATASOURCE_URL"),
			System.getenv("SPRING_DATASOURCE_USERNAME"), System.getenv("SPRING_DATASOURCE_PASSWORD"))) {
			var db = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c));
			try (var lb = new Liquibase("db/changelog/db.changelog-master.yaml", new ClassLoaderResourceAccessor(), db)) {
				if (a.length > 0) {
					lb.update(a[0], "");
				} else {
					lb.update("");
				}
			}
			System.out.println("liquibase update" + (a.length > 0 ? " до метки " + a[0] : "") + ": ok");
		}
	}
}
