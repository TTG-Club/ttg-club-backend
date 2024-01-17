package club.dnd5.portal.dto.api.bestiary;

import club.dnd5.portal.dto.api.NameValueApi;
import club.dnd5.portal.model.creature.Creature;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonInclude(Include.NON_NULL)

@NoArgsConstructor

@Getter
@Setter
public class SenseApi {
	@Schema(description = "пассивная Внимательность")
	private String passivePerception;
	@Schema(description = "список чувств")
	private List<NameValueApi> senses;

	public SenseApi(Creature beast) {
		passivePerception = String.valueOf(beast.getPassivePerception());
		if (Objects.nonNull(beast.getPassivePerceptionBonus())) {
			passivePerception += beast.getPassivePerceptionBonus();
		}
		if (Objects.nonNull(beast.getDarkvision())) {
			senses = new ArrayList<>(4);
			senses.add(NameValueApi.builder()
				.name("тёмное зрение")
				.value(beast.getDarkvision())
				.build());
		}
		if (Objects.nonNull(beast.getTrysight())) {
			if (senses == null) {
				senses = new ArrayList<>(3);
			}
			senses.add(NameValueApi.builder()
				.name("истинное зрение")
				.value(beast.getTrysight())
				.build());
		}
		if (Objects.nonNull(beast.getBlindsight())) {
			if (senses == null) {
				senses = new ArrayList<>(2);
			}
			NameValueApi.NameValueApiBuilder builder = NameValueApi.builder().name("слепое зрение").value(beast.getBlindsight());
			if (beast.getBlindsightRadius() != null) {
				builder.additional("слеп за пределами этого радиуса");
			}
			senses.add(builder.build());
		}
		if (Objects.nonNull(beast.getVibration())) {
			if (senses == null) {
				senses = new ArrayList<>(1);
			}
			NameValueApi.NameValueApiBuilder builder = NameValueApi.builder().name("чувство вибрации").value(beast.getVibration());
			if (beast.getBlindsightRadius() != null && beast.getBlindsightRadius() == 1) {
				builder.additional("слеп за пределами этого радиуса");
			}
			senses.add(builder.build());
		}
	}
}
