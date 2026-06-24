package club.dnd5.portal.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Hidden
@Controller
public class SpaController {
	private static final String INDEX = "forward:/index.html";

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
	public String getIndex() {
		return INDEX;
	}
}
