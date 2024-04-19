package club.dnd5.portal.mappers;


import club.dnd5.portal.dto.api.NameValueApi;
import club.dnd5.portal.dto.api.bestiary.SenseApi;
import club.dnd5.portal.model.creature.Creature;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface SenseMapper {
	SenseMapper INSTANCE = Mappers.getMapper(SenseMapper.class);

	@Named("mapDarkVision")
	default Integer mapDarkVision(SenseApi senseApi) {
		if (senseApi.getSenses() != null) {
			for (NameValueApi sense : senseApi.getSenses()) {
				if ("тёмное зрение".equals(sense.getName())) {
					return Integer.valueOf(sense.getValue().toString());
				}
			}
		}
		return 0;
	}

	@Named("fillCreatureFromSenseApi")
	default void fillCreatureFromSenseApi(SenseApi senseApi, @MappingTarget Creature creature) {
		if (senseApi.getPassivePerception() != null) {
			creature.setPassivePerception((byte) Integer.parseInt(senseApi.getPassivePerception()));
		}
		if (senseApi.getSenses() != null) {
			for (NameValueApi sense : senseApi.getSenses()) {
				String name = sense.getName();
				String value = sense.getValue().toString();
				switch (name) {
					case "тёмное зрение":
						creature.setDarkvision(Integer.parseInt(value));
						break;
					case "истинное зрение":
						creature.setTrysight(Integer.parseInt(value));
						break;
					case "слепое зрение":
						creature.setBlindsight(Integer.parseInt(value));
						if (sense.getAdditional() != null && sense.getAdditional().equals("слеп за пределами этого радиуса")) {
							creature.setBlindsightRadius(1);
						}
						break;
					case "чувство вибрации":
						creature.setVibration(Integer.parseInt(value));
						if (sense.getAdditional() != null && sense.getAdditional().equals("слеп за пределами этого радиуса")) {
							creature.setBlindsightRadius(1);
						}
						break;
					default:
						break;
				}
			}
		}
	}
}
