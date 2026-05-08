package club.dnd5.portal.dto.online;

import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
public class OnlineHeartbeatRequest {
	@NotBlank
	@Size(max = 128)
	private String key;

	@Size(max = 128)
	private String previousGuestKey;

	@NotBlank
	@Pattern(regexp = "GUEST|REGISTERED")
	private String type;

    public void setKey(String key) {
		this.key = key;
	}

    public void setPreviousGuestKey(String previousGuestKey) {
		this.previousGuestKey = previousGuestKey;
	}

    public void setType(String type) {
		this.type = type;
	}
}
