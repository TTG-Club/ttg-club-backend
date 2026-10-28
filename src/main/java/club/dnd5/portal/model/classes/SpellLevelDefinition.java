package club.dnd5.portal.model.classes;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "levelDefinitions")
public class SpellLevelDefinition {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private Byte level;

	private Byte slot1;
	private Byte slot2;
	private Byte slot3;
	private Byte slot4;
	private Byte slot5;
	private Byte slot6;
	private Byte slot7;
	private Byte slot8;
	private Byte slot9;
	
	@OneToMany
	@JoinColumn(name = "level_def_id")
	private List<Tracker> trackers;
	
	@ManyToOne(targetEntity = HeroClass.class)
	private HeroClass heroClass;
}