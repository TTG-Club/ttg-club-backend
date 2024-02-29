package club.dnd5.portal.dto.api;

import lombok.Data;

import java.util.Date;

@Data
public class UserPartyFilter {
	private Long userId;
	private boolean onlyOwner;
	private String partyName;
	private Date startDate;
	private Date endDate;
	private Integer minMembers;
	private Integer maxMembers;
}
