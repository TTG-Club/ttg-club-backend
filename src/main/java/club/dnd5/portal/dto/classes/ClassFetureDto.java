package club.dnd5.portal.dto.classes;

import org.thymeleaf.util.StringUtils;

import club.dnd5.portal.model.classes.HeroClassTrait;
import club.dnd5.portal.model.classes.archetype.ArchetypeTrait;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ClassFetureDto {
	private int id;
	private String name;
	private byte level;
	private byte order;
	private String type;
	private String child;
	private String optional;
	private String prefix;
	private String description;

	public ClassFetureDto(HeroClassTrait feature, String className) {
		id = feature.getId();
		name = feature.getName();
		level = feature.getLevel();
		description = feature.getDescription();
		type = String.valueOf(feature.getLevel());
		switch (feature.getLevel()) {
		case 5:
		case 6:
		case 7:
		case 8:
			type += "-го уровня";
			break;
		default:
			type += "-й уровень";
			break;
		}
		prefix ="c";
		if (className.equalsIgnoreCase("Чародей")) {
			className = "Чародея";
		} else if (className.equalsIgnoreCase("Изобретатель")) {
			className = "Изобретателя";
		} else if (className.equalsIgnoreCase("Кровавый охотник")) {
			className = "Кровавого охотника";
		}
		else if (className.equalsIgnoreCase("Заклинатель Напарник")) {
			className = "Заклинателя Напарника";
		}
		else if (className.equalsIgnoreCase("Эксперт Напарник")) {
			className = "Эксперта Напарника";
		}
		else if (className.equalsIgnoreCase("Боец Напарник")) {
			className = "Бойца Напарника";
		}
		else {
			className += "а";
		}
		child = feature.getChild();
		type+= ", умение " + StringUtils.capitalizeWords(className.toLowerCase());
		if (feature.getOptional() == 1) {
			optional = "Эта опция представлена в книге «<b>Котёл Таши со всякой всячиной</b>». Всё в этой книге <b>опционально</b>: поговорите с Мастером и решите, можно ли вам получить умение или опцию, если вы соответствуете его требованиям. По договорённости с Мастером вы можете использовать некоторые из них, все или ни одну.";
		}
		order = 2;
	}

	public ClassFetureDto(ArchetypeTrait feature, String archetypeName) {
		id = feature.getId();
		name = feature.getName();
		level = feature.getLevel();
		description = feature.getDescription();
		type = String.valueOf(feature.getLevel());
		switch (feature.getLevel()) {
		case 5:
		case 6:
		case 7:
		case 8:
			type += "-го уровня";
			break;
		default:
			type += "-й уровень";
			break;
		}
		child = feature.getChild();
		prefix ="a";
		type += ", умение " + archetypeName;
		order = 1;
	}
}
