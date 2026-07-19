package club.dnd5.portal.service.tracker;

import club.dnd5.portal.dto.api.tracker.ParticipantResponse;
import club.dnd5.portal.dto.api.tracker.TrackerDetailedResponse;
import club.dnd5.portal.dto.api.tracker.TrackerShortResponse;
import club.dnd5.portal.model.tracker.InitiativeParticipant;
import club.dnd5.portal.model.tracker.InitiativeTracker;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Ручное отображение сущностей трекера в DTO (MapStruct в проекте не используется).
 */
@Component
public class InitiativeTrackerMapper {

	/**
	 * Ответ на создание — единственный, в котором отдаётся секретный ключ доступа
	 * {@code accessKey} (аноним сохраняет его на клиенте).
	 */
	public TrackerDetailedResponse toCreatedResponse(InitiativeTracker tracker,
													 List<InitiativeParticipant> participants) {
		return toDetailed(tracker, participants, tracker.getAccessKey());
	}

	public TrackerDetailedResponse toDetailedResponse(InitiativeTracker tracker,
													  List<InitiativeParticipant> participants) {
		return toDetailed(tracker, participants, null);
	}

	private TrackerDetailedResponse toDetailed(InitiativeTracker tracker,
											   List<InitiativeParticipant> participants,
											   java.util.UUID accessKey) {
		TrackerDetailedResponse response = new TrackerDetailedResponse();
		response.setId(tracker.getId());
		response.setName(tracker.getName());
		response.setStatus(tracker.getStatus());
		response.setStatusName(tracker.getStatus().getName());
		response.setRound(tracker.getRound());
		response.setRerollEachRound(tracker.isRerollEachRound());
		response.setCurrentParticipantId(tracker.getCurrentParticipantId());
		response.setAccessKey(accessKey);
		response.setCreatedAt(tracker.getCreatedAt());
		response.setUpdatedAt(tracker.getUpdatedAt());
		response.setParticipants(toParticipantResponseList(participants));
		return response;
	}

	public TrackerShortResponse toShortResponse(InitiativeTracker tracker) {
		TrackerShortResponse response = new TrackerShortResponse();
		response.setId(tracker.getId());
		response.setName(tracker.getName());
		response.setStatus(tracker.getStatus());
		response.setStatusName(tracker.getStatus().getName());
		response.setRound(tracker.getRound());
		response.setRerollEachRound(tracker.isRerollEachRound());
		response.setDeleted(tracker.isDeleted());
		response.setCreatedAt(tracker.getCreatedAt());
		response.setUpdatedAt(tracker.getUpdatedAt());
		return response;
	}

	public List<TrackerShortResponse> toShortResponseList(Collection<InitiativeTracker> trackers) {
		return trackers.stream().map(this::toShortResponse).collect(Collectors.toList());
	}

	public ParticipantResponse toParticipantResponse(InitiativeParticipant participant) {
		ParticipantResponse response = new ParticipantResponse();
		response.setId(participant.getId());
		response.setType(participant.getType());
		response.setTypeName(participant.getType().getName());
		response.setName(participant.getName());
		response.setInitiativeBonus(participant.getInitiativeBonus());
		response.setDead(participant.isDead());
		response.setInitiativeRoll(participant.getInitiativeRoll());
		response.setInitiativeTotal(participant.getInitiativeTotal());
		response.setCreatureUrl(participant.getCreatureUrl());
		return response;
	}

	public List<ParticipantResponse> toParticipantResponseList(Collection<InitiativeParticipant> participants) {
		return participants.stream().map(this::toParticipantResponse).collect(Collectors.toList());
	}
}
