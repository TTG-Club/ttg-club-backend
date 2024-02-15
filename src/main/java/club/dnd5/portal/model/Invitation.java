package club.dnd5.portal.model;

import club.dnd5.portal.model.user.UserParty;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@Table(uniqueConstraints={
	@UniqueConstraint(columnNames = {"link", "code"})
})
public class Invitation {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true)
	private String link;

	@Column(unique = true)
	private String code;
	private Date generationDate;

	@OneToOne
	@JoinColumn(name = "user_party_id")
	private UserParty userParty;

	private Long expirationTime;
}
