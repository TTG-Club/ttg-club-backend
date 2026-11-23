package club.dnd5.portal.dto.api.charactersheet;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.Size;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CharacterSheetRequest {

	@Size(max = 100)
	@Schema(description = "Имя персонажа; пустое — «Новый персонаж» при создании, без изменений при сохранении")
	private String name;

	@Schema(description = "Документ листа (JSON-объект); обязателен при создании, null при сохранении — без изменений")
	private Map<String, Object> data;

	@Schema(description = "Версия, с которой клиент начинал правку; не совпала с текущей — 409. null — без проверки")
	private Long version;
}
