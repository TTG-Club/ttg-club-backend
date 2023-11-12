package club.dnd5.portal.dto.api.tools;

import club.dnd5.portal.dto.api.NameValueApi;
import club.dnd5.portal.model.creature.HabitatType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@JsonInclude(Include.NON_NULL)

@NoArgsConstructor
@Getter
@Setter
public class RandomEncounterInputApi {
	private Collection<RandomEncounterSource> sources;

	private Collection<NameValueApi> environments;
	private Collection<NameValueApi> levels;

	public RandomEncounterInputApi(Set<HabitatType> values) {
		levels = new ArrayList<>();
		levels.add(NameValueApi.builder().name("1-4").value(1).build());
		levels.add(NameValueApi.builder().name("5-10").value(2).build());
		levels.add(NameValueApi.builder().name("11-15").value(3).build());
		levels.add(NameValueApi.builder().name("17-20").value(4).build());
		environments = values.stream()
				.map(e -> NameValueApi.builder().name(e.getName()).value(e.name()).build())
				.collect(Collectors.toList());
	}
}
