package club.dnd5.portal.mappers;

import club.dnd5.portal.dto.api.NameValueApi;
import club.dnd5.portal.dto.api.bestiary.LairApi;
import club.dnd5.portal.dto.api.bestiary.TagApi;
import club.dnd5.portal.dto.api.bestiary.request.BeastDetailRequest;
import club.dnd5.portal.dto.api.bestiary.request.DescriptionRequest;
import club.dnd5.portal.dto.api.classes.NameApi;
import club.dnd5.portal.model.ArmorType;
import club.dnd5.portal.model.Language;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.creature.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface BestiaryMapper {
	BestiaryMapper INSTANCE = Mappers.getMapper(BestiaryMapper.class);
	ActionMapper actionMapper = ActionMapper.INSTANCE;
	CreatureFeatMapper creatureFeatMapper = CreatureFeatMapper.INSTANCE;
	LairMapper lairMapper = LairMapper.INSTANCE;
	LanguageMapper languageMapper = LanguageMapper.INSTANCE;
	SenseMapper senseMapper = SenseMapper.INSTANCE;
	SkillMapper skillMapper = SkillMapper.INSTANCE;
	SpeedMapper speedMapper = SpeedMapper.INSTANCE;
	TagMapper tagMapper = TagMapper.INSTANCE;

	//TODO после мапера пройтись по сущностям и сделать unique значение, например как в таблице languages сами языки
	//грубо говоря задача будет заключаться в том, что после маппера не сохранялась новые значение в таблице (сущности)

	@Mapping(target = "armorTypes", source = "armors", qualifiedByName = "mapArmors")
	@Mapping(target = "immunityDamages", source = "damageImmunities")
	@Mapping(target = "resistanceDamages", source = "damageResistances")
	@Mapping(target = "vulnerabilityDamages", source = "damageVulnerabilities")
	@Mapping(target = "AC", source = "armorClass")
	@Mapping(target = "raceId", source = "npc", qualifiedByName = "mapNPC")
	@Mapping(target = "name", source = "name", qualifiedByName = "getNameFromNameApi")
	@Mapping(target = "altName", source = "name", qualifiedByName = "getAltNameFromNameApi")
	@Mapping(target = "englishName", source = "name", qualifiedByName = "getEngNameFromNameApi")
	@Mapping(target = "feats", source = "feats", qualifiedByName = "mapFeats")
	@Mapping(target = "races", source = "tags", qualifiedByName = "mapTags")
	@Mapping(target = "languages", source = "languages", qualifiedByName = "mapLanguages")
	@Mapping(target = "lair", source = "lair", qualifiedByName = "mapLair")
	@Mapping(target = "skills", source = "skills", qualifiedByName = "mapSkills")
	@Mapping(target = "habitates", source = "environment", qualifiedByName = "mapEnvironment")
	@Mapping(target = "book", source = "source", qualifiedByName = "mapSource")
	@Mapping(target = "speed", ignore = true)
	@Mapping(target = "flySpeed", ignore = true)
	@Mapping(target = "swimmingSpped", ignore = true)
	@Mapping(target = "actions", ignore = true)
	@Mapping(target = "reaction", ignore = true)
	@Mapping(target = "legendary", ignore = true)
	Creature toEntity(BeastDetailRequest dto);

	@Named("mapSkills")
	default List<Skill> mapSkills(Collection<NameValueApi> skills) {
		return skillMapper.mapSkills(skills);
	}

	@Named("mapFeats")
	default List<CreatureFeat> mapFeats(Collection<DescriptionRequest> feats) {
		return creatureFeatMapper.mapFeats(feats);
	}

	@Named("mapLair")
	default Lair mapLair(LairApi lairApi) {
		return lairMapper.mapLair(lairApi);
	}

	@Named("mapTags")
	default List<CreatureRace> mapRaces(Collection<TagApi> tags) {
		return tagMapper.mapTags(tags);
	}

	@Named("mapLanguages")
	default List<Language> mapLanguages(Collection<String> languages) {
		return languageMapper.mapLanguages(languages);
	}

	@Named("mapNPC")
	default Integer mapNPC(boolean npc) {
		if (npc) {
			return 102;
		} else {
			return 0;
		}
	}

	@Named("mapArmors")
	default List<ArmorType> mapArmors(Collection<String> armors) {
		return armors.stream()
			.map(ArmorType::valueOf)
			.collect(Collectors.toList());
	}

	@Named("mapEnvironment")
	default List<HabitatType> mapEnvironment(Collection<HabitatType> environment) {
		return new ArrayList<>(environment);
	}

	@Named("mapSource")
	default Book mapSource(String source) {
		return new Book(source);
	}

	@Named("getNameFromNameApi")
	default String getNameFromNameApi(NameApi nameApi) {
		return nameApi.getRus();
	}

	@Named("getEngNameFromNameApi")
	default String getEngNameFromNameApi(NameApi nameApi) {
		return nameApi.getEng();
	}

	@Named("getAltNameFromNameApi")
	default String getAltNameFromNameApi(NameApi nameApi) {
		return nameApi.getAlt();
	}
}
