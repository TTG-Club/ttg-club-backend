package club.dnd5.portal.model.races;

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
	private List<Language> lanuages;
	
	@ElementCollection(targetClass = SkillType.class)
	@CollectionTable(name = "race_feature_skill_type")
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private List<SkillType> skills;

	private Integer replaceFeatureId;
	
	public boolean isNotFeature() {
		return !feature;
	}
}
