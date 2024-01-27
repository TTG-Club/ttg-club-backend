package club.dnd5.portal.dto.api.bestiary;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import club.dnd5.portal.dto.api.classes.NameApi;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(Include.NON_NULL)

@Getter
@Setter
public class SizeApi extends NameApi {
	@Schema(description = "размер занимаемый в ячейках")
	private String cell;
	public SizeApi(String rus, String eng, String cell) {
		super(rus, eng);
		this.cell = cell;
	}
}