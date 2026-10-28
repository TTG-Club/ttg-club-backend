package club.dnd5.portal.model.classes;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import club.dnd5.portal.model.Rest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@Entity
@Table(name = "trackers")
public class Tracker {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String name;
	private String value;

	@Enumerated(EnumType.STRING)
	private Type type;
	@Enumerated(EnumType.STRING)
	private Rest rest;
	
	enum Type {
		Number,
		Counter,
		Roll
	}
}