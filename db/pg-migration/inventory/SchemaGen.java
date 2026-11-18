import jakarta.persistence.Entity;
import jakarta.persistence.Embeddable;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.schema.spi.SchemaManagementToolCoordinator;
import java.nio.file.*;
import java.util.*;

/**
 * Офлайн-генерация DDL из сущностей (без подключения к БД), с теми же стратегиями
 * именования, что у Spring Boot 4. Аргументы: target/classes, файл вывода, диалект.
 */
public class SchemaGen {
  public static void main(String[] a) throws Exception {
    Path classes = Path.of(a[0]);
    var settings = new HashMap<String, Object>();
    settings.put("hibernate.dialect", a[2]);
    settings.put("hibernate.boot.allow_jdbc_metadata_access", "false");
    settings.put("hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.PhysicalNamingStrategySnakeCaseImpl");
    settings.put("hibernate.implicit_naming_strategy", "org.springframework.boot.hibernate.SpringImplicitNamingStrategy");
    settings.put("hibernate.temp.use_jdbc_metadata_defaults", "false");
    settings.put("hibernate.auto_quote_keyword", "true"); // как в application.properties
    var registry = new StandardServiceRegistryBuilder().applySettings(settings).build();
    var sources = new MetadataSources(registry);
    int n = 0;
    try (var walk = Files.walk(classes)) {
      for (Path p : (Iterable<Path>) walk.filter(x -> x.toString().endsWith(".class"))::iterator) {
        String cn = classes.relativize(p).toString().replace('\\', '/').replace('/', '.').replaceAll("\\.class$", "");
        // Весь пакет приложения: сущности есть и вне model (например, repository.VerificationToken).
        if (!cn.startsWith("club.dnd5.portal.")) continue;
        Class<?> c;
        try { c = Class.forName(cn, false, SchemaGen.class.getClassLoader()); } catch (Throwable t) { continue; }
        if (c.isAnnotationPresent(Entity.class) || c.isAnnotationPresent(Embeddable.class) || c.isAnnotationPresent(MappedSuperclass.class)) {
          sources.addAnnotatedClass(c); n++;
        }
      }
    }
    System.out.println("classes: " + n);
    var md = sources.buildMetadata();
    Files.deleteIfExists(Path.of(a[1]));
    var cfg = new HashMap<String, Object>(settings);
    cfg.put("jakarta.persistence.schema-generation.database.action", "none");
    cfg.put("jakarta.persistence.schema-generation.scripts.action", "create");
    cfg.put("jakarta.persistence.schema-generation.scripts.create-target", a[1]);
    cfg.put("hibernate.hbm2ddl.delimiter", ";");
    cfg.put("hibernate.format_sql", "true");
    SchemaManagementToolCoordinator.process(md, registry, cfg, action -> {});
    // Таблицы коллекций (join-таблицы @ManyToMany / @OneToMany с @JoinTable, таблицы @ElementCollection):
    // порядок строк в них задаёт порядок элементов списка, см. db/pg-migration/baseline.
    if (a.length > 3) {
      var sb = new StringBuilder("[\n");
      for (var c : md.getCollectionBindings()) {
        if (c.isOneToMany()) continue; // строки лежат в таблице целевой сущности
        if (sb.length() > 2) sb.append(",\n");
        var keyCols = new StringJoiner("\", \"", "[\"", "\"]");
        c.getKey().getColumns().forEach(col -> keyCols.add(col.getName()));
        sb.append(String.format("  {\"table\": \"%s\", \"role\": \"%s\", \"inverse\": %s, \"key\": %s}",
            c.getCollectionTable().getName(), c.getRole(), c.isInverse(), keyCols));
      }
      Files.writeString(Path.of(a[3]), sb.append("\n]\n").toString());
    }
    System.out.println("done");
  }
}
