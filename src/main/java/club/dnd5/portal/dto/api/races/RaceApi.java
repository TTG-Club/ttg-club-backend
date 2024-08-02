package club.dnd5.portal.dto.api.races;

import club.dnd5.portal.dto.api.GroupApi;
import club.dnd5.portal.dto.api.NameValueApi;
import club.dnd5.portal.dto.api.SourceApi;
import club.dnd5.portal.dto.api.classes.NameApi;
import club.dnd5.portal.dto.api.classes.SourceTypeApi;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.races.Race;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

@JsonInclude(Include.NON_NULL)

@Getter
@Setter
public class RaceApi {
	private NameApi name;
	protected String url;
	private Collection<NameValueApi> abilities;
	protected Object type;
	private GroupApi group;
	private SourceApi source;

	protected List<RaceApi> subraces;

	private String image;

	public RaceApi(Race race, Set<String> books) {
		name = new NameApi(race.getName(), race.getEnglishName());
		if (race.getParent() == null) {
			url = String.format("/races/%s", race.getUrl());
		}
		else {
			url = String.format("/races/%s/%s", race.getParent().getUrl(), race.getUrl());
		}
		type = new SourceTypeApi(race.getBook().getType().getName(), race.getBook().getType().ordinal());
		image = String.format("https://img.ttg.club/races/background/race-%s.webp", race.getEnglishName().replace(' ', '-').toLowerCase());
		if (race.getParent() != null) {
			image = String.format("https://img.ttg.club/races/background/race-%s.webp", race.getParent().getEnglishName().replace(' ', '-').toLowerCase());
		}
		abilities = race.getAbilityValueBonuses()
				.stream()
				.map(bonus -> NameValueApi.builder()
					.key(bonus.getAbility())
					.name(bonus.getAbility().getCyrilicName())
					.shortName(bonus.getAbility().getShortName())
					.value(bonus.getBonus())
					.build())
				.collect(Collectors.toList());
		source = new SourceApi(race.getBook());
		if (!race.getSubRaces().isEmpty()) {
			subraces = race.getSubRaces()
				.stream()
				.filter(r -> !r.isView())
				.filter(r -> books.isEmpty() || books.contains(r.getBook().getSource()))
				.map(race1 -> new RaceApi(race1, books))
				.collect(Collectors.toList());
		}
		if (Objects.nonNull(race.getOrigin())) {
			group = new GroupApi("Происхождения", (byte) 0);
		} else if (race.getBook().getSource().equals("MPMM")) {
			group = new GroupApi("Расы из Монстры Мультивселенной", (byte) 1);
		} else if (race.getBook().getType() == TypeBook.TEST) {
			group = new GroupApi("Расы Unearthed Arcana", (byte) 2);
		} else if (race.getBook().getType() == TypeBook.THIRD_PARTY) {
			group = new GroupApi("Расы от третьих лиц", (byte) 3);
		}  else if (race.getBook().getType() == TypeBook.CUSTOM) {
			group = new GroupApi("Расы Homebrew", (byte) 4);
		}
	}
}
