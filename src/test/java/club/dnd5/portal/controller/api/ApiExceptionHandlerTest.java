package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.ApiErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApiExceptionHandlerTest {
	@Test
	void returnsBadRequestForInvalidHtml() {
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
		bindingResult.addError(new FieldError("request", "description", "Некорректный HTML"));
		MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
			mock(MethodParameter.class),
			bindingResult
		);
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getRequestURI()).thenReturn("/api/v1/workshop/spells/42");

		ResponseEntity<ApiErrorResponse> response = new ApiExceptionHandler()
			.handleValidationException(exception, request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getMessage()).isEqualTo("description: Некорректный HTML");
		assertThat(response.getBody().getPath()).isEqualTo("/api/v1/workshop/spells/42");
	}
}
