package club.dnd5.portal.model.tavern;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import club.dnd5.portal.model.creature.HabitatType;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "taverna_drinks")
@Getter
@Setter
public class TavernaDrink {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String name;
	@Enumerated(EnumType.STRING)
	private HabitatType habitat;
	@Enumerated(EnumType.STRING)
	private TavernaCategory category;
}