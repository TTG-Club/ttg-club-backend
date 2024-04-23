package club.dnd5.portal.mappers;

import club.dnd5.portal.dto.api.NameValueApi;
import club.dnd5.portal.dto.api.bestiary.LegendaryApi;
import club.dnd5.portal.dto.api.bestiary.request.ActionDetailRequest;
import club.dnd5.portal.dto.api.classes.NameApi;
import club.dnd5.portal.model.creature.Action;
import club.dnd5.portal.model.creature.ActionType;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/*
 * These mapping methods are used for fields like those in the BeastDetailRequest class:
 * - mysticalActions: Collection<DescriptionRequest>
 *   - bonusActions: Collection<DescriptionRequest>
 *     - reactions: Collection<ActionDetailRequest>
 *       - actions: Collection<ActionDetailRequest>
 *
 * These methods are responsible for mapping nested collections of actions and descriptions
 * to corresponding entities or DTOs.
 */
@Mapper(componentModel = "spring")
public interface ActionMapper {
	//TODO подумать над тем, что действий много и их мне надо в 1 лист запихнуть

	ActionMapper INSTANCE = Mappers.getMapper(ActionMapper.class);

	default List<Action> mapLegendaryAction(LegendaryApi legendaryApi) {
		Collection<NameValueApi> list = legendaryApi.getList();
		List<Action> actions = new ArrayList<>();
		for (NameValueApi nameValueApi : list) {
			Action action = new Action();
			action.setName(nameValueApi.getName());
			//разобраться какой дескрипшен тут вставляется, ибо есть ещё вариант значение value с nameValueApi
			action.setDescription(legendaryApi.getDescription());
			action.setActionType(ActionType.LEGENDARY);
			actions.add(action);
		}
		return actions;
	}

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
