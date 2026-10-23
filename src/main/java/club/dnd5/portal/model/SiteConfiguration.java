package club.dnd5.portal.model;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Getter
@Setter
@Entity
public class SiteConfiguration {
	@Id
	@Column(name = "config_key")
	private String key;

	@Column(name = "config_value")
	private String value;
}
