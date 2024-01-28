package club.dnd5.portal.dto.api.bestiary;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import club.dnd5.portal.model.creature.Creature;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(Include.NON_NULL)

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class HitPointsApi {
	@Schema(description = "среднее количество", required = true, example = "5")
	private Short average;
	@Schema(description = "формула", example = "2к6 + 10")
	private String formula;
	@Schema(description = "знак", example = "+")
	private String sign;
	@Schema(description = "бонус", example = "10")
	private Short bonus;
	@Schema(description = "текстовое описание")
	private String text;
	public HitPointsApi(Creature beast) {
		average = beast.getAverageHp();
		if (beast.getDiceHp() != null && beast.getCountDiceHp() != null && beast.getBonusHP() != null) {
			formula = String.format("%d%s", beast.getCountDiceHp(), beast.getDiceHp().getName());
		} else if (beast.getDiceHp() != null && beast.getCountDiceHp() != null) {
			formula = String.format("%d%s", beast.getCountDiceHp(), beast.getDiceHp().getName());
		}
		if (beast.getBonusHP()!=null) {
			sign = beast.getBonusHP() >=0 ? " + " : " − ";
			bonus = beast.getBonusHP();
		}
		if (beast.getSuffixHP() != null) {
			text = beast.getSuffixHP();
		}
	}
}