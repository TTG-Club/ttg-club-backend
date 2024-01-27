package club.dnd5.portal.dto.api.bestiary.request;

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
    @Schema(description = "описание", required = true)
    private String description;
}
