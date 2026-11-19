package club.dnd5.portal.model.creature;

import club.dnd5.portal.model.SkillType;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

/**
 * Навык существа — элемент jsonb-массива {@code creatures.skills}.
 * В JSON пишутся только поля (вычисляемые геттеры — нет).
 */
@Getter
@Setter
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE,
	isGetterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class Skill {
	private SkillType type;
	private byte bonus;
	private String additionalBonus;

	public Skill(SkillType type, byte bonus) {
		this.type = type;
		this.bonus = bonus;
	}

	public String getText() {
		return type != null ? String.format("%s %+d", StringUtils.capitalize(type.name().toLowerCase().replace('_', ' ')), bonus) : "";
	}

	public String getCyrilicText() {
		return type != null
				? String.format("%s %+d", StringUtils.capitalize(type.getCyrilicName().toLowerCase()), bonus)
				: "";
	}
}
