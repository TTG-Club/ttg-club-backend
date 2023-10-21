package club.dnd5.portal.dto.api;

import club.dnd5.portal.dto.api.classes.NameApi;
import club.dnd5.portal.model.token.Token;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)

@NoArgsConstructor
@Getter
@Setter
public class TokenApi {
	@NonNull
	private Long id;
	/**
	 * Id существа из бестиария
	 */
	@NonNull
	private Integer ref;
	@NonNull
	private NameApi name;
	/**
	 * Типы: круглый, гекс, сверху
	 */
	private String type;
	private String url;

	public TokenApi(Token token) {
		id = token.getId();
		ref = token.getRefId();
		name = new NameApi(token.getName(), token.getEnglishName());
		type = token.getType();
		url = token.getUrl();
	}
}
