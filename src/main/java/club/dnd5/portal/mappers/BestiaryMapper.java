package club.dnd5.portal.mappers;

import club.dnd5.portal.dto.api.bestiary.request.BeastDetailRequest;
import club.dnd5.portal.dto.api.classes.NameApi;
import club.dnd5.portal.model.ArmorType;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.creature.Creature;
import club.dnd5.portal.model.creature.HabitatType;
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

	//TODO после мапера пройтись по сущностям и сделать unique значение, например как в таблице languages сами языки
	//грубо говоря задача будет заключаться в том, что после маппера не сохранялась новые значение в таблице (сущности)

	@Mapping(target = "armorTypes", source = "armors", qualifiedByName = "mapArmors")
	@Mapping(target = "immunityDamages", source = "damageImmunities")
	@Mapping(target = "resistanceDamages", source = "damageResistances")
	@Mapping(target = "vulnerabilityDamages", source = "damageVulnerabilities")
	@Mapping(target = "name", source = "name", qualifiedByName = "getNameFromNameApi")
	@Mapping(target = "altName", source = "name", qualifiedByName = "getAltNameFromNameApi")
	@Mapping(target = "englishName", source = "name", qualifiedByName = "getEngNameFromNameApi")
	@Mapping(target = "feats", ignore = true)
	@Mapping(target = "AC", source = "armorClass")
	@Mapping(target = "lair", source = "lair")
	@Mapping(target = "speed", ignore = true)
	@Mapping(target = "flySpeed", ignore = true)
	@Mapping(target = "swimmingSpped", ignore = true)
	@Mapping(target = "actions", ignore = true)
	@Mapping(target = "reaction", ignore = true)
	@Mapping(target = "legendary", ignore = true)
	@Mapping(target = "languages", ignore = true)
	Creature toEntity(BeastDetailRequest dto);

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
