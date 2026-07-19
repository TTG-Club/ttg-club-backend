package club.dnd5.portal.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Настройки трекера инициативы (инструмент).
 * <p>
 * Лимит «один трекер анониму» невозможно строго обеспечить на сервере — у анонима нет
 * идентичности, его трекер держит клиент (id + ключ в localStorage). Поэтому сервер
 * защищается от абьюза: ограничивает создание анонимных трекеров по IP и удаляет
 * анонимные трекеры без активности по TTL.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "tracker")
public class TrackerProperties {

	/** Сколько анонимных трекеров можно создать с одного IP за окно {@link #anonymousCreateWindow}. */
	private int anonymousCreateLimit = 10;

	/** Окно лимита создания анонимных трекеров. */
	private Duration anonymousCreateWindow = Duration.ofHours(1);

	/** Срок жизни анонимного трекера с момента последней активности; дальше — физическое удаление. */
	private Duration anonymousTtl = Duration.ofDays(14);

	/** TTL простаивающих записей лимитера создания (очистка карты в памяти). */
	private Duration bucketIdleTtl = Duration.ofHours(2);
}
