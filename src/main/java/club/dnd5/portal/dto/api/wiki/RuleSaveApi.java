package club.dnd5.portal.dto.api.wiki;

import club.dnd5.portal.model.rule.Rule;
import club.dnd5.portal.validation.ValidHtml;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@Getter
@Setter
public class RuleSaveApi {
	@NotBlank
	private String name;
	@NotBlank
	private String englishName;
	private String altName;
	@NotBlank
	private String type;
	@NotBlank
	@ValidHtml
	private String description;
	private String source;
	private Short page;

	public RuleSaveApi(Rule rule) {
		name = rule.getName();
		englishName = rule.getEnglishName();
		altName = rule.getAltName();
		type = rule.getType();
		description = rule.getDescription();
		source = rule.getBook() == null ? null : rule.getBook().getSource();
		page = rule.getPage();
	}
}
