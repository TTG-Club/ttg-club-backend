package club.dnd5.portal.dto.api.bestiary;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import club.dnd5.portal.model.creature.Creature;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"str", "dex", "con", "int", "wiz", "cha"})

@NoArgsConstructor
@Getter
@Setter
public class AbilityApi {
	@Schema(description = "Значение Силы", example = "10")
	private int str;
	@Schema(description = "Значение Ловеости", example = "10")
	private int dex;
	@Schema(description = "Значение Телосложения", example = "10")
	private int con;
	@Schema(description = "Значение Интеллекта", example = "10")
	@JsonProperty("int")
	private int intellect;
	@Schema(description = "Значение Мудрости", example = "10")
	private int wiz;
	@Schema(description = "Значение Харизмы", example = "10")
	private int cha;
	public AbilityApi(Creature beast) {
		str = beast.getStrength();
		dex = beast.getDexterity();
		con = beast.getConstitution();
		intellect = beast.getIntellect();
		wiz = beast.getWizdom();
		cha = beast.getCharisma();
	}
}