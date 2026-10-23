package club.dnd5.portal.model.items;

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

import club.dnd5.portal.model.book.Book;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "treasures")
@Getter
@Setter
@NoArgsConstructor
public class Treasure {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	private String name;
	@Column(unique = true)
	private String englishName;
	private String altName;

	private Integer cost;
	
	@Enumerated(EnumType.STRING)
	private TreasureType type;

	@ManyToOne
	@JoinColumn(name = "source")
	private Book book;
	private Short page;
}