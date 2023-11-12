package club.dnd5.portal.dto.api;

import club.dnd5.portal.dto.api.spells.Order;
import club.dnd5.portal.dto.api.spells.SearchRequest;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@JsonInclude(Include.NON_NULL)

@Getter
@Setter
@NoArgsConstructor
public class RequestApi {
	@Schema(description = "page number", defaultValue = "0")
    public Integer page;
	@Schema(description = "page size", defaultValue = "10")
    public Integer size = -1;
	@Schema(description = "the search object", defaultValue = "null")
    public SearchRequest search;

	@Schema(description = "the array of sorts", defaultValue = "null")
    @JsonProperty("order")
    public List<Order> orders;
}
