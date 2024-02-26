package club.dnd5.portal.model.user;

import club.dnd5.portal.model.Invitation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Table(name = "user_party")
public class UserParty {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "owner_id")
	private Long ownerId;

	@Column(name = "group_name")
	private String groupName;

	@Column(name = "description")
	private String description;

	@ManyToMany(mappedBy = "userParties")
	private List<User> userList = new ArrayList<>();

	@OneToOne(mappedBy = "userParty")
	private Invitation invitation;

	@Column(name = "creation_date")
	private Date creationDate;

	@Column(name = "last_update_date")
	private Date lastUpdateDate;
}
