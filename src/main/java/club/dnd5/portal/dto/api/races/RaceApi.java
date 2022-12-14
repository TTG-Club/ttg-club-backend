package club.dnd5.portal.dto.api.races;

import club.dnd5.portal.dto.api.GroupApi;
import club.dnd5.portal.dto.api.NameValueApi;
import club.dnd5.portal.dto.api.SourceApi;
import club.dnd5.portal.dto.api.classes.NameApi;
import club.dnd5.portal.dto.api.classes.SourceTypeApi;
import club.dnd5.portal.model.races.Race;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.List;
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

	public RaceApi(Race race) {
		name = new NameApi(race.getCapitalazeName(), race.getEnglishName());
		if (race.getParent() == null) {
			url = String.format("/races/%s", race.getUrlName());
		}
		else {
			url = String.format("/races/%s/%s", race.getParent().getUrlName(), race.getUrlName());
		}
		type = new SourceTypeApi(race.getBook().getType().getName(), race.getBook().getType().ordinal());
		image = String.format("https://image.ttg.club:8089/races/background/race-%s.webp", race.getEnglishName().replace(' ', '-').toLowerCase());
		if (race.getParent() != null) {
			image = String.format("https://image.ttg.club:8089/races/background/race-%s.webp", race.getParent().getEnglishName().replace(' ', '-').toLowerCase());
		}
		abilities = race.getAbilityValueBonuses()
				.stream()
				.map(bonus -> new NameValueApi(bonus.getAbility().getCyrilicName(), bonus.getAbility().getShortName(), bonus.getAbility(), bonus.getBonus()))
				.collect(Collectors.toList());
		source = new SourceApi(race.getBook());
		if (!race.getSubRaces().isEmpty()) {
			subraces = race.getSubRaces()
				.stream()
				.filter(r -> !r.isView())
				.map(RaceApi::new)
				.collect(Collectors.toList());
		}
	}
}
