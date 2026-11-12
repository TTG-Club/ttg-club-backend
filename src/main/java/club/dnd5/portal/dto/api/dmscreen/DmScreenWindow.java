package club.dnd5.portal.dto.api.dmscreen;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Окно ширмы. Имена свойств заданы явно: для boolean-поля {@code isMinimized} Lombok и Jackson
 * по умолчанию дали бы свойство {@code minimized}, а фронт ждёт ровно {@code isMinimized}.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonPropertyOrder({"id", "path", "title", "x", "y", "width", "height", "layer", "isMinimized", "content"})
@Schema(description = "Окно на холсте ширмы")
public class DmScreenWindow {

	@JsonProperty("id")
	private String id;

	@JsonProperty("path")
	@Schema(description = "Путь записи сайта, начинается с «/», или «note:…» у заметки; до 256 символов")
	private String path;

	@JsonProperty("title")
	@Schema(description = "Заголовок окна, до 256 символов")
	private String title;

	@JsonProperty("x")
	private Double x;

	@JsonProperty("y")
	private Double y;

	@JsonProperty("width")
	private Double width;

	@JsonProperty("height")
	private Double height;

	@JsonProperty("layer")
	private Integer layer;

	@JsonProperty("isMinimized")
	private Boolean isMinimized;

	/**
	 * HTML заметки мастера: есть только у окон-заметок (путь «note:…»), у записей сайта — null.
	 * Показывается только владельцу ширмы, фронт очищает разметку при показе.
	 */
	@JsonProperty("content")
	@Schema(description = "HTML заметки, до 20000 символов; только у окон-заметок", nullable = true)
	private String content;
}
