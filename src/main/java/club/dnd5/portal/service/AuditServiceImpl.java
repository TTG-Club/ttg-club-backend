package club.dnd5.portal.service;

import club.dnd5.portal.dto.api.audit.RevisionInfoApi;
import club.dnd5.portal.model.audit.RevisionHistory;
import club.dnd5.portal.model.audit.RevisionOperation;
import club.dnd5.portal.model.user.User;
import club.dnd5.portal.repository.RevisionHistoryRepository;
import club.dnd5.portal.repository.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

	private final RevisionHistoryRepository revisionHistoryRepository;
	private final UserRepository userRepository;
	private final ObjectMapper objectMapper;

	@Override
	@SneakyThrows
	public void record(String entityType, Integer entityId, RevisionOperation operation, Object snapshotDto) {
		RevisionHistory history = new RevisionHistory();
		history.setEntityType(entityType);
		history.setEntityId(entityId);
		history.setRevision(revisionHistoryRepository.findMaxRevision(entityType, entityId) + 1);
		history.setOperation(operation);
		history.setSnapshot(objectMapper.writeValueAsString(snapshotDto));
		applyCurrentUser(history);
		revisionHistoryRepository.save(history);
	}

	@Override
	public List<RevisionInfoApi> getRevisions(String entityType, Integer entityId) {
		return revisionHistoryRepository.findByEntityTypeAndEntityIdOrderByRevisionDesc(entityType, entityId)
			.stream()
			.map(RevisionInfoApi::new)
			.collect(Collectors.toList());
	}

	@Override
	@SneakyThrows
	public <T> T getSnapshot(String entityType, Integer entityId, Integer revision, Class<T> type) {
		RevisionHistory history = revisionHistoryRepository
			.findByEntityTypeAndEntityIdAndRevision(entityType, entityId, revision)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Revision not found"));
		return objectMapper.readValue(history.getSnapshot(), type);
	}

	private void applyCurrentUser(RevisionHistory history) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			history.setUsername("system");
			return;
		}
		String name = authentication.getName();
		history.setUsername(name);
		userRepository.findByEmailOrUsername(name, name)
			.map(User::getId)
			.ifPresent(history::setUserId);
	}
}
