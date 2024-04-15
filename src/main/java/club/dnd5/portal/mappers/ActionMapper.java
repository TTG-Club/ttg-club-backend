package club.dnd5.portal.mappers;

import club.dnd5.portal.dto.api.bestiary.request.ActionDetailRequest;
import club.dnd5.portal.dto.api.classes.NameApi;
import club.dnd5.portal.model.creature.Action;
import club.dnd5.portal.model.creature.ActionType;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ActionMapper {
	ActionMapper INSTANCE = Mappers.getMapper(ActionMapper.class);

	List<Action> toEntities(Collection<ActionDetailRequest> actions);

	@Named("mapActions")
	default List<Action> mapActions(Collection<ActionDetailRequest> actions) {
		return actions.stream()
			.map(this::mapToAction)
			.collect(Collectors.toList());
	}

	default Action mapToAction(ActionDetailRequest actionDetailRequest) {
		Action action = new Action();
		NameApi nameApi = actionDetailRequest.getName();
		action.setActionType(ActionType.valueOf(actionDetailRequest.getActionType().toUpperCase()));
		action.setDescription(actionDetailRequest.getDescription());
		action.setName(nameApi.getRus());
		action.setEnglishName(nameApi.getEng());
		return action;
	}
}
