package club.dnd5.portal.model.user;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Data
@Entity
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

	@OneToMany(mappedBy = "userParty")
	private List<User> userList;

	@Column(name = "creation_date")
	private Date creationDate;

	@Column(name = "last_update_date")
	private Date lastUpdateDate;
}
