package club.dnd5.portal.model.tavern;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import club.dnd5.portal.model.races.Sex;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "taverna_names")
@Getter
@Setter
public class TavernaName {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String name;
	private String names;
	@Enumerated(EnumType.STRING)
	private Sex sex;
	@Enumerated(EnumType.STRING)
	private ObjectType objectType;

	public static enum ObjectType{
		ANIMATED,
		OBJECT
	}
}