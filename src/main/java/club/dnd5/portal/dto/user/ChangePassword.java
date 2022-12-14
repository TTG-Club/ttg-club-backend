package club.dnd5.portal.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

@JsonInclude(Include.NON_NULL)

@Getter
@Setter
public class ChangePassword {
	private String userToken;
	private String resetToken;
	private String password;
}