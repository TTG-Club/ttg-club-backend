package club.dnd5.portal.dto.api.bestiary;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import club.dnd5.portal.dto.api.SourceApi;
import club.dnd5.portal.dto.api.classes.NameApi;
import club.dnd5.portal.model.creature.Creature;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(Include.NON_NULL)

@NoArgsConstructor
@Getter
@Setter
public class BeastApi {
	@Schema(description = "Имя")
	private NameApi name;
	@Schema(description = "тип существа")
	protected Object type;
	@Schema(description = "Уровень опасности")
	private String challengeRating;
	@Schema(description = "URL")
	protected String url;
	@Schema(description = "источник")
	protected SourceApi source;

	public BeastApi(Creature beast) {
		name = new NameApi(beast.getName(), beast.getEnglishName());
		type = beast.getType().getCyrillicName();
		challengeRating = beast.getChallengeRating();
		url = String.format("/bestiary/%s", beast.getUrl());
		source = new SourceApi(beast.getBook());
	}
}
