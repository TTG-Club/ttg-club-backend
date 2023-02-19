package club.dnd5.portal.util;

import club.dnd5.portal.exception.PageNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class NotHandlerControllerAdvice {
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(PageNotFoundException.class)
	public String handlePageNotFound(Exception exception) {
		return "spa";
	}

	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(NoHandlerFoundException.class)
	public String handleNoHandlePageException(Exception exception) {
		return "spa";
	}
}
