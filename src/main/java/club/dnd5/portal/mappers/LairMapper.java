package club.dnd5.portal.mappers;

import club.dnd5.portal.dto.api.bestiary.LairApi;
import club.dnd5.portal.model.creature.Lair;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface LairMapper {
	LairMapper INSTANCE = Mappers.getMapper(LairMapper.class);

	@Named("mapLair")
	default Lair mapLair(LairApi lairApi) {
		if (lairApi == null) {
			return null;
		}
		Lair lair = new Lair();
		lair.setDescription(lairApi.getDescription());
		lair.setAction(lairApi.getAction());
		lair.setEffect(lairApi.getEffect());
		return lair;
	}
}
