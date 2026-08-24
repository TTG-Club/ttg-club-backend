package club.dnd5.portal.dto.api.bestiary.request;

import club.dnd5.portal.dto.api.NameValueApi;
import club.dnd5.portal.dto.api.bestiary.*;
import club.dnd5.portal.dto.api.classes.NameApi;
import club.dnd5.portal.model.Alignment;
import club.dnd5.portal.model.CreatureSize;
import club.dnd5.portal.model.CreatureType;
import club.dnd5.portal.model.DamageType;
import club.dnd5.portal.model.creature.HabitatType;
import club.dnd5.portal.validation.ValidHtml;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import java.util.Collection;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"name", "size", "type", "str", "dex", "con", "int", "wiz", "cha"})

@NoArgsConstructor
@Getter
@Setter
public class BeastDetailRequest {
	@Schema(description = "ID из базы, если обновление, для создания оставляем NULL")
	private Integer id;
	@Schema(description = "имя существа", required = true)
	private NameApi name;
	@Schema(description = "Размер существа", example = "MEDIUM", required = true)
	private CreatureSize size;
	@Schema(description = "Тип существа", example = "ABERRATION", required = true)
	private CreatureType type;
	@Schema(description = "мировоззрение", example = "LAWFUL_GOOD", required = true)
	private Alignment alignment;
	@Schema(description = "КД", example = "10", required = true)
	private Byte armorClass;
	@Schema(description = "свободный текст для КД")
	private String armorText;
	@Schema(description = "список доспехов")
	private Collection<String> armors;
	@Schema(description = "хиты", required = true)
	private HitRequest hits;
	@Valid
	private Collection<NameValueApi> speed;
	@Schema(description = "Характеристики", required = true)
	private AbilityApi ability;
	@Schema(description = "Бонусы к спасброскам")
	@Valid
	private Collection<NameValueApi> savingThrows;
	@Schema(description = "Бонусы к умениям")
	@Valid
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
	@Schema(description = "Языки свободным текстом")
	private String language;
	@Schema(description = "Уровень опасности", required = true)
	private String challengeRating;
	@Schema(description = "Особенности")
	@Valid
	private Collection<DescriptionRequest> feats;
	@Schema(description = "Действия")
	@Valid
	private Collection<DescriptionRequest> actions;
	@Schema(description = "Реакции")
	@Valid
	private Collection<DescriptionRequest> reactions;
	@ValidHtml
	private String reaction;
	@Schema(description = "Бонусные действия")
	@Valid
	private Collection<DescriptionRequest> bonusActions;
	@Schema(description = "Легенданые действия")
	@Valid
	private LegendaryApi legendary;
	@Schema(description = "Мистически действия")
	@Valid
	private Collection<DescriptionRequest> mysticalActions;
	@Schema(description = "Текстовое описание")
	@ValidHtml
	private String description;
	@Schema(description = "Тэги", example = "Дроу")
	private Collection<String> tags;
	@Schema(description = "Среда обитания")
	private Collection<HabitatType> environment;
	@Schema(description = "URLs изображений")
	private Collection<String> images;
	@Schema(description = "Логово")
	@Valid
	private LairApi lair;
	@Schema(description = "true если НПС", example = "false")
	private boolean npc;
	@Schema(description = "источник", example = "MM")
	protected String source;
}
