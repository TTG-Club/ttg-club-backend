package club.dnd5.portal.dto.api.bestiary.request;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import tools.jackson.databind.annotation.JsonDeserialize;
import club.dnd5.portal.util.markup.MarkupBlocksDeserializer;
import club.dnd5.portal.dto.api.classes.NameApi;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class DescriptionRequest  {
    @Schema(description = "название", required = true)
    private NameApi name;
    @Schema(description = "описание: блоки разметки core-api (HTML-строку переводит десериализатор)", required = true)
    @JsonDeserialize(using = MarkupBlocksDeserializer.class)
    private List<@NotBlank String> description;
}
