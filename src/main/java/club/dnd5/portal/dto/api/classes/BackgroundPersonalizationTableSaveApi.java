package club.dnd5.portal.dto.api.classes;

import club.dnd5.portal.model.background.Personalization;
import club.dnd5.portal.model.background.PersonalizationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@Getter
@Setter
public class BackgroundPersonalizationTableSaveApi {
	@NotNull
	private PersonalizationType type;

	@NotEmpty
	private List<@NotBlank String> values = new ArrayList<>();

	public BackgroundPersonalizationTableSaveApi(PersonalizationType type, List<Personalization> personalizations) {
		this.type = type;
		values = personalizations.stream().map(Personalization::getText).collect(Collectors.toList());
	}

	public List<Personalization> toPersonalizations() {
		return values.stream().map(value -> {
			Personalization personalization = new Personalization();
			personalization.setType(type);
			personalization.setText(value.trim());
			return personalization;
		}).collect(Collectors.toList());
	}
}
