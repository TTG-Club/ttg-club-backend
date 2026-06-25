package club.dnd5.portal.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Hidden
@Controller
public class SpaController {
	private static final Resource INDEX = new ClassPathResource("templates/spa.html");

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
		"/workshop/bestiary",
		"/workshop/bestiary/create",
		"/workshop/bestiary/{name}/edit",
		"/workshop/feats",
		"/workshop/feats/create",
		"/workshop/feats/{name}/edit",
		"/workshop/spells",
		"/workshop/spells/create",
		"/workshop/spells/{name}/edit",
		"/workshop/weapons",
		"/workshop/weapons/create",
		"/workshop/weapons/{name}/edit",
		"/tools/ability-calc",
		"/tools/encounters",
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
	public ResponseEntity<Resource> getIndex() {
		return ResponseEntity
			.ok()
			.contentType(MediaType.TEXT_HTML)
			.body(INDEX);
	}
}
