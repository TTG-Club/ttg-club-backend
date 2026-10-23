package club.dnd5.portal.model.classes.archetype;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import club.dnd5.portal.model.splells.Spell;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@Entity
@Table(name = "archetype_spell")
public class ArchetypeSpell {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String advenced;
	private int level;
	private String type;
	
	@OneToOne
	private Spell spell;
}