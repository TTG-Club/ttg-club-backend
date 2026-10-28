package club.dnd5.portal.dto.api.races;

import club.dnd5.portal.model.CreatureSize;
import club.dnd5.portal.model.CreatureType;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.races.Race;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RaceDetailApiTest {
	@Test
	void shouldLinkSubraceToBaseRaceAndSiblings() {
		Race elf = race("Эльф", "Elf");
		Race high = race("Высший эльф", "High elf");
		Race wood = race("Лесной эльф", "Wood elf");
		high.setParent(elf);
		wood.setParent(elf);
		elf.setSubRaces(new ArrayList<>(List.of(high, wood)));

		RaceDetailApi api = RaceDetailApi.withBaseRace(high, Collections.emptySet());

		assertThat(api.getBaseRace().getUrl()).isEqualTo("/races/elf");
		assertThat(api.getBaseRace().getSubraces())
			.extracting(RaceApi::getUrl)
			.containsExactly("/races/elf/high_elf", "/races/elf/wood_elf");
	}

	@Test
	void shouldUseRaceItselfAsBaseRace() {
		Race elf = race("Эльф", "Elf");

		RaceDetailApi api = RaceDetailApi.withBaseRace(elf, Collections.emptySet());

		assertThat(api.getBaseRace().getUrl()).isEqualTo("/races/elf");
		assertThat(api.getBaseRace().getSubraces()).isNull();
	}

	private Race race(String name, String englishName) {
		Book book = new Book("PHB");
		book.setName("Книга игрока");
		book.setType(TypeBook.OFFICAL);

		Race race = new Race();
		race.setName(name);
		race.setEnglishName(englishName);
		race.setBook(book);
		race.setSize(CreatureSize.MEDIUM);
		race.setType(CreatureType.HUMANOID);
		race.setView(false);
		race.setBonuses(new ArrayList<>());
		return race;
	}
}
