package club.dnd5.portal.dto.api.bestiary.request;

import club.dnd5.portal.util.markup.MarkupBlocksDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

/**
 * Легендарные действия в запросе сохранения существа. Ответ — {@link club.dnd5.portal.dto.api.bestiary.LegendaryApi}
 * с теми же полями; здесь описания строго блоки разметки core-api.
 */
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "легендарные действия")
public class LegendaryRequest {
	@Schema(description = "список легендарных действий")
	private List<@Valid LegendaryActionRequest> list;
	@Schema(description = "количество возможных легендарных действий")
	private int count;
	@Schema(description = "вступление к легендарным действиям: блоки разметки core-api")
	@JsonDeserialize(using = MarkupBlocksDeserializer.class)
	private List<@NotBlank String> description;

	/** Легендарное действие: название и описание блоками разметки (поле {@code value}, как в ответе). */
	@NoArgsConstructor
	@Getter
	@Setter
	public static class LegendaryActionRequest {
		@Schema(description = "название", required = true)
		private String name;
		@Schema(description = "описание: блоки разметки core-api (HTML-строку переводит десериализатор)")
		@JsonDeserialize(using = MarkupBlocksDeserializer.class)
		private List<@NotBlank String> value;
	}
}
