package club.dnd5.portal.model.splells;

import club.dnd5.portal.model.TimeUnit;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Getter
@Setter

@Entity
@NoArgsConstructor
@Table(name ="spells_times")
public class TimeCast {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private int number;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TimeUnit unit;
	@Column(name = "`condition`")
	private String condition;

	public String getName() {
		if (unit == TimeUnit.ON_HIT) {
			return unit.getName();
		}
		if (condition == null) {
			return String.format("%d %s", number, unit.getName(number));
		}
		return String.format("%d %s, %s ", number, unit.getName(number), condition);
	}
}
