package club.dnd5.portal.model;

import java.util.Arrays;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import club.dnd5.portal.util.StringUtil;

import club.dnd5.portal.model.book.Book;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString

@Entity
@Table(name = "conditions")
public class Condition {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String name;
	private String englishName;
	@Column(columnDefinition = "TEXT")
	private String description;
	@Enumerated(EnumType.STRING)
	private Type type;
	
	@ManyToOne
	@JoinColumn(name = "source")
	private Book book;
	private Short page;
	
	@AllArgsConstructor
	@Getter
	public enum Type {
		CONDITION("Состояния"),
		DISEASE("Болезни"),
		OTHER("Прочие состояния");
		private String name;
		
		public static Type parse(String type) {
			return Arrays.asList(values()).stream().filter(t -> t.getName().equals(type)).findFirst().orElse(CONDITION);
		}
	}
	
	public String getName() {
		return StringUtil.capitalizeWords(name.toLowerCase());
	}
}