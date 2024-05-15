package club.dnd5.portal.mappers;

import club.dnd5.portal.dto.api.NameValueApi;
import club.dnd5.portal.model.SkillType;
import club.dnd5.portal.model.creature.Skill;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface SkillMapper {
	SkillMapper INSTANCE = Mappers.getMapper(SkillMapper.class);

	@Named("mapSkills")
	default List<Skill> mapSkills(Collection<NameValueApi> skills) {
		return skills.stream()
			.map(this::mapNameValueApiToSkill)
			.collect(Collectors.toList());
	}

	default Skill mapNameValueApiToSkill(NameValueApi nameValueApi) {
		String name = nameValueApi.getName();
		SkillType skillType = null;
		for (SkillType type : SkillType.values()) {
			if (type.getCyrilicName().equalsIgnoreCase(name)) { // Assuming you want to compare case-insensitively
				skillType = type;
				break;
			}
		}
		Byte bonus = parseObjectToByte(nameValueApi.getValue());
		String additionalBonus = nameValueApi.getAdditional().toString();

		return Skill.builder()
			.type(skillType)
			.additionalBonus(additionalBonus)
			.bonus(bonus)
			.build();
	}

	 default Byte parseObjectToByte(Object value) {
		if (value instanceof Number) {
			return ((Number) value).byteValue();
		} else if (value instanceof String) {
			try {
				return Byte.parseByte((String) value);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Error parsing value to byte: " + value, e);
			}
		} else {
			throw new IllegalArgumentException("Unsupported type for conversion to byte: " + value.getClass());
		}
	}
}
