package club.dnd5.portal.dto.api.races;

import club.dnd5.portal.model.mechanics.RaceMechanics;
import club.dnd5.portal.model.effect.ActiveEffect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import club.dnd5.portal.model.races.Feature;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(Include.NON_NULL)

@Setter
@Getter
public class RaceSkillApi {
	  private String name;
	  /** Блоки разметки core-api. */
	  private List<String> description;
	  private Boolean subrace;
	  private Boolean opened;
	  /** Механика умения для листа персонажа (модель core-api); {@code null} — не заполнена. */
	  private RaceMechanics mechanics;
	  /** Активные эффекты умения (Active Effects VTTG); {@code null} — нет. */
	  private List<ActiveEffect> activeEffects;
	  public RaceSkillApi(Feature feature) {
		name = feature.getName();
		description = feature.getDescription();
		mechanics = feature.getMechanics();
		activeEffects = feature.getActiveEffects();
		if (feature.isFeature()) {
			opened = Boolean.TRUE;
		}
	}
}