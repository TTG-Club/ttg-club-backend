package club.dnd5.portal.mappers;

import club.dnd5.portal.dto.api.bestiary.request.DescriptionRequest;
import club.dnd5.portal.model.creature.CreatureFeat;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CreatureFeatMapper {
	CreatureFeatMapper INSTANCE = Mappers.getMapper(CreatureFeatMapper.class);

	@Named("mapFeats")
	default List<CreatureFeat> mapFeats(Collection<DescriptionRequest> feats) {
		return feats.stream()
			.map(this::mapToCreatureFeat)
			.collect(Collectors.toList());
	}

	default CreatureFeat mapToCreatureFeat(DescriptionRequest request) {
		CreatureFeat creatureFeat = new CreatureFeat();
		creatureFeat.setDescription(request.getDescription());
		creatureFeat.setName(request.getName().getRus());
		creatureFeat.setEnglishName(request.getName().getEng());
		return creatureFeat;
	}
}
