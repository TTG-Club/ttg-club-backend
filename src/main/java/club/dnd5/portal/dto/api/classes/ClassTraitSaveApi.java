package club.dnd5.portal.dto.api.classes;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@Getter
@Setter
public class ClassTraitSaveApi {
	private Integer id;
	@NotBlank
	private String name;
	private String suffix;
	@Min(1)
	@Max(20)
	private byte level;
	@NotBlank
	private String description;
	private boolean optional;
	private String child;
}
