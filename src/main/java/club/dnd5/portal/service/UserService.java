package club.dnd5.portal.service;

import club.dnd5.portal.model.user.User;
import club.dnd5.portal.repository.VerificationToken;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
public interface UserService {
	Optional<User> findByUsername(String username);
	Optional<User> findByEmail(String email);

	@Transactional
	void createVerificationToken(User user, String token);

	VerificationToken getVerificationToken(String token);
}
