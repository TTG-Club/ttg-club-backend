import org.postgresql.PGConnection;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;

/**
 * Выполняет SQL-файл от dump_to_pg.py через JDBC — замена `psql -f`, когда psql не установлен.
 * Понимает блоки "COPY ... FROM stdin; ... \." (через CopyManager) и пропускает мета-команды psql.
 *
 * Аргументы: <файл.sql>. Подключение: SPRING_DATASOURCE_URL / _USERNAME / _PASSWORD.
 * Всё, кроме ANALYZE, выполняется в одной транзакции (BEGIN/COMMIT из файла).
 */
public class LoadPg {
  public static void main(String[] a) throws Exception {
    String url = System.getenv("SPRING_DATASOURCE_URL");
    try (Connection c = DriverManager.getConnection(url, System.getenv("SPRING_DATASOURCE_USERNAME"),
        System.getenv("SPRING_DATASOURCE_PASSWORD"));
         BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(a[0]), StandardCharsets.UTF_8))) {
      c.setAutoCommit(true);
      var copy = c.unwrap(PGConnection.class).getCopyAPI();
      StringBuilder stmt = new StringBuilder();
      String line;
      long tables = 0, rows = 0, t0 = System.currentTimeMillis();
      while ((line = in.readLine()) != null) {
        if (line.startsWith("--") || line.startsWith("\\") || line.isBlank()) continue;
        if (line.startsWith("COPY ")) {
          StringBuilder data = new StringBuilder();
          String d;
          long n = 0;
          while (!(d = in.readLine()).equals("\\.")) { data.append(d).append('\n'); n++; }
          copy.copyIn(line.substring(0, line.length() - 1),
              new ByteArrayInputStream(data.toString().getBytes(StandardCharsets.UTF_8)));
          tables++;
          rows += n;
          System.out.println("COPY " + line.split("\"")[1] + ": " + n);
          continue;
        }
        stmt.append(line).append('\n');
        if (line.trim().endsWith(";")) {
          try (Statement s = c.createStatement()) { s.execute(stmt.toString()); }
          stmt.setLength(0);
        }
      }
      System.out.println("таблиц: " + tables + ", строк: " + rows + ", за " + (System.currentTimeMillis() - t0) / 1000 + " с");
    }
  }
}
