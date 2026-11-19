package club.dnd5.portal.model.races;

import jakarta.persistence.OrderColumn;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import club.dnd5.portal.model.Language;
import club.dnd5.portal.model.SkillType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter

@Entity
@Table(name = "race_features")
public class Feature {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "race_id", nullable = false)
	private Integer raceId;

	private String name;
	private String englishName;
	@Column(columnDefinition = "TEXT")
	private String description;
	private boolean feature;
	
	@ManyToMany
	@OrderColumn(name = "sort_order") // порядок как в MySQL, см. db/pg-migration/load
	private List<Language> lanuages;
	
	@JdbcTypeCode(SqlTypes.JSON) // jsonb, см. db/changelog 0015
	@Column(columnDefinition = "jsonb", nullable = false)
	private List<SkillType> skills = new ArrayList<>();

	private Integer replaceFeatureId;
	
	public boolean isNotFeature() {
		return !feature;
	}

	/** jsonb-колонка NOT NULL: «нет значений» хранится как [], а не NULL. */
	@jakarta.persistence.PrePersist
	@jakarta.persistence.PreUpdate
	private void nullJsonListsToEmpty() {
		skills = skills == null ? new ArrayList<>() : skills;
	}
}
