package club.dnd5.portal.dto.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPartyCreateApi {
	private String groupName;
	private String description;
	private List<Long> userListIds = new ArrayList<>();
	private boolean sendEmail;
}
