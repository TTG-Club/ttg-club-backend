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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"name", "size", "type", "str", "dex", "con", "int", "wiz", "cha"})

@NoArgsConstructor
@Getter
@Setter
public class BeastDetailApi extends BeastApi {
	private int id;
	private Integer experience;
	private String proficiencyBonus;
	private SizeApi size;
	private String alignment;
	private Byte armorClass;
	private Collection<UrlApi> armors;
	private String armorText;
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
	private List<SourceApi> sources;

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
			NameValueApi.NameValueApiBuilder builder = NameValueApi.builder().name("летая")
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
				.map(DamageType::getCyrilicName)
				.collect(Collectors.toList());
		}
		if (!beast.getImmunityDamages().isEmpty()) {
			damageImmunities = beast.getImmunityDamages()
				.stream()
				.map(DamageType::getCyrilicName)
				.collect(Collectors.toList());
		}
		if (!beast.getVulnerabilityDamages().isEmpty()) {
			damageVulnerabilities = beast.getVulnerabilityDamages()
				.stream()
				.map(DamageType::getCyrilicName)
				.collect(Collectors.toList());
		}
		if (!beast.getImmunityStates().isEmpty()) {
			conditionImmunities = beast.getImmunityStates().stream()
				.map(Condition::getCyrilicName)
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
