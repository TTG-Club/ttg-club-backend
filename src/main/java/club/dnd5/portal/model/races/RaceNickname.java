package club.dnd5.portal.model.races;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "race_nicknames")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RaceNickname {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String name;
	@ManyToOne
	@JoinColumn(name = "race_id")
	private Race race;
	@Enumerated(EnumType.STRING)
	NicknameType type;
	
	@AllArgsConstructor
	@Getter
	public static enum NicknameType {
		CLAN("из клана", "Кланы:"),
		SURNAME("", "Фамилии:"),
		NICKNAME("по прозвищу", "Прозвища:"),
		HOUSE("из дома", "Дома:"),
		SQUAD("из отряда", "Отряды:"),
		TRIBE("из племени", "Племена:"),
		FROM("из", "По месту рождения:"),
		DYNASTY("династии", "Династии"),
		;

		private String name;
		private String display;
	}
}