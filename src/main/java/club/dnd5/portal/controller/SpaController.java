package club.dnd5.portal.controller;

import club.dnd5.portal.controller.api.MetaApiController;
import club.dnd5.portal.dto.api.MetaApi;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Hidden
@Controller
public class SpaController {
	private static final ClassPathResource INDEX = new ClassPathResource("templates/spa.html");

	// Канонический домен старого справочника (редакция 2014). На него ссылается
	// self-canonical каждой страницы, чтобы сайт индексировался как самостоятельный
	// и не схлопывался с новым сайтом (new.ttg.club, редакция 2024).
	private static final String CANONICAL_ORIGIN = "https://5e14.ttg.club";

	// index.html кэшируем в памяти: на каждый запрос лишь подставляем canonical,
	// а не перечитываем ресурс с диска.
	private static volatile String indexTemplate;

	// Маркер редакции в серверном <title>/og:title — различимость от нового
	// сайта (new.ttg.club, редакция 2024).
	private static final String EDITION_SUFFIX = " (2014)";

	// Резолвер title/description по пути запроса (переиспользует логику /api/v1/meta).
	private final MetaApiController metaApiController;

	public SpaController(MetaApiController metaApiController) {
		this.metaApiController = metaApiController;
	}

	@GetMapping({
		"/",
		"/search",
		"/reset-password",
		"/verify-email",
		"/error",
		"/error.html",
		"/401",
		"/403",
		"/404",
		"/500",
		"/armors",
		"/armors/{name}",
		"/backgrounds",
		"/backgrounds/{name}",
		"/backgrounds/fragment/{id}",
		"/bestiary",
		"/bestiary/{name}",
		"/books",
		"/books/{name}",
		"/classes",
		"/classes/{name}",
		"/classes/{name}/{archetype}",
		"/classes/fragment/{englishName}",
		"/classes/fragment_id/{id}",
		"/classes/images/{englishName}",
		"/classes/spells/{englishName}",
		"/classes/options/{englishName}",
		"/classes/{englishName}/architypes/list",
		"/classes/{className}/architypes/{archetypeName}",
		"/feats",
		"/feats/{name}",
		"/traits",
		"/traits/{name}",
		"/gods",
		"/gods/{name}",
		"/info/{url}",
		"/items",
		"/items/{name}",
		"/items/magic",
		"/items/magic/{name}",
		"/magic-items",
		"/magic-items/{name}",
		"/options",
		"/options/{name}",
		"/profile",
		"/profile/{username}",
		"/profile/beast",
		"/profile/beast/trait",
		"/profile/beast/action",
		"/profile/beast/reaction",
		"/profile/beast/bonus",
		"/profile/beast/legendary",
		"/admin/bestiary/{id}",
		"/races",
		"/races/{name}",
		"/races/{raceEnglishName}/{subraceEnglishName}",
		"/rules",
		"/rules/{name}",
		"/screens",
		"/screens/{name}",
		"/screens/{name}/{subscreen}",
		"/spells",
		"/spells/{name}",
		"/workshop",
		"/workshop/armors",
		"/workshop/armors/create",
		"/workshop/armors/{name}/edit",
		"/workshop/backgrounds",
		"/workshop/backgrounds/create",
		"/workshop/backgrounds/{name}/edit",
		"/workshop/classes",
		"/workshop/classes/create",
		"/workshop/classes/{name}/edit",
		"/workshop/bestiary",
		"/workshop/bestiary/create",
		"/workshop/bestiary/{name}/edit",
		"/workshop/feats",
		"/workshop/feats/create",
		"/workshop/feats/{name}/edit",
		"/workshop/races",
		"/workshop/races/create",
		"/workshop/races/{name}/edit",
		"/workshop/spells",
		"/workshop/spells/create",
		"/workshop/spells/{name}/edit",
		"/workshop/weapons",
		"/workshop/weapons/create",
		"/workshop/weapons/{name}/edit",
		"/tools/ability-calc",
		"/tools/encounters",
		"/tools/initiative",
		"/tools/initiative/{id}",
		"/tools/items/magic",
		"/tools/madness",
		"/tools/names",
		"/tools/tavern",
		"/tools/tavern/habitates/",
		"/tools/trader",
		"/tools/treasury",
		"/tools/wildmagic",
		"/treasures",
		"/weapons",
		"/weapons/{name}"
	})
	@ResponseBody
	public ResponseEntity<String> getIndex(HttpServletRequest request) throws IOException {
		String path = request.getRequestURI();
		if (path.length() > 1 && path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}

		final StringBuilder head = new StringBuilder();

		// self-canonical на текущий путь (в пределах этого домена).
		head.append("<link rel=\"canonical\" href=\"")
			.append(CANONICAL_ORIGIN).append(escapeHtmlAttr(path)).append("\"/>");

		// Серверные title/description из тех же метаданных, что и /api/v1/meta,
		// чтобы их видел Яндекс (без JS). null → просто не инжектим.
		final MetaApi meta = metaApiController.resolveByPath(path);
		if (meta != null) {
			if (meta.getTitle() != null && !meta.getTitle().isEmpty()) {
				final String title = escapeHtmlAttr(meta.getTitle() + EDITION_SUFFIX);
				head.append("<title>").append(title).append("</title>");
				head.append("<meta property=\"og:title\" content=\"").append(title).append("\"/>");
			}
			if (meta.getDescription() != null && !meta.getDescription().isEmpty()) {
				final String description = escapeHtmlAttr(meta.getDescription());
				head.append("<meta name=\"description\" content=\"").append(description).append("\"/>");
				head.append("<meta property=\"og:description\" content=\"").append(description).append("\"/>");
			}
		}

		final String html = getIndexTemplate().replace("</head>", head + "</head>");

		return ResponseEntity
			.ok()
			.contentType(new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8))
			.body(html);
	}

	private static String getIndexTemplate() throws IOException {
		String template = indexTemplate;
		if (template == null) {
			synchronized (SpaController.class) {
				template = indexTemplate;
				if (template == null) {
					try (InputStream in = INDEX.getInputStream()) {
						template = StreamUtils.copyToString(in, StandardCharsets.UTF_8);
					}
					indexTemplate = template;
				}
			}
		}
		return template;
	}

	// Экранируем значения перед вставкой в HTML (href/атрибуты/текст). В путь
	// попадают пользовательские сегменты ({name}) — защита от reflected XSS.
	// HtmlUtils.htmlEscape экранирует < > & " и распознаётся статическим
	// анализом (CodeQL) как санитайзер.
	private static String escapeHtmlAttr(String value) {
		return HtmlUtils.htmlEscape(value);
	}
}
