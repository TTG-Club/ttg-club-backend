package club.dnd5.portal.model.user;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

	// Отображаемое имя для комментариев (и прочих мест, где логин показывать не хочется).
	// null — имя не задано, и везде показывается username. Колонку добавляет ddl-auto=update.
	@Column(name = "display_name", length = 24)
	private String displayName;

	private LocalDateTime createDate;

	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
	@JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
	private List<Role> roles;

    @Column(name = "enabled")
    private boolean enabled;

	@ManyToMany(mappedBy = "userList")
	private List<UserParty> userParties = new ArrayList<>();

	// Источники, снятые в фильтре по умолчанию (ключи books.source). Пусто — фильтр источников
	// как у всех: выбраны все книги. Новые книги остаются выбранными. Таблицу добавляет ddl-auto=update.
	@ElementCollection
	@CollectionTable(name = "user_excluded_sources", joinColumns = @JoinColumn(name = "user_id"))
	@Column(name = "source", nullable = false)
	private Set<String> excludedSources = new HashSet<>();

    public User() {
    	this.createDate = LocalDateTime.now();
    }
}
