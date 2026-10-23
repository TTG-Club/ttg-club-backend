package club.dnd5.portal.model.tavern;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import club.dnd5.portal.model.SkillType;
import club.dnd5.portal.model.creature.HabitatType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(of = "name")
@Entity
@Table(name = "taverna_visitors")
public class Visitor {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String name;

	@OneToMany (targetEntity = VisitorChance.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "visitor_id")
	private List<VisitorChance> chance;
	
	@ElementCollection(targetClass = SkillType.class)
	@CollectionTable(name = "taverna_visitor_habitates")
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	List<HabitatType> habitatTypes;
}