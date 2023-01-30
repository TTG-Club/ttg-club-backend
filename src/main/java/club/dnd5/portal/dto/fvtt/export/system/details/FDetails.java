package club.dnd5.portal.dto.fvtt.export.system.details;

import java.util.stream.Collectors;

import club.dnd5.portal.model.creature.Creature;
import club.dnd5.portal.model.creature.HabitatType;
import club.dnd5.portal.model.creature.Spellcater;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FDetails {
    public FBiography biography;
    public String alignment;
    public String race = "";
    public FType type;
    public String environment;
    public double cr;
    public int spellLevel;
    public String source;

	public FDetails(Creature creature) {
		biography = new FBiography(creature.getDescription(), "");
		alignment = creature.getAlignment().getCyrilicName();
		race = creature.getRaceName();
		type = new FType(creature.getType().name().toLowerCase(), "", "", "");
		environment = creature.getHabitates().stream().map(HabitatType::getName).collect(Collectors.joining(", "));
		switch (creature.getChallengeRating()) {
		case "1/2":
			cr = 1f/2;
			break;
		case "1/4":
			cr = 1f/4;
			break;
		case "1/8":
			cr = 1f/8;
			break;
		case "—":
			cr = 0;
			break;
		default:
			cr = Integer.valueOf(creature.getChallengeRating());
		}
		if (!creature.getSpellcasters().isEmpty()) {
			for (Spellcater spellcaster : creature.getSpellcasters()) {
				spellLevel = spellcaster.getLevel();
			}
		}
		source = creature.getBook().getSource();
	}
}
