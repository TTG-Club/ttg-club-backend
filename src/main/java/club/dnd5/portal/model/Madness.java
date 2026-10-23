package club.dnd5.portal.model;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Безумие персонажей
 */
@Getter
@Setter

@Entity
@Table(name = "hero_madness")
public class Madness {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(columnDefinition = "TEXT")
	private String description;
	
	private String other;
	
	@Column(name = "type")
	@Enumerated(EnumType.STRING)
	private MadnessType madnessType;
}