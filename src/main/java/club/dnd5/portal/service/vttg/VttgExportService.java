package club.dnd5.portal.service.vttg;

import club.dnd5.portal.dto.api.vttg.VttgChange;
import club.dnd5.portal.dto.api.vttg.VttgChangesResponse;
import club.dnd5.portal.dto.api.vttg.VttgChangesStatus;
import club.dnd5.portal.dto.api.vttg.VttgSource;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.repository.datatable.BackgroundRepository;
import club.dnd5.portal.repository.datatable.FeatRepository;
import club.dnd5.portal.repository.datatable.RaceRepository;
import club.dnd5.portal.repository.datatable.SpellRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * Выгрузка компендиума редакции 2014 для VTTG в формате ручек нового сайта
 * ({@code /api/v2/vttg/changes} и {@code /changes/status}).
 *
 * <p>Времени изменения у записей старого сайта нет, поэтому выгрузка не инкрементальная.
 * Запрос с начала времён получает все записи, запрос с курсора — пустой список, а
 * изменение контента VTTG замечает по {@code schemaVersion}: это отпечаток выгрузки, и
 * при его смене VTTG сам перекачивает компендиум целиком.</p>
 *
 * <p>Сборка обходит все разделы с их связями, поэтому готовая выгрузка держится в памяти
 * {@link #TTL}: правка на сайте доезжает до VTTG не позже чем через это время.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VttgExportService {
	/** Курсор «с начала времён», которым VTTG просит полную выгрузку. */
	static final Instant EPOCH = Instant.EPOCH;
	static final Duration TTL = Duration.ofMinutes(10);

	private final RaceRepository raceRepository;
	private final BackgroundRepository backgroundRepository;
	private final FeatRepository featRepository;
	private final SpellRepository spellRepository;
	private final VttgRaceMapper raceMapper;
	private final VttgBackgroundMapper backgroundMapper;
	private final VttgFeatMapper featMapper;
	private final VttgSpellMapper spellMapper;
	private final ObjectMapper objectMapper;
	private final PlatformTransactionManager transactionManager;

	private volatile Snapshot snapshot;

	public VttgChangesResponse changes(Instant since) {
		Snapshot current = snapshot();
		List<VttgChange> upserts = isFull(since) ? current.upserts : Collections.emptyList();
		return new VttgChangesResponse(Instant.now().toString(), upserts, VttgSections.tree(), current.sources,
			current.schemaVersion);
	}

	public VttgChangesStatus status(Instant since) {
		Snapshot current = snapshot();
		boolean full = isFull(since);
		long count = full ? current.upserts.size() : 0;
		return new VttgChangesStatus(
			since == null ? null : since.toString(),
			Instant.now().toString(),
			count > 0,
			count,
			full ? current.byType : Collections.emptyMap(),
			current.schemaVersion);
	}

	private static boolean isFull(Instant since) {
		return since == null || !since.isAfter(EPOCH);
	}

	private Snapshot snapshot() {
		Snapshot current = snapshot;
		if (current != null && current.builtAt.plus(TTL).isAfter(Instant.now())) {
			return current;
		}
		synchronized (this) {
			current = snapshot;
			if (current == null || !current.builtAt.plus(TTL).isAfter(Instant.now())) {
				current = buildInTransaction();
				snapshot = current;
			}
			return current;
		}
	}

	/** Сборка читает связи записей лениво, поэтому идёт в своей транзакции только на чтение. */
	private Snapshot buildInTransaction() {
		TransactionTemplate transaction = new TransactionTemplate(transactionManager);
		transaction.setReadOnly(true);
		return transaction.execute(status -> build());
	}

	Snapshot build() {
		List<VttgChange> upserts = new ArrayList<>();
		Map<String, Book> books = new TreeMap<>();

		collect(upserts, books, "races", raceRepository.findAll(), raceMapper::toVttg,
			race -> race.getBook(), race -> race.getEnglishName());
		collect(upserts, books, "backgrounds", backgroundRepository.findAll(), backgroundMapper::toVttg,
			background -> background.getBook(), background -> background.getEnglishName());
		collect(upserts, books, "feats", featRepository.findAll(), featMapper::toVttg,
			trait -> trait.getBook(), trait -> trait.getEnglishName());
		collect(upserts, books, "spells", spellRepository.findAll(), spellMapper::toVttg,
			spell -> spell.getBook(), spell -> spell.getEnglishName());

		Map<String, Long> byType = new LinkedHashMap<>();
		for (VttgChange change : upserts) {
			byType.merge(change.getType(), 1L, Long::sum);
		}

		List<VttgSource> sources = new ArrayList<>();
		for (Map.Entry<String, Book> entry : books.entrySet()) {
			Book book = entry.getValue();
			sources.add(new VttgSource(
				entry.getKey(),
				firstNonBlank(book.getName(), book.getSource()),
				firstNonBlank(book.getEnglishName(), book.getSource()),
				book.getSource()));
		}
		return new Snapshot(Instant.now(), upserts, sources, byType, fingerprint(upserts, sources));
	}

	/**
	 * Переводит раздел в записи VTTG. Запись без английского названия пропускается: из
	 * него строится ключ, без ключа VTTG запись не разложит. Сбой одной записи не роняет
	 * выгрузку — она уходит без неё, а сбой виден в логе.
	 */
	private <T> void collect(List<VttgChange> upserts, Map<String, Book> books, String type, Collection<T> entities,
							 Function<T, Map<String, Object>> mapper, Function<T, Book> book,
							 Function<T, String> englishName) {
		for (T entity : entities) {
			if (VttgText.isBlank(englishName.apply(entity))) {
				continue;
			}
			try {
				Map<String, Object> data = mapper.apply(entity);
				upserts.add(new VttgChange(type, Objects.toString(data.get("id")), data));
				Book source = book.apply(entity);
				if (source != null && !VttgText.isBlank(source.getSource())) {
					books.putIfAbsent(VttgDictionaries.sourceKey(source), source);
				}
			} catch (RuntimeException e) {
				log.warn("VTTG: запись раздела {} не выгружена: {}", type, englishName.apply(entity), e);
			}
		}
	}

	/** Отпечаток выгрузки: положительное число, меняется с любым изменением записей. */
	private int fingerprint(List<VttgChange> upserts, List<VttgSource> sources) {
		try {
			String json = objectMapper.writeValueAsString(Arrays.asList(upserts, sources, VttgSections.tree()));
			return json.hashCode() & Integer.MAX_VALUE;
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Не удалось сериализовать выгрузку VTTG", e);
		}
	}

	private static String firstNonBlank(String value, String fallback) {
		return VttgText.isBlank(value) ? fallback : value.trim();
	}

	static final class Snapshot {
		private final Instant builtAt;
		private final List<VttgChange> upserts;
		private final List<VttgSource> sources;
		private final Map<String, Long> byType;
		private final int schemaVersion;

		Snapshot(Instant builtAt, List<VttgChange> upserts, List<VttgSource> sources, Map<String, Long> byType,
				 int schemaVersion) {
			this.builtAt = builtAt;
			this.upserts = Collections.unmodifiableList(upserts);
			this.sources = Collections.unmodifiableList(sources);
			this.byType = Collections.unmodifiableMap(byType);
			this.schemaVersion = schemaVersion;
		}
	}
}
