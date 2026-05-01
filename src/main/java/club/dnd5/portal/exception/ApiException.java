package club.dnd5.portal.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private final HttpStatus status;
    private final String message;

    public ApiException(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}