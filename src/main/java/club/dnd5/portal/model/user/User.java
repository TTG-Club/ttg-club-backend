package club.dnd5.portal.model.user;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter

@Entity
@Table(name = "users")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;
	private String username;
	private String password;
	private String email;
	private LocalDateTime createDate;

	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
	@JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
	private List<Role> roles;

    @Column(name = "enabled")
    private boolean enabled;

	@ManyToOne
	@JoinColumn(name = "user_party_id")
	private UserParty userParty;

    public User() {
    	this.createDate = LocalDateTime.now();
    }
}
