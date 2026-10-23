package club.dnd5.portal.model.creature;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import club.dnd5.portal.model.AbilityType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@Entity
@Table(name = "creature_spellcaster")
public class Spellcater {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	private Byte level;
	private Byte dc;
	private Byte attack;

	@Enumerated(EnumType.STRING)
	private AbilityType spellAbility;
	
	private Byte slot1;
	private Byte slot2;
	private Byte slot3;
	private Byte slot4;
	private Byte slot5;
	private Byte slot6;
	private Byte slot7;
	private Byte slot8;
	private Byte slot9;
}