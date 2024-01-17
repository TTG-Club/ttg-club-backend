package club.dnd5.portal.dto.api.bestiary.request;

import club.dnd5.portal.dto.api.NameValueApi;
import club.dnd5.portal.dto.api.UrlApi;
import club.dnd5.portal.dto.api.bestiary.*;
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

	private SizeApi size;
	@Schema(description = "мировоззрение", example = "11-3031.01")
	private String alignment;
	@Schema(description = "КД", example = "10")
	private Byte armorClass;
	@Schema(description = "список доспехов")
	private Collection<UrlApi> armors;
	private String armorText;
	@Schema(description = "хиты")
	private HitPointsApi hits;
	private Collection<NameValueApi> speed;
	private AbilityApi ability;

	private Collection<NameValueApi> savingThrows;
	private Collection<NameValueApi> skills;

	private Collection<String> damageResistances;
	private Collection<String> damageImmunities;
	private Collection<String> damageVulnerabilities;
	private Collection<String> conditionImmunities;
	private SenseApi senses;
	private Collection<String> languages;

	private Collection<NameValueApi> feats;
	private Collection<NameValueApi> actions;
	private Collection<NameValueApi> reactions;
	private String reaction;
	private Collection<NameValueApi> bonusActions;
	private LegendaryApi legendary;
	private Collection<NameValueApi> mysticalActions;

	private String description;
	private Collection<TagApi> tags;

	private Collection<String> environment;
	private Collection<String> images;
	private LairApi lair;
}
