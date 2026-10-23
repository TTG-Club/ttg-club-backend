package club.dnd5.portal.model.tavern;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "taverna_visitor_chances")
@Getter
@Setter
public class VisitorChance {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Enumerated(EnumType.STRING)
	private TavernaType tavernaType;
	@Enumerated(EnumType.STRING)
	private TavernaCategory tavernaCategory;
	private int chance;
	@ManyToOne
	private Visitor visitor;
}