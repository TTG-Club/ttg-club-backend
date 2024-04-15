package club.dnd5.portal.model.creature;

import club.dnd5.portal.model.SkillType;
import lombok.*;
import org.springframework.util.StringUtils;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor

@Entity
@Table(name = "creature_skills")
@Builder
@AllArgsConstructor
public class Skill {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Enumerated(EnumType.STRING)
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
