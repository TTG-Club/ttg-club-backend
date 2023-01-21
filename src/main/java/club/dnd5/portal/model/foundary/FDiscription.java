package club.dnd5.portal.model.foundary;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FDiscription {
	private static final Pattern pattern = Pattern.compile("<dice-roller formula=\"\\d{0,}(к|d)\\d+(\\s\\+\\s\\d+){0,}\"/{0,}>");

	private String value;
	private String chat ="";
	private String unidentified ="";

	public FDiscription(String description) {
    	Matcher matcher = pattern.matcher(description);
    	while (matcher.find()) {
    		String group = matcher.group();
    		String formula = group
    				.replace("<dice-roller formula=\"", "<strong>")
    				.replace("\"/>", "</strong>");
    		description = description.replace(group, formula);
    	}
		value = description;
	}
}
