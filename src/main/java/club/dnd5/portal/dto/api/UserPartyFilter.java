package club.dnd5.portal.dto.api;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserPartyFilter {
	private Long userId;
	private boolean onlyOwner;
	private String partyName;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
	private Integer minMembers;
	private Integer maxMembers;
}
