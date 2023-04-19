package club.dnd5.portal.dto.fvtt.export;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FDiscription {
	private static final Pattern pattern = Pattern.compile("<dice-roller formula=\"\\d*(к|d)\\d*(\\s?(\\+|-)?\\s?\\d+){0,}\"\\/{0,}>(\\+?\\d*к?\\d*\\s?\\+?\\s?\\d*<\\/dice-roller>)?");
	private static final Pattern patternFormula = Pattern.compile("\"\\d*(к|d)\\d+\\s*\\+?\\s*\\d*");

	private String value;
	private String chat ="";
	private String unidentified ="";

	public FDiscription(String description) {
    	Matcher matcher = pattern.matcher(description);
    	while (matcher.find()) {
    		String group = matcher.group();
			Matcher formula = patternFormula.matcher(group);
			if (formula.find()) {
				String stringFormula = String.format("[[/r %s]]", formula.group().replace('к', 'd'));
				description = description.replace(group, stringFormula);
			}
    	}
		value = description;
	}
}
