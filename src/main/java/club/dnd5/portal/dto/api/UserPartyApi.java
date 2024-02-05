package club.dnd5.portal.dto.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPartyApi {
	private Long id;
	private Long ownerId;
	private String groupName;
	private String description;
	private List<Long> userListIds;
	private Date creationDate;
	private Date lastUpdateDate;
}
