package club.dnd5.portal.mappers;

import club.dnd5.portal.dto.api.bestiary.request.BeastDetailRequest;
import club.dnd5.portal.dto.api.classes.NameApi;
import club.dnd5.portal.model.ArmorType;
import club.dnd5.portal.model.DamageType;
import club.dnd5.portal.model.creature.Creature;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface BestiaryMapper {
	BestiaryMapper INSTANCE = Mappers.getMapper(BestiaryMapper.class);

	@Mapping(target = "armors", source = "armorTypes", qualifiedByName = "mapArmors")
	@Mapping(target = "damageImmunities", source = "immunityDamages", qualifiedByName = "mapImmunityDamages")
	@Mapping(target = "damageResistances", source = "resistanceDamages", qualifiedByName = "mapResistanceDamages")
	@Mapping(target = "damageVulnerabilities", source = "vulnerabilityDamages", qualifiedByName = "mapVulnerabilityDamages")
	@Mapping(target = "name", qualifiedByName = "getNameFromNameApi")
	@Mapping(target = "altName", qualifiedByName = "getAltNameFromNameApi")
	@Mapping(target = "englishName", qualifiedByName = "getEngNameFromNameApi")
	@Mapping(target = "lair", source = "lair")
	Creature toEntity(BeastDetailRequest dto);

	@Named("mapArmors")
	default List<ArmorType> mapArmors(List<String> armorTypes) {
		return armorTypes.stream()
			.map(ArmorType::valueOf)
			.collect(Collectors.toList());
	}

	@Named("mapImmunityDamages")
	default List<DamageType> mapImmunityDamages(List<String> immunityDamages) {
		return immunityDamages.stream()
			.map(DamageType::valueOf)
			.collect(Collectors.toList());
	}

	@Named("mapResistanceDamages")
	default List<DamageType> mapResistanceDamages(List<String> resistanceDamages) {
		return resistanceDamages.stream()
			.map(DamageType::valueOf)
			.collect(Collectors.toList());
	}

	@Named("mapVulnerabilityDamages")
	default List<DamageType> mapVulnerabilityDamages(List<String> vulnerabilityDamages) {
		return vulnerabilityDamages.stream()
			.map(DamageType::valueOf)
			.collect(Collectors.toList());
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
