package club.dnd5.portal.service.vttg;

import club.dnd5.portal.dto.api.vttg.VttgChangesResponse;
import club.dnd5.portal.dto.api.vttg.VttgChangesStatus;
import club.dnd5.portal.model.CreatureSize;
import club.dnd5.portal.model.CreatureType;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.races.Race;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.repository.datatable.BackgroundRepository;
import club.dnd5.portal.repository.datatable.FeatRepository;
import club.dnd5.portal.repository.datatable.RaceRepository;
import club.dnd5.portal.repository.datatable.SpellRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VttgExportServiceTest {
	private final RaceRepository raceRepository = mock(RaceRepository.class);
	private final BackgroundRepository backgroundRepository = mock(BackgroundRepository.class);
	private final FeatRepository featRepository = mock(FeatRepository.class);
	private final SpellRepository spellRepository = mock(SpellRepository.class);
	private VttgExportService service;

	@BeforeEach
	void setUp() {
		service = new VttgExportService(raceRepository, backgroundRepository, featRepository, spellRepository,
			new VttgRaceMapper(), new VttgBackgroundMapper(), new VttgFeatMapper(), new VttgSpellMapper(),
			new ObjectMapper(), mock(PlatformTransactionManager.class));

		Book phb = new Book("PHB");
		phb.setName("Книга игрока");
		phb.setEnglishName("Player's Handbook");
		Race dwarf = new Race();
		dwarf.setName("Дварф");
		dwarf.setEnglishName("Dwarf");
		dwarf.setBook(phb);
		dwarf.setType(CreatureType.HUMANOID);
		dwarf.setSize(CreatureSize.MEDIUM);
		dwarf.setBonuses(new ArrayList<>());
		Race unnamed = new Race();
		unnamed.setName("Без английского названия");
		Spell light = new Spell();
		light.setName("Свет");
		light.setEnglishName("Light");
		light.setBook(phb);
		light.setDistance("Касание");
		light.setDuration("1 час");

		when(raceRepository.findAll()).thenReturn(Arrays.asList(dwarf, unnamed));
		when(backgroundRepository.findAll()).thenReturn(Collections.emptyList());
		when(featRepository.findAll()).thenReturn(Collections.emptyList());
		when(spellRepository.findAll()).thenReturn(Collections.singletonList(light));
	}

	@Test
	void fullDownloadReturnsEveryRecordWithSourcesAndTree() {
		VttgChangesResponse response = service.changes(Instant.EPOCH);

		assertThat(response.getUpserts()).extracting(change -> change.getData().get("id"))
			.containsExactly("dwarf", "light");
		assertThat(response.getSources()).singleElement()
			.satisfies(source -> {
				assertThat(source.getKey()).isEqualTo("phb");
				assertThat(source.getName()).isEqualTo("Книга игрока");
				assertThat(source.getAbbreviation()).isEqualTo("PHB");
			});
		assertThat(response.getSections()).extracting(node -> node.get("section"))
			.containsExactly("spells", "species", "backgrounds", "feats");
		assertThat(response.getSchemaVersion()).isPositive();
	}

	@Test
	void cursorGetsNothingButSameFingerprint() {
		VttgChangesResponse full = service.changes(null);
		VttgChangesResponse delta = service.changes(Instant.parse("2026-01-01T00:00:00Z"));
		VttgChangesStatus status = service.status(Instant.parse("2026-01-01T00:00:00Z"));

		assertThat(delta.getUpserts()).isEmpty();
		assertThat(delta.getSchemaVersion()).isEqualTo(full.getSchemaVersion());
		assertThat(status.isHasUpdates()).isFalse();
		assertThat(status.getCount()).isZero();
		assertThat(status.getSchemaVersion()).isEqualTo(full.getSchemaVersion());
	}

	@Test
	void statusFromEpochCountsByType() {
		VttgChangesStatus status = service.status(Instant.EPOCH);

		assertThat(status.isHasUpdates()).isTrue();
		assertThat(status.getCount()).isEqualTo(2);
		assertThat(status.getByType()).containsEntry("races", 1L).containsEntry("spells", 1L);
	}

	@Test
	void snapshotIsBuiltOnceWithinTtl() {
		service.changes(Instant.EPOCH);
		service.status(Instant.EPOCH);

		verify(spellRepository, times(1)).findAll();
	}
}
