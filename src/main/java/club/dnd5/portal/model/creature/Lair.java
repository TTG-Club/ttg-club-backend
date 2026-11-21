package club.dnd5.portal.model.creature;

import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.JdbcTypeCode;
import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@Entity
@Table(name = "lairs")
public class Lair {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int lairId;
	/** Разметка core-api, массив строк-блоков (см. db/changelog 0035); {@code null} — текста нет. */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private List<String> description;
	/** Действия логова, разметка core-api; {@code null} — нет. */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private List<String> action;
	/** Региональные эффекты, разметка core-api; {@code null} — нет. */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private List<String> effect;
}