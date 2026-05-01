package club.dnd5.portal.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

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