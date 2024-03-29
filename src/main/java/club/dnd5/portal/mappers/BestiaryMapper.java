package club.dnd5.portal.mappers;

import club.dnd5.portal.dto.api.bestiary.request.BeastDetailRequest;
import club.dnd5.portal.dto.api.bestiary.request.DescriptionRequest;
import club.dnd5.portal.model.ArmorType;
import club.dnd5.portal.model.DamageType;
import club.dnd5.portal.model.creature.Creature;
import club.dnd5.portal.model.creature.CreatureFeat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface BestiaryMapper {
	BestiaryMapper INSTANCE = Mappers.getMapper(BestiaryMapper.class);

	@Mapping(target = "armorTypes", source = "armors")
	@Mapping(target = "immunityStates", source = "conditionImmunities")
	@Mapping(target = "immunityDamages", source = "damageImmunities")
	@Mapping(target = "resistanceDamages", source = "damageResistances")
	@Mapping(target = "vulnerabilityDamages", source = "damageVulnerabilities")
	@Mapping(target = "lair", source = "lair")
	BeastDetailRequest creatureToBeastDetailRequest(Creature entity);

	Creature BeastDetailRequestToCreature(BeastDetailRequest dto);

	default List<String> mapArmorTypes(List<ArmorType> armorTypes) {
		return armorTypes.stream()
			.map(ArmorType::toString) // Преобразование в String
			.collect(Collectors.toList());
	}

	// Дефолтный метод для преобразования коллекции DamageType в List<String>
	default List<String> mapDamageTypes(Collection<DamageType> damageTypes) {
		return damageTypes.stream()
			.map(DamageType::toString) // Преобразование в String
			.collect(Collectors.toList());
	}

	// Дефолтный метод для преобразования коллекции CreatureFeat в List<DescriptionRequest>
	default List<DescriptionRequest> mapCreatureFeats(List<CreatureFeat> feats) {
		return feats.stream()
			.map(this::mapCreatureFeatToDescriptionRequest) // Преобразование CreatureFeat в DescriptionRequest
			.collect(Collectors.toList());
	}

	default DescriptionRequest mapCreatureFeatToDescriptionRequest(CreatureFeat feat) {
		DescriptionRequest descriptionRequest = new DescriptionRequest();
//		descriptionRequest.setName(feat.getName());
		descriptionRequest.setDescription(feat.getDescription());
		return descriptionRequest;
	}

	default List<DescriptionRequest> mapCreatureFeatsToDescriptionRequests(List<CreatureFeat> feats) {
		return feats.stream()
			.map(this::mapCreatureFeatToDescriptionRequest)
			.collect(Collectors.toList());
	}
}
