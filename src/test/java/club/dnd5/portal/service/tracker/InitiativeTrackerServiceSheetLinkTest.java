package club.dnd5.portal.service.tracker;

import club.dnd5.portal.dto.api.tracker.ParticipantAddRequest;
import club.dnd5.portal.dto.api.tracker.ParticipantResponse;
import club.dnd5.portal.dto.api.tracker.TrackerDetailedResponse;
import club.dnd5.portal.model.tracker.InitiativeParticipant;
import club.dnd5.portal.model.tracker.InitiativeTracker;
import club.dnd5.portal.model.tracker.ParticipantSheetLink;
import club.dnd5.portal.model.tracker.ParticipantType;
import club.dnd5.portal.model.tracker.TrackerStatus;
import club.dnd5.portal.repository.datatable.BestiaryRepository;
import club.dnd5.portal.repository.tracker.InitiativeParticipantRepository;
import club.dnd5.portal.repository.tracker.InitiativeTrackerRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.core.context.SecurityContextHolder;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Игрок из листа персонажа: привязка к листу приходит в запросе добавления, сохраняется у
 * участника и отдаётся в ответе — по ней фронтенд пишет хиты из боя обратно в лист.
 */
class InitiativeTrackerServiceSheetLinkTest {

	private InitiativeParticipantRepository participantRepository;
	private InitiativeTrackerService service;
	private InitiativeTracker tracker;
	private final List<InitiativeParticipant> stored = new ArrayList<>();

	@BeforeEach
	@SuppressWarnings("unchecked")
	void setUp() {
		SecurityContextHolder.clearContext();
		InitiativeTrackerRepository trackerRepository = mock(InitiativeTrackerRepository.class);
		participantRepository = mock(InitiativeParticipantRepository.class);
		service = new InitiativeTrackerService(trackerRepository, participantRepository,
				mock(InitiativeCombatService.class), mock(TrackerCreationRateLimiter.class),
				mock(BestiaryRepository.class), new InitiativeTrackerMapper());

		tracker = new InitiativeTracker();
		tracker.setId(UUID.randomUUID());
		tracker.setName("Бой");
		tracker.setAccessKey(UUID.randomUUID());
		tracker.setStatus(TrackerStatus.PREPARING);
		tracker.setCreatedAt(Instant.now());
		tracker.setUpdatedAt(Instant.now());

		when(trackerRepository.findById(tracker.getId())).thenReturn(Optional.of(tracker));
		when(participantRepository.saveAll(anyList())).thenAnswer(invocation -> {
			List<InitiativeParticipant> added = invocation.getArgument(0);
			added.forEach(participant -> participant.setId(UUID.randomUUID()));
			stored.addAll(added);
			return added;
		});
		when(participantRepository.findAllByTrackerId(any())).thenAnswer(invocation -> List.copyOf(stored));
	}

	private static ParticipantSheetLink savedLink() {
		return new ParticipantSheetLink(UUID.randomUUID(), ParticipantSheetLink.SOURCE_SAVED, UUID.randomUUID(),
				UUID.randomUUID());
	}

	@Test
	@SuppressWarnings("unchecked")
	void playerKeepsSheetLink() {
		ParticipantSheetLink link = savedLink();
		ParticipantAddRequest request = new ParticipantAddRequest(ParticipantType.PLAYER, "Торин", 3, null, null, link);

		TrackerDetailedResponse response =
				service.addParticipants(tracker.getId(), request, tracker.getAccessKey().toString());

		ArgumentCaptor<List<InitiativeParticipant>> captor = ArgumentCaptor.forClass(List.class);
		verify(participantRepository).saveAll(captor.capture());
		assertThat(captor.getValue()).singleElement()
				.satisfies(participant -> assertThat(participant.getSheetLink()).isSameAs(link));

		ParticipantResponse player = response.getParticipants().getFirst();
		assertThat(player.getSheetLink().getSheetId()).isEqualTo(link.getSheetId());
		assertThat(player.getSheetLink().getSavedId()).isEqualTo(link.getSavedId());
	}

	@Test
	void manualPlayerHasNoSheetLink() {
		ParticipantAddRequest request = new ParticipantAddRequest(ParticipantType.PLAYER, "Гимли", 1, null, null, null);

		TrackerDetailedResponse response =
				service.addParticipants(tracker.getId(), request, tracker.getAccessKey().toString());

		assertThat(response.getParticipants().getFirst().getSheetLink()).isNull();
		assertThat(new JsonMapper().writeValueAsString(response.getParticipants().getFirst()))
				.doesNotContain("sheetLink");
	}

	@Test
	void sheetLinkRoundTripsThroughJson() {
		ParticipantSheetLink link = new ParticipantSheetLink(UUID.randomUUID(), ParticipantSheetLink.SOURCE_OWN,
				null, null);
		JsonMapper mapper = new JsonMapper();

		String json = mapper.writeValueAsString(link);
		ParticipantSheetLink parsed = mapper.readValue(json, ParticipantSheetLink.class);

		assertThat(json).doesNotContain("savedId").doesNotContain("shareToken");
		assertThat(parsed.getSheetId()).isEqualTo(link.getSheetId());
		assertThat(parsed.getSource()).isEqualTo(ParticipantSheetLink.SOURCE_OWN);
	}

	@Test
	void sheetLinkSourceIsValidated() {
		Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
		ParticipantSheetLink link = savedLink();
		link.setSource("stolen");
		ParticipantAddRequest request = new ParticipantAddRequest(ParticipantType.PLAYER, "Торин", 0, null, null, link);

		assertThat(validator.validate(request)).isNotEmpty();
		assertThat(validator.validate(new ParticipantAddRequest(ParticipantType.PLAYER, "Торин", 0, null, null,
				savedLink()))).isEmpty();
	}
}
