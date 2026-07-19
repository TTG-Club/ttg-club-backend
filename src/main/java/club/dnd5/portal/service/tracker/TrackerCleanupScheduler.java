package club.dnd5.portal.service.tracker;

import club.dnd5.portal.config.properties.TrackerProperties;
import club.dnd5.portal.repository.tracker.InitiativeParticipantRepository;
import club.dnd5.portal.repository.tracker.InitiativeTrackerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Периодически удаляет анонимные трекеры инициативы без активности (и их участников).
 * <p>
 * У анонимного трекера нет владельца: если клиент потерял ключ (очистил localStorage),
 * трекер больше никому не доступен и копится мусором. Признак активности — updated_at
 * строки трекера; операции только с участниками дополнительно «трогают» трекер
 * (см. {@link InitiativeTrackerRepository#touch}). Трекеры владельцев не удаляются —
 * они видны в истории пользователя.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class TrackerCleanupScheduler {

	private final InitiativeTrackerRepository trackerRepository;
	private final InitiativeParticipantRepository participantRepository;
	private final TrackerProperties properties;

	@Transactional
	@Scheduled(fixedDelayString = "${tracker.cleanup-interval:PT1H}")
	public void cleanupStaleAnonymous() {
		Instant cutoff = Instant.now().minus(properties.getAnonymousTtl());
		// Сначала участники (FK-каскада на уровне БД нет), затем сами трекеры.
		participantRepository.deleteParticipantsOfStaleAnonymous(cutoff);
		int removed = trackerRepository.deleteStaleAnonymous(cutoff);
		if (removed > 0) {
			log.info("Удалено анонимных трекеров инициативы без активности: {}", removed);
		}
	}
}
