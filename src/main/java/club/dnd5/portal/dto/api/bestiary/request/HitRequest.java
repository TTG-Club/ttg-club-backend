package club.dnd5.portal.dto.api.bestiary.request;

import club.dnd5.portal.model.Dice;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class HitRequest {
    @Schema(description = "среднее количество", required = true, example = "5")
    private Short average;
    @Schema(description = "количество дайсов", example = "10")
    private Short diceCount;
    @Schema(description = "Хит дайс", example = "d8")
    private Dice hitDice;
    @Schema(description = "бонус", example = "10")
    private Short bonus;
    @Schema(description = "текстовое описание, например 'доспехи мага'")
    private String text;
}
