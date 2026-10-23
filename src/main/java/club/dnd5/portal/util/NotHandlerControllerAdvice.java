package club.dnd5.portal.util;

import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.exception.StorageException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

// Страницы отдаёт фронт: бэкенд на ненайденное отвечает только статусом 404.
@ControllerAdvice
public class NotHandlerControllerAdvice {
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler({
		PageNotFoundException.class,
		StorageException.class,
		NoHandlerFoundException.class
	})
	public void handleNotFound() {
		// Тело ответа не нужно
	}
}
