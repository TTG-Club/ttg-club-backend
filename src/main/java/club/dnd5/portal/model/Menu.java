package club.dnd5.portal.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "menu")
public class Menu {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Short id;

	@Basic(optional = false)
	@Column(nullable = false)
	private String name;
	private String icon;
	private String url;
	private boolean external;
	@JoinColumn(name = "only_dev")
	private boolean onlyDev;

	@Basic(optional = false)
	private int order;

	@ManyToOne
	@JoinColumn(name = "parent_id")
	private Menu parent;

	@OneToMany(mappedBy = "parent", orphanRemoval = false, cascade = CascadeType.REMOVE)
	private List<Menu> children;
}
