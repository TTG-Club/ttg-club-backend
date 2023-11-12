package club.dnd5.portal.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@ControllerAdvice
public class VersionControllerAdvice {
	@Value("${frontend.application.sha:1}")
	private String version;
	@Value("${spring.profiles.active:prod}")
	private String profile;

	@ModelAttribute
	public void handleRequest(HttpServletRequest request, Model model) {
		if (version == null || version.isEmpty()) {
			version = String.valueOf(new Date().getTime());
		}

		model.addAttribute("version", version);
		model.addAttribute("profile", profile);

		String themeName = "dark";
		Cookie[] cookies = request.getCookies();

		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("ttg_theme_name")) {
					themeName = cookie.getValue();
				}
			}
		}

		model.addAttribute("themeName", themeName);
	}
}
