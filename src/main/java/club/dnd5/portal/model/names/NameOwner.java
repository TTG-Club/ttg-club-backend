package club.dnd5.portal.model.names;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter

@Entity
@Table(name = "name_owners")
public class NameOwner {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String englishName;

	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "parent_id")
	private NameOwner parent;

	@OneToMany(mappedBy = "parent", orphanRemoval = true)
	private List<NameOwner> children;
}
