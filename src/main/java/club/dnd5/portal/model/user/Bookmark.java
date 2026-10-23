package club.dnd5.portal.model.user;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import java.util.UUID;

@Getter
@Setter

@Entity
@Table(name = "bookmarks")
public class Bookmark {
	@Id
	@JdbcTypeCode(SqlTypes.CHAR)
	private UUID uuid;

	private String name;
	private String prefix;
	private String url;
	@Column(name = "bookmark_order")
	private Integer order;

	@ManyToOne
	@JoinColumn(name = "parent_id")
	private Bookmark parent;

	@OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
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