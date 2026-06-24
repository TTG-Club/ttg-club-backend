package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice(basePackages = "club.dnd5.portal.controller.api")
public class ApiExceptionHandler {
	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ApiErrorResponse> handleResponseStatusException(
		ResponseStatusException exception,
		HttpServletRequest request
	) {
		HttpStatus status = exception.getStatus();
		return ResponseEntity
			.status(status)
			.body(new ApiErrorResponse(status.value(), status.getReasonPhrase(), exception.getReason(), request.getRequestURI()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleException(Exception exception, HttpServletRequest request) {
		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
		return ResponseEntity
			.status(status)
			.body(new ApiErrorResponse(status.value(), status.getReasonPhrase(), exception.getMessage(), request.getRequestURI()));
	}
}
