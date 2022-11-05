package club.dnd5.portal.config;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@ControllerAdvice
public class VersionControllerAdvice {
	@Value("${spring.profiles.active}")
	private String profile;

	@ModelAttribute
	public void handleRequest(HttpServletRequest request, Model model) {
		model.addAttribute("version", getVersion());
		model.addAttribute("profile", profile);

		String themeName = "dark";
		Cookie[] cookies = request.getCookies();

		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("theme_name")) {
					themeName = cookie.getValue();
				}
			}
		}

		model.addAttribute("themeName", themeName);
	}

	private String getVersion() {
		try {
			InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("my.properties");
			Properties properties = new Properties();
			properties.load(inputStream);

			if (properties.getProperty("app.frontend.sha") != null) {
				return properties.getProperty("app.frontend.sha");
			}

			return "";
		} catch (IOException err) {
			return "";
		}
	}
}
