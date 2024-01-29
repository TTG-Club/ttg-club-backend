package club.dnd5.portal.dto.api.bestiary;

import club.dnd5.portal.model.CreatureSize;
import club.dnd5.portal.model.CreatureType;
import club.dnd5.portal.model.DamageType;
import club.dnd5.portal.model.creature.Condition;
import club.dnd5.portal.model.creature.HabitatType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@JsonInclude(Include.NON_NULL)

@NoArgsConstructor
@Getter
@Setter
public class BeastFilter {
	@Schema(description = "уровени опасности")
	@JsonProperty("challengeRating")
	private List<String> challengeRatings;
	@Schema(description = "типы существ")
	@JsonProperty("type")
	private List<CreatureType> types;

	@Schema(description = "размеры существ")
	@JsonProperty("size")
	private List<CreatureSize> sizes;
	@Schema(description = "тэги")
	@JsonProperty("tag")
	private List<Integer> tags;
	private List<String> moving;
	@Schema(description = "особые чувства")
	private List<String> senses;
	@Schema(description = "умения")
	private List<String> features;
	@Schema(description = "НПС")
	private List<String> npc;
	@Schema(description = "уязвимости к урону")
	private List<DamageType> vulnerabilityDamage;
	@Schema(description = "сопротивления к урону")
	private List<DamageType> resistanceDamage;
	@Schema(description = "иммунитеты к урону")
	private List<DamageType> immunityDamage;
	@Schema(description = "иммунитеты к состояниям")
	private List<Condition> immunityCondition;

	@Schema(description = "среда обитания")
	@JsonProperty("environment")
	private List<HabitatType> environments;

	@JsonProperty("book")
	private List<String> books;
}
