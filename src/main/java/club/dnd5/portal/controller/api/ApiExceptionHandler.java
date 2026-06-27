package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.ApiErrorResponse;
import org.hibernate.JDBCException;
import org.hibernate.exception.JDBCConnectionException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.PersistenceException;
import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;

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

	@ExceptionHandler({DataAccessException.class, PersistenceException.class})
	public ResponseEntity<ApiErrorResponse> handlePersistenceException(Exception exception, HttpServletRequest request) {
		HttpStatus status = exception instanceof JDBCConnectionException
			? HttpStatus.SERVICE_UNAVAILABLE
			: HttpStatus.INTERNAL_SERVER_ERROR;
		return ResponseEntity
			.status(status)
			.body(new ApiErrorResponse(status.value(), status.getReasonPhrase(), getMostSpecificMessage(exception), request.getRequestURI()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleException(Exception exception, HttpServletRequest request) {
		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
		return ResponseEntity
			.status(status)
			.body(new ApiErrorResponse(status.value(), status.getReasonPhrase(), getMostSpecificMessage(exception), request.getRequestURI()));
	}

	private String getMostSpecificMessage(Throwable exception) {
		Throwable current = exception;
		Throwable mostSpecific = exception;
		while (current != null) {
			if (current instanceof SQLException || current instanceof JDBCException) {
				mostSpecific = current;
			}
			if (current.getCause() == null || current.getCause() == current) {
				break;
			}
			current = current.getCause();
		}
		String message = mostSpecific.getMessage();
		return message == null ? exception.getMessage() : message;
	}
}
