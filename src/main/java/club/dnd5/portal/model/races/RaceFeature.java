package club.dnd5.portal.model.races;

import club.dnd5.portal.model.Language;
import club.dnd5.portal.model.SkillType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter

@Entity
@Table(name = "race_features")
public class RaceFeature {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
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
}