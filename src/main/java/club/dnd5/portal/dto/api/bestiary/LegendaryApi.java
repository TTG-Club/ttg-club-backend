package club.dnd5.portal.dto.api.bestiary;

import java.util.List;
import club.dnd5.portal.dto.api.NameValueApi;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;

@JsonInclude(Include.NON_NULL)

@NoArgsConstructor
@AllArgsConstructor

@Getter
@Setter
@Schema(description = "легендарные действия")
/** Легендарные действия в ответе; запрос сохранения — {@link club.dnd5.portal.dto.api.bestiary.request.LegendaryRequest}. */
public class LegendaryApi {
	@Schema(description = "список легендарных действий")
	private Collection<NameValueApi> list;
	@Schema(description = "количество возможных легендарных действий")
	private int count;
	@Schema(description = "вступление к легендарным действиям: блоки разметки core-api")
	private List<String> description;
}
