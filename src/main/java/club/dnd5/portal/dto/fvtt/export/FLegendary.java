package club.dnd5.portal.dto.fvtt.export;

import club.dnd5.portal.util.markup.Markup;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import club.dnd5.portal.model.creature.Action;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FLegendary {
    public String name;
    public List<String> entries;
    public FLegendary(Action action) {
    	name = action.getName();
    	entries = Arrays.stream(Markup.htmlOrEmpty(action.getDescription()).replace("<p>", "").split("</p>")).filter(t -> !t.isEmpty()).collect(Collectors.toList());
    }
}
