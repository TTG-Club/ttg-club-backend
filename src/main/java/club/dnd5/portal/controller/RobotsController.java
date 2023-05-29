package club.dnd5.portal.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

@Hidden
@Controller
public class RobotsController {
	@GetMapping(value = {"/robots", "/robot", "/robot.txt", "/robots.txt", "/null"})
	public void robots(HttpServletResponse response) {
		InputStream resourceAsStream = null;
		try {
			ClassLoader classLoader = getClass().getClassLoader();
			resourceAsStream = classLoader.getResourceAsStream("robots.txt");
			response.addHeader("Content-disposition", "filename=robots.txt");
			response.setContentType("text/plain");
			IOUtils.copy(resourceAsStream, response.getOutputStream());
			response.flushBuffer();
		} catch (Exception e) {

		}
	}

	@GetMapping("/manifest.json")
	public void manifest(HttpServletResponse response) {
		InputStream resourceAsStream = null;
		try {
			ClassLoader classLoader = getClass().getClassLoader();
			resourceAsStream = classLoader.getResourceAsStream("static/manifest.json");
			response.addHeader("Content-disposition", "filename=manifest.json");
			response.setContentType("text/plain");
			IOUtils.copy(resourceAsStream, response.getOutputStream());
			response.flushBuffer();
		} catch (Exception e) {

		}
	}

	@GetMapping("/sitemap.xml")
	public void sitemap(HttpServletResponse response) {
		InputStream resourceAsStream = null;
		try {
			ClassLoader classLoader = getClass().getClassLoader();
			resourceAsStream = classLoader.getResourceAsStream("sitemap.xml");
			response.addHeader("Content-disposition", "filename=sitemap.xml");
			response.setContentType("text/plain");
			IOUtils.copy(resourceAsStream, response.getOutputStream());
			response.flushBuffer();
		} catch (Exception e) {

		}
	}
}
