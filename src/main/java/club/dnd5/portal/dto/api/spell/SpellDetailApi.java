package club.dnd5.portal.dto.api.spell;

import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.model.splells.TimeCast;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

@JsonInclude(Include.NON_NULL)

@NoArgsConstructor
@Getter
@Setter
public class SpellDetailApi extends SpellApi {
	@NotNull
	private int id;
	@NotNull
	private String range;
	@NotNull
	private String duration;
	@NotNull
	private String time;
	private List<ReferenceClassApi> classes;
	private List<ReferenceClassApi> subclasses;
	private List<ReferenceClassApi> races;
	private List<ReferenceClassApi> backgrounds;
	private List<ReferenceClassApi> feats;

	@NotNull
	private String description;
	private String upper;

	public SpellDetailApi(Spell spell) {
		super(spell);
		id = spell.getId();
		range = spell.getDistance();
		duration = spell.getDuration();
		time = spell.getTimes().stream().map(TimeCast::getName).collect(Collectors.joining(" или "));
		description = spell.getDescription();
		components.setM(spell.getAdditionalMaterialComponent());
		classes = spell.getHeroClass().stream().map(ReferenceClassApi::new).collect(Collectors.toList());
		if (spell.getUpperLevel() != null) {
			upper = spell.getUpperLevel();
		}
		source.setPage(spell.getPage());
	}
}
