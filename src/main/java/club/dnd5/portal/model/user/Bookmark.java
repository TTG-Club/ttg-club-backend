package club.dnd5.portal.model.user;

import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@Entity
@Table(name = "bookmarks")
public class Bookmark {
	@Id
	@Type(type = "uuid-char")
	private UUID uuid;

	private String name;
	private String prefix;
	private String url;
	@Column(name = "bookmark_order")
	private Integer order;

	@ManyToOne
	@JoinColumn(name = "parent_id")
	private Bookmark parent;

	@OneToMany(mappedBy = "parent", orphanRemoval = false, cascade = CascadeType.REMOVE)
	private List<Bookmark> children;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	public void addChild(Bookmark bookmark) {
		children.add(bookmark);
	}
	public void incrementOrder() {
		if (order != null) {
			order++;
		}
	}
	public void decrimentOrder() {
		if (order != null) {
			order--;
		}
	}
}