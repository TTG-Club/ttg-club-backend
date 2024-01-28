package club.dnd5.portal.dto.api.spell;

import club.dnd5.portal.dto.api.SourceApi;
import club.dnd5.portal.dto.api.classes.NameApi;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.splells.Spell;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@JsonInclude(Include.NON_NULL)

@NoArgsConstructor
@Getter
@Setter
public class SpellApi {
	@NotNull
	protected NameApi name;
	@NotNull
	protected byte level;
	@NotNull
	protected String school;
	protected String additionalType;
	protected ComponentsApi components = new ComponentsApi();
	protected Boolean ritual;
	protected Boolean concentration;
	@NotNull
	protected String url;
	@NotNull
	protected SourceApi source;

	public SpellApi(Spell spell) {
		name = new NameApi(spell.getName(), spell.getEnglishName());
		level = spell.getLevel();
		school = spell.getSchool().getName();
		if (spell.isVerbalComponent()) {
			components.setV(Boolean.TRUE);	
		}
		if (spell.isSomaticComponent()) {
			components.setS(Boolean.TRUE);	
		}
		if (spell.getAdditionalMaterialComponent() != null) {
			components.setM(Boolean.TRUE);
		}
		if (spell.getRitual()) {
			ritual = Boolean.TRUE;
		}
		if (spell.getConcentration()) {
			concentration = Boolean.TRUE;
		}
		if (spell.getAdditionalType() != null) {
			additionalType = spell.getAdditionalType();
		}
		url = String.format("/spells/%s", spell.getUrlName());
		source = new SourceApi(spell.getBook());
		if (spell.getBook().getType() == TypeBook.CUSTOM) {
			source.setHomebrew(Boolean.TRUE);
		}
	}
}