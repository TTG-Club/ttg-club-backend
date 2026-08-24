package club.dnd5.portal.dto.api.classes;

import club.dnd5.portal.model.classes.FeatureLevelDefinition;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class FeatureLevelSaveApi {
	private Integer id;
	@NotBlank
	private String name;
	private String prefix;
	private String suffix;
	@Size(min = 20, max = 20)
	private List<Byte> levels;

	public FeatureLevelSaveApi(FeatureLevelDefinition definition) {
		id = definition.getId();
		name = definition.getName();
		prefix = definition.getPrefix();
		suffix = definition.getSufix();
		levels = Arrays.asList(
			definition.getL1(), definition.getL2(), definition.getL3(), definition.getL4(), definition.getL5(),
			definition.getL6(), definition.getL7(), definition.getL8(), definition.getL9(), definition.getL10(),
			definition.getL11(), definition.getL12(), definition.getL13(), definition.getL14(), definition.getL15(),
			definition.getL16(), definition.getL17(), definition.getL18(), definition.getL19(), definition.getL20());
	}
}
