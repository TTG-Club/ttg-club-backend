import java.nio.file.*;
import java.sql.*;
import java.util.*;

/**
 * Только чтение: выполняет SELECT-блоки из sql-файла и пишет каждый в TSV.
 *
 * Аргументы: <properties | -> <sql-файл> <каталог вывода>.
 * Подключение берётся из SPRING_DATASOURCE_URL / _USERNAME / _PASSWORD, если они заданы,
 * иначе из spring.datasource.* в properties-файле. Сессия переводится в READ ONLY.
 * Блоки разделяются строкой "-- @имя"; ошибка одного блока не останавливает остальные.
 */
public class Dump {
  static String clean(String v) {
    if (v == null) return "<NULL>";
    return v.replace('\t', ' ').replace('\n', ' ').replace('\r', ' ');
  }

  static String setting(Properties p, String env, String key) {
    String v = System.getenv(env);
    if (v != null && !v.isBlank()) return v.trim();
    v = p.getProperty(key);
    if (v == null) throw new IllegalStateException("Не задано " + env + " / " + key);
    return v.trim();
  }

  public static void main(String[] a) throws Exception {
    Properties p = new Properties();
    if (!a[0].equals("-")) {
      try (var r = Files.newBufferedReader(Path.of(a[0]))) { p.load(r); }
    }
    String url = setting(p, "SPRING_DATASOURCE_URL", "spring.datasource.url");
    String user = setting(p, "SPRING_DATASOURCE_USERNAME", "spring.datasource.username");
    String pass = setting(p, "SPRING_DATASOURCE_PASSWORD", "spring.datasource.password");
    Files.createDirectories(Path.of(a[2]));
    int ok = 0, failed = 0;
    try (Connection c = DriverManager.getConnection(url, user, pass)) {
      c.setReadOnly(true);
      try (Statement s = c.createStatement()) { s.execute("SET SESSION TRANSACTION READ ONLY"); }
      String sql = Files.readString(Path.of(a[1]));
      for (String block : sql.split("(?m)^-- @")) {
        block = block.trim();
        if (block.isEmpty()) continue;
        int nl = block.indexOf('\n');
        String name = block.substring(0, nl).trim();
        String q = block.substring(nl).trim();
        if (q.endsWith(";")) q = q.substring(0, q.length() - 1);
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(q)) {
          var md = rs.getMetaData();
          int n = md.getColumnCount();
          var sb = new StringBuilder();
          for (int i = 1; i <= n; i++) sb.append(i > 1 ? "\t" : "").append(md.getColumnLabel(i));
          sb.append('\n');
          while (rs.next()) {
            for (int i = 1; i <= n; i++) sb.append(i > 1 ? "\t" : "").append(clean(rs.getString(i)));
            sb.append('\n');
          }
          Files.writeString(Path.of(a[2], name + ".tsv"), sb.toString());
          ok++;
        } catch (SQLException e) {
          failed++;
          System.out.println(name + ": ERR " + e.getMessage());
        }
      }
    }
    System.out.println("блоков: " + ok + " ок, " + failed + " с ошибкой");
  }
}
