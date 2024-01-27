package club.dnd5.portal.dto.api.bestiary;

import club.dnd5.portal.dto.api.NameValueApi;
import club.dnd5.portal.dto.api.SourceApi;
import club.dnd5.portal.dto.api.UrlApi;
import club.dnd5.portal.model.CreatureSize;
import club.dnd5.portal.model.DamageType;
import club.dnd5.portal.model.Language;
import club.dnd5.portal.model.creature.*;
import club.dnd5.portal.util.MarkdownUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"name", "size", "type", "str", "dex", "con", "int", "wiz", "cha"})

@NoArgsConstructor
@Getter
@Setter
public class BeastDetailApi extends BeastApi {
	private int id;
	@Schema(description = "опыт")
	private Integer experience;
	@Schema(description = "бонус мастерства")
	private String proficiencyBonus;
	@Schema(description = "размер")
	private SizeApi size;
	@Schema(description = "мировоозрение")
	private String alignment;
	@Schema(description = "значение класса доспеха")
	private Byte armorClass;
	@Schema(description = "список доспехов")
	private Collection<UrlApi> armors;
	@Schema(description = "свободный текст для доспехов")
	private String armorText;
	@Schema(description = "хиты")
	private HitPointsApi hits;
	@Schema(description = "скорости")
	private Collection<NameValueApi> speed;
	@Schema(description = "характеристики")
	private AbilityApi ability;
	@Schema(description = "бонусы к спасброскам")
	private Collection<NameValueApi> savingThrows;
	@Schema(description = "бонусы к умениям")
	private Collection<NameValueApi> skills;
	@Schema(description = "сопротивление к урону")
	private Collection<String> damageResistances;
	@Schema(description = "иммунитеты к урону")
	private Collection<String> damageImmunities;
	@Schema(description = "уязвимости к урону")
	private Collection<String> damageVulnerabilities;
	@Schema(description = "иммунитеты к состояниям")
	private Collection<String> conditionImmunities;
	@Schema(description = "чувства")
	private SenseApi senses;
	@Schema(description = "языки")
	private Collection<String> languages;

	@Schema(description = "умения")
	private Collection<NameValueApi> feats;
	@Schema(description = "действия")
	private Collection<NameValueApi> actions;
	@Schema(description = "реакции")
	private Collection<NameValueApi> reactions;
	@Schema(description = "свободный текст к реакциям")
	private String reaction;
	@Schema(description = "бонусный действия")
	private Collection<NameValueApi> bonusActions;
	@Schema(description = "легендарный действия")
	private LegendaryApi legendary;
	@Schema(description = "мистические действия")
	private Collection<NameValueApi> mysticalActions;
	@Schema(description = "текстовое описание")
	private String description;
	@Schema(description = "тэги")
	private Collection<TagApi> tags;
	@Schema(description = "места обитания")
	private Collection<String> environment;
	@Schema(description = "ссылки на токены и изображения")
	private Collection<String> images;
	@Schema(description = "логово")
	private LairApi lair;

	public BeastDetailApi(Creature beast) {
		super(beast);
		id = beast.getId();
		size = new SizeApi(beast.getSizeName(), beast.getSize().name().toLowerCase(), beast.getSize().getCell());
		if (beast.getSize() == CreatureSize.SMALL_MEDIUM) {
			size.setEng("small or medium");
		}
		if (!"—".equals(beast.getChallengeRating())){
			experience = beast.getExp();
		}
		proficiencyBonus = beast.getProficiencyBonus();

		alignment = beast.getAligment();
		armorClass = beast.getAC();
		setType(new TypeDetailApi(beast));
		if (!beast.getArmorTypes().isEmpty()) {
			armors = beast.getArmorTypes()
				.stream()
				.map(UrlApi::new)
				.collect(Collectors.toList());
		}
		if (beast.getBonusAC() != null) {
			armorText = beast.getBonusAC();
		}
		hits = new HitPointsApi(beast);
		speed = new ArrayList<>(5);
		speed.add(NameValueApi.builder()
			.value(beast.getSpeed())
			.build());
		if (beast.getFlySpeed() != null) {
			NameValueApi.NameValueApiBuilder builder = NameValueApi.builder()
					.name("летая")
					.value(beast.getFlySpeed());
			if (beast.getHover() != null) {
				builder.additional("парит");
			}
			speed.add(builder.build());
		}
		if (beast.getSwimmingSpped() != null) {
			speed.add(NameValueApi.builder()
				.name("плавая")
				.value(beast.getSwimmingSpped())
				.build());
		}
		if (beast.getDiggingSpeed() != null) {
			speed.add(NameValueApi.builder()
				.name("копая")
				.value(beast.getDiggingSpeed())
				.build());
		}
		if (beast.getClimbingSpeed() != null) {
			speed.add(NameValueApi.builder()
				.name("лазая")
				.value(beast.getClimbingSpeed())
				.build());
		}
		if (beast.getSpeedText() != null) {
			speed.add(NameValueApi.builder()
				.name(beast.getSpeedText())
				.value("")
				.build());
		}
		ability = new AbilityApi(beast);

		if (!beast.getSavingThrows().isEmpty()) {
			savingThrows = beast.getSavingThrows()
					.stream()
					.map(st -> NameValueApi.builder()
						.name(st.getAbility().getCyrilicName())
						.shortName(st.getAbility().getShortName())
						.value(st.getBonus())
						.additional(st.getAdditionalBonus()).build())
					.collect(Collectors.toList());
		}
		if (!beast.getSkills().isEmpty()) {
			skills = beast.getSkills()
					.stream()
					.map(skill -> NameValueApi.builder()
						.name(skill.getType().getCyrilicName())
						.value(skill.getBonus())
						.additional(skill.getAdditionalBonus())
						.build())
					.collect(Collectors.toList());
		}
		if (!beast.getResistanceDamages().isEmpty()) {
			damageResistances = beast.getResistanceDamages().stream()
				.map(DamageType::getCyrillicName)
				.collect(Collectors.toList());
		}
		if (!beast.getImmunityDamages().isEmpty()) {
			damageImmunities = beast.getImmunityDamages()
				.stream()
				.map(DamageType::getCyrillicName)
				.collect(Collectors.toList());
		}
		if (!beast.getVulnerabilityDamages().isEmpty()) {
			damageVulnerabilities = beast.getVulnerabilityDamages()
				.stream()
				.map(DamageType::getCyrillicName)
				.collect(Collectors.toList());
		}
		if (!beast.getImmunityStates().isEmpty()) {
			conditionImmunities = beast.getImmunityStates().stream()
				.map(Condition::getCyrillicName)
				.collect(Collectors.toList());
		}
		senses = new SenseApi(beast);
		if (!beast.getFeats().isEmpty()) {
			feats = beast.getFeats()
				.stream()
				.peek(feat -> MarkdownUtil.convert(feat, beast))
				.map(feat -> NameValueApi.builder()
					.name(feat.getName())
					.value(feat.getDescription())
					.build())
				.collect(Collectors.toList());
		}
		Collection<Action> actionsBeast = beast.getActions(ActionType.ACTION);
		if (!actionsBeast.isEmpty()) {
			actions = actionsBeast
				.stream()
				.peek(feat -> MarkdownUtil.convert(feat, beast))
				.peek(action -> MarkdownUtil.convert(action, beast))
				.map(action -> NameValueApi.builder()
					.name(action.getName())
					.value(action.getDescription())
					.build())
				.collect(Collectors.toList());
		}
		actionsBeast = beast.getActions(ActionType.REACTION);
		if (!actionsBeast.isEmpty()) {
			reaction = beast.getReaction();
			reactions = actionsBeast
				.stream()
				.peek(feat -> MarkdownUtil.convert(feat, beast))
				.map(action -> NameValueApi.builder()
					.name(action.getName())
					.value(action.getDescription())
					.build())
				.collect(Collectors.toList());
		}
		actionsBeast = beast.getActions(ActionType.BONUS);
		if (!actionsBeast.isEmpty()) {
			bonusActions = actionsBeast
				.stream()
				.peek(feat -> MarkdownUtil.convert(feat, beast))
				.map(action -> NameValueApi.builder()
					.name(action.getName())
					.value(action.getDescription())
					.build())
				.collect(Collectors.toList());
		}
		actionsBeast = beast.getActions(ActionType.LEGENDARY);
		if (!actionsBeast.isEmpty()) {
			legendary = new LegendaryApi();
			legendary.setList(actionsBeast
				.stream()
				.peek(feat -> MarkdownUtil.convert(feat, beast))
				.map(action -> NameValueApi.builder()
					.name(action.getName())
					.value(action.getDescription())
					.build())
				.collect(Collectors.toList()));
			if (beast.getLegendary() != null) {
				legendary.setDescription(beast.getLegendary());
			}
		}
		actionsBeast = beast.getActions(ActionType.MYSTICAL);
		if (!actionsBeast.isEmpty()) {
			mysticalActions = actionsBeast
				.stream()
				.peek(feat -> MarkdownUtil.convert(feat, beast))
				.map(action -> NameValueApi.builder()
					.name(action.getName())
					.value(action.getDescription())
					.build())
				.collect(Collectors.toList());
		}
		description = beast.getDescription();
		if (!beast.getHabitates().isEmpty()) {
			environment = beast.getHabitates()
				.stream()
				.map(HabitatType::getName)
				.collect(Collectors.toList());
		}
		source = new SourceApi(beast.getBook());
		if (beast.getLair() != null) {
			lair = new LairApi(beast.getLair());
		}
		if (!beast.getRaces().isEmpty()) {
			tags = beast.getRaces()
				.stream()
				.map(TagApi::new)
				.collect(Collectors.toList());
		}
		if (!beast.getLanguages().isEmpty()) {
			languages = beast.getLanguages()
				.stream()
				.map(Language::getName)
				.collect(Collectors.toList());
		}
	}
}
