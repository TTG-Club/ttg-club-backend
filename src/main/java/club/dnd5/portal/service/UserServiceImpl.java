package club.dnd5.portal.service;

import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.user.User;
import club.dnd5.portal.repository.VerificationToken;
import club.dnd5.portal.repository.user.UserRepository;
import club.dnd5.portal.repository.user.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
	private final UserRepository userRepository;
	private final VerificationTokenRepository tokenRepository;

	@Override
	public Optional<User> findByUsername(String username) {
		return userRepository.findByName(username);
	}

	@Override
	public Optional<User> findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	@Override
	public void createVerificationToken(User user, String token) {
		VerificationToken myToken = new VerificationToken(token, user);
		tokenRepository.save(myToken);
	}

	@Override
	public VerificationToken getVerificationToken(String token) {
		return tokenRepository.findByToken(token).orElseThrow(PageNotFoundException::new);
	}
}
