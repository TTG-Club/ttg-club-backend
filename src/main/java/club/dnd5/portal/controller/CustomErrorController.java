package club.dnd5.portal.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Hidden
@Controller
public class CustomErrorController implements ErrorController {
	@GetMapping({
		"/error",
		"/error.html",
		"/401",
		"/403",
		"/404",
		"/500"
	})
    public String handleError(HttpServletRequest request) {
        return "spa";
    }
}
