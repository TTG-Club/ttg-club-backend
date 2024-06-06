package club.dnd5.portal.interceptor;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.regex.Pattern;

@Component
public class RedirectToLowercaseInterceptor implements HandlerInterceptor {
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		String path = request.getServletPath();

		Pattern baseDir = Pattern.compile("^/(api|css|fonts|icon|img|js|style)/.*$", Pattern.CASE_INSENSITIVE);

		if (baseDir.matcher(path).find()) {
			return true;
		}

		Pattern incorrectLetters = Pattern.compile("(\\p{Lu}|\\p{Z})");

		if (incorrectLetters.matcher(path).find()) {
			response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
			response.setHeader(
				HttpHeaders.LOCATION,
				response.encodeRedirectURL(path.toLowerCase().replace(" ", "_"))
			);

			return false;
		}

		return true;
	}
}
