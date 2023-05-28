package club.dnd5.portal.dto.api.tools;

import club.dnd5.portal.dto.api.NameValueApi;
import club.dnd5.portal.dto.api.SourceApi;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TraderApi {
	private List<NameValueApi> magicLevels;
	private List<SourceApi> sources;
}
