package club.dnd5.portal.model.items;

import club.dnd5.portal.model.Name;
import club.dnd5.portal.model.book.Book;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "treasures")
@Getter
@Setter
@NoArgsConstructor
public class Treasure extends Name {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	

	private Integer cost;
	
	@Enumerated(EnumType.STRING)
	private TreasureType type;

	@ManyToOne
	@JoinColumn(name = "source")
	private Book book;
	private Short page;
}