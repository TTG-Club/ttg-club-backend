package club.dnd5.portal.dto.api;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/*
	Нужно понимать, что под это класс, также попадают люди, которые не приняли инвайт, а также люди, которых владелец группы не подтвердил
 */
@Data
@Builder
public class PartyMember {
	private String userName;
	private String name;
	private List<String> roles;
	private boolean ownerApprove;
}
