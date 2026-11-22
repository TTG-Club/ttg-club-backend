package club.dnd5.portal.dto.fvtt.plutonium;

import club.dnd5.portal.dto.fvtt.FvttHtml;
import club.dnd5.portal.util.markup.Markup;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import club.dnd5.portal.model.creature.Action;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FAction {
    public String name;
    public List<Object> entries;
    public FAction(Action action) {
    	name = action.getName()
    			.replace("(перезарядка 6)", "{@recharge 6}")
    			.replace("(перезарядка 5-6)", "{@recharge 5}")
    			.replace("(перезарядка 4–6)", "{@recharge 4}");
    	entries = new ArrayList<>(5);
    	if (Markup.htmlOrEmpty(action.getDescription()).contains("Рукопашная атака оружием")) {
    		Matcher matcher = Pattern.compile("\\d+").matcher(Markup.htmlOrEmpty(action.getDescription()));
    		matcher.find();
    		int attack = Integer.valueOf(matcher.group());
    		entries.add(String.format("{@atk mw} {@hit %d} ", attack));
    	}
    	if (Markup.htmlOrEmpty(action.getDescription()).contains("Дальнобойная атака оружием")) {
    		Matcher matcher = Pattern.compile("\\d+").matcher(Markup.htmlOrEmpty(action.getDescription()));
    		matcher.find();
    		int attack = Integer.valueOf(matcher.group());
    		entries.add(String.format("{@atk rw} {@hit %d} ", attack));
    	}
    	if (Markup.htmlOrEmpty(action.getDescription()).contains("Рукопашная или дальнобойная атака оружием")) {
    		Matcher matcher = Pattern.compile("\\d+").matcher(Markup.htmlOrEmpty(action.getDescription()));
    		matcher.find();
    		int attack = Integer.valueOf(matcher.group());
    		entries.add(String.format("{@atk mw,rw} {@hit %d} ", attack));
    	}
    	if (Markup.htmlOrEmpty(action.getDescription()).contains("Рукопашная атака заклинанием")) {
    		Matcher matcher = Pattern.compile("\\d+").matcher(Markup.htmlOrEmpty(action.getDescription()));
    		matcher.find();
    		int attack = Integer.valueOf(matcher.group());
    		entries.add(String.format("{@atk ms} {@hit %d} ", attack));
    	}
    	if (Markup.htmlOrEmpty(action.getDescription()).contains("Дальнобойная атака заклинанием")) {
    		Matcher matcher = Pattern.compile("\\d+").matcher(Markup.htmlOrEmpty(action.getDescription()));
    		if(matcher.find()) {
        		int attack = Integer.valueOf(matcher.group());
        		entries.add(String.format("{@atk rs} {@hit %d} ", attack));
    		}
    	}

    	String description = FvttHtml.absoluteLinks(
    		FvttHtml.rolls(Markup.htmlOrEmpty(action.getDescription()), formula -> "{@damage " + formula + "}"));
    	entries.addAll(Arrays.stream(description.replace("<p>", "").split("</p>"))
    			.filter(t -> !t.isEmpty())
    			.collect(Collectors.toList()));

    }
}
