package club.dnd5.portal.dto.api.wiki;

import club.dnd5.portal.model.rule.Rule;
import club.dnd5.portal.util.markup.MarkupBlocksDeserializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

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
	/** Блоки разметки core-api; HTML-строку старых ревизий аудита переводит десериализатор. */
	@NotEmpty
	@JsonDeserialize(using = MarkupBlocksDeserializer.class)
	private List<@NotBlank String> description;
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
