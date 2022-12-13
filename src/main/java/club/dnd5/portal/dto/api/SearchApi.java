package club.dnd5.portal.dto.api;

import club.dnd5.portal.model.background.Background;
import club.dnd5.portal.model.classes.Option;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.model.trait.Trait;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import club.dnd5.portal.dto.api.classes.NameApi;
import club.dnd5.portal.model.races.Race;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(Include.NON_NULL)

@Getter
@Setter
public class SearchApi {
	private NameApi name;
	private String section;
	private String url;

	public SearchApi(Race race) {
		name = new NameApi(race.getName(), race.getEnglishName());
		section = "Расы";
		url = String.format("/races/%s", race.getUrlName());
	}
	public SearchApi(Trait trait) {
		name = new NameApi(trait.getName(), trait.getEnglishName());
		section = "Черты";
		url = String.format("/traits/%s", trait.getUrlName());
	}
	public SearchApi(Option option) {
		name = new NameApi(option.getName(), option.getEnglishName());
		section = "Особенности классов";
		url = String.format("/traits/%s", option.getUrlName());
	}
	public SearchApi(Background background) {
		name = new NameApi(background.getName(), background.getEnglishName());
		section = "Особенности классов";
		url = String.format("/backgrounds/%s", background.getUrlName());
	}
	public SearchApi(Spell spell) {
		name = new NameApi(spell.getName(), spell.getEnglishName());
		section = "Особенности классов";
		url = String.format("/spells/%s", spell.getUrlName());
	}
}
