package club.dnd5.portal.dto.fvtt.plutonium;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import club.dnd5.portal.model.creature.CreatureFeat;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FTrait {
    public String name;
    public List<String> entries;
    public FTrait(CreatureFeat feat) {
    	name = feat.getName().replace("(перезарядка 6)", "{@recharge 6}").replace("(перезарядка 5-6)", "{@recharge 5}").replace("(перезарядка 4–6)", "{@recharge 4}");
    	entries = Arrays.stream(feat.getDescription()
    				.replace("<p>", "")
    				.replace("href=\"", "href=\"https://ttg.club/")
    				.replace(" class=\"tip_spell\"", "")
    				.split("</p>"))
    			.filter(t -> !t.isEmpty())
    			.collect(Collectors.toList());
    }
}
