package club.dnd5.portal.mappers;

import club.dnd5.portal.dto.api.NameValueApi;
import club.dnd5.portal.dto.api.bestiary.request.BeastDetailRequest;
import club.dnd5.portal.dto.api.classes.NameApi;
import club.dnd5.portal.exception.ApiException;
import club.dnd5.portal.model.ArmorType;
import club.dnd5.portal.model.Language;
import club.dnd5.portal.model.creature.Creature;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import org.springframework.http.HttpStatus;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface BestiaryMapper {
	final int STANDARD_MOVEMENT = 30;
	BestiaryMapper INSTANCE = Mappers.getMapper(BestiaryMapper.class);

	//TODO подумать над тем, не будет он дублировать просто напросто значения языка в бдшке
	//То есть главная проблема на данный момент, это как находить в репе значения или
	//как создавать свое но при этом не создавать новые значение в бдшке

	@Mapping(target = "armorTypes", source = "armors", qualifiedByName = "mapArmors")
	@Mapping(target = "immunityDamages", source = "damageImmunities")
	@Mapping(target = "resistanceDamages", source = "damageResistances")
	@Mapping(target = "vulnerabilityDamages", source = "damageVulnerabilities")
	@Mapping(target = "name", source = "name", qualifiedByName = "getNameFromNameApi")
	@Mapping(target = "altName", source = "name", qualifiedByName = "getAltNameFromNameApi")
	@Mapping(target = "englishName", source = "name", qualifiedByName = "getEngNameFromNameApi")
	@Mapping(target = "AC", source = "armorClass")
	@Mapping(target = "lair", source = "lair")
	@Mapping(target = "speed", ignore = true)
	@Mapping(target = "flySpeed", ignore = true)
	@Mapping(target = "swimmingSpped", ignore = true)
	@Mapping(target = "climbingSpeed", ignore = true)
	@Mapping(target = "diggingSpeed", ignore = true)
	Creature toEntity(BeastDetailRequest dto);


	@Named("mapLanguages")
	default List<Language> mapLanguages(Collection<String> languages) {
		return languages.stream()
			.map(this::createLanguageFromName)
			.collect(Collectors.toList());
	}

	@Named("mapSpeed")
	default void speed(Collection<NameValueApi> speeds, @MappingTarget Creature entity) {
		for (NameValueApi speed : speeds) {
			switch (speed.getName()) {
				case "скорость":
					byte speedMovement = convertObjectToByte(speed.getValue());
					entity.setSpeed(speedMovement);
					break;
				case "полет":
					short flyValue = convertObjectToShort(speed.getValue());
					entity.setFlySpeed(flyValue);
					break;
				case "плаванье":
					short swimmingSpeed = convertObjectToShort(speed.getValue());
					entity.setSwimmingSpped(swimmingSpeed);
					break;
				case "лазанье":
					short climbingSpeed = convertObjectToShort(speed.getValue());
					entity.setClimbingSpeed(climbingSpeed);
					break;
				case "копание":
					short diggingSpeed = convertObjectToShort(speed.getValue());
					entity.setDiggingSpeed(diggingSpeed);
					break;
			}
		}
	}

	@Named("mapArmors")
	default List<ArmorType> mapArmors(Collection<String> armors) {
		return armors.stream()
			.map(ArmorType::valueOf)
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

	default Language createLanguageFromName(String languageName) {
		Language language = new Language();
		language.setName(languageName);
		return language;
	}

	@Named("objectToShort")
	static short convertObjectToShort(Object value) {
		if (value instanceof Number) {
			return ((Number) value).shortValue();
		} else if (value instanceof String) {
			try {
				return Short.parseShort((String) value);
			} catch (NumberFormatException e) {
				throw new ApiException(HttpStatus.BAD_REQUEST, "Error parsing value to short");
			}
		} else {
			return STANDARD_MOVEMENT;
		}
	}

	@Named("objectToByte")
	static byte convertObjectToByte(Object value) {
		if (value instanceof Number) {
			return ((Number) value).byteValue();
		} else if (value instanceof String) {
			try {
				return Byte.parseByte((String) value);
			} catch (NumberFormatException e) {
				throw new ApiException(HttpStatus.BAD_REQUEST, "Error parsing value to byte");
			}
		} else {
			return STANDARD_MOVEMENT;
		}
	}
}
