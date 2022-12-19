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
	private Integer id;

	@Basic(optional = false)
	@Column(nullable = false)
	private String name;
	private String icon;
	private String url;
	private Boolean external;
	@JoinColumn(name = "in_dev")
	private Boolean inDev;

	@Basic(optional = false)
	@Column(nullable = false)
	private Integer order;

	@ManyToOne
	@JoinColumn(name = "parent_id")
	private Menu parent;

	@OneToMany(mappedBy = "parent", orphanRemoval = false, cascade = CascadeType.REMOVE)
	private List<Menu> children;
}
