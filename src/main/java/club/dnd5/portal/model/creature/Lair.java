package club.dnd5.portal.model.creature;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@Entity
@Table(name = "lairs")
public class Lair {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int lairId;
	@Column(columnDefinition = "TEXT")
	private String description;
	@Column(columnDefinition = "TEXT")
	private String action;
	@Column(columnDefinition = "TEXT")
	private String effect;
}