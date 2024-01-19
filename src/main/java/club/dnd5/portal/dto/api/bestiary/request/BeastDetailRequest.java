package club.dnd5.portal.dto.api.bestiary.request;

import club.dnd5.portal.dto.api.NameValueApi;
import club.dnd5.portal.dto.api.bestiary.*;
import club.dnd5.portal.model.Alignment;
import club.dnd5.portal.model.CreatureSize;
import club.dnd5.portal.model.DamageType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"name", "size", "type", "str", "dex", "con", "int", "wiz", "cha"})

@NoArgsConstructor
@Getter
@Setter
public class BeastDetailRequest extends BeastApi {
	@Schema(description = "ID из базы")
	private Integer id;
	@Schema(description = "Размер существа")
	private CreatureSize size;
	@Schema(description = "мировоззрение", example = "11-3031.01")
	private Alignment alignment;
	@Schema(description = "КД", example = "10")
	private Byte armorClass;
	@Schema(description = "список доспехов")
	private Collection<String> armors;
	@Schema(description = "хиты")
	private HitRequest hits;
	private Collection<NameValueApi> speed;
	@Schema(description = "Характеристики")
	private AbilityApi ability;
	@Schema(description = "Бонусы к спасброскам")
	private Collection<NameValueApi> savingThrows;
	@Schema(description = "Бонусы к умениям")
	private Collection<NameValueApi> skills;
	@Schema(description = "Сопротивления к урону")
	private Collection<DamageType> damageResistances;
	@Schema(description = "Иммунитеты к урону")
	private Collection<DamageType> damageImmunities;
	@Schema(description = "Уязвимости к урону")
	private Collection<DamageType> damageVulnerabilities;
	@Schema(description = "Иммунитеты к состояниям")
	private Collection<String> conditionImmunities;
	@Schema(description = "Чувства")
	private SenseApi senses;
	@Schema(description = "Языки")
	private Collection<String> languages;
	@Schema(description = "Особенности")
	private Collection<NameValueApi> feats;
	@Schema(description = "Действия")
	private Collection<NameValueApi> actions;
	@Schema(description = "Реакции")
	private Collection<NameValueApi> reactions;
	private String reaction;
	@Schema(description = "Бонусные действия")
	private Collection<NameValueApi> bonusActions;
	@Schema(description = "Легенданые действия")
	private LegendaryApi legendary;
	@Schema(description = "Мистически действия")
	private Collection<NameValueApi> mysticalActions;
	@Schema(description = "Текстовое описание")
	private String description;
	@Schema(description = "Тэги")
	private Collection<TagApi> tags;
	@Schema(description = "Среда обитания")
	private Collection<String> environment;
	@Schema(description = "URLs изображений")
	private Collection<String> images;
	@Schema(description = "Логово")
	private LairApi lair;
}
