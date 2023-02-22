package club.dnd5.portal.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "partners")
public class Partner {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Short id;

	@Basic(optional = false)
	@Column(nullable = false)
	private String name;

	private String description;

	@Basic(optional = false)
	@Column(nullable = false)
	private String img;

	@Basic(optional = false)
	@Column(nullable = false)
	private String url;

	@Basic(optional = false)
	private int order;
}
