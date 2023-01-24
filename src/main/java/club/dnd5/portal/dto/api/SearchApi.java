package club.dnd5.portal.dto.api;

import club.dnd5.portal.model.background.Background;
import club.dnd5.portal.model.classes.Option;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.model.trait.Trait;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import club.dnd5.portal.dto.api.classes.NameApi;
import club.dnd5.portal.model.races.Race;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SearchApi {
	private Object name;
	private Object section;
	private Object url;
}
