package club.dnd5.portal.dto.api.wiki;

import club.dnd5.portal.model.Alignment;
import club.dnd5.portal.model.god.Domain;
import club.dnd5.portal.model.god.God;
import club.dnd5.portal.model.god.GodSex;
import club.dnd5.portal.model.god.Rank;
import club.dnd5.portal.validation.ValidHtml;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class GodSaveApi {
	@NotBlank
	private String name;
	@NotBlank
	private String englishName;
	private String altName;
	private String commitment;
	@NotNull
	private GodSex sex;
	@NotNull
	private Rank rank;
	@NotNull
	private Alignment alignment;
	@ValidHtml
	private String description;
	@ValidHtml
	private String alternativeDescription;
	private String symbol;
	private String nicknames;
	@NotEmpty
	private List<Domain> domains;
	@NotNull
	private Integer pantheonId;
	private String source;
	private Short page;

	public GodSaveApi(God god) {
		name = god.getName();
		englishName = god.getEnglishName();
		altName = god.getAltName();
		commitment = god.getCommitment();
		sex = god.getSex();
		rank = god.getRankValue();
		alignment = god.getAligment();
		description = god.getRawDescription();
		alternativeDescription = god.getAlternativeDescription();
		symbol = god.getSymbol();
		nicknames = god.getNicknames();
		domains = new ArrayList<>(god.getDomains());
		pantheonId = god.getPantheon().getId();
		source = god.getBook() == null ? null : god.getBook().getSource();
		page = god.getPage();
	}
}
