package club.dnd5.portal.service;

import club.dnd5.portal.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class EmailService {
	private final Environment environment;
	@Qualifier("userServiceImpl")
	private final UserService service;

	private final JavaMailSender mailSender;

	@Async
	public void confirmRegistration(User user) {
		String token = UUID.randomUUID().toString();
		service.createVerificationToken(user, token);

		String recipientAddress = user.getEmail();
		String subject = "Подтверждение регистрации";
		String confirmationUrl = createConfirmationUrl(token);
		String message = "Подтвердите ваш email адрес, перейдя по ссылке:";

		sendEmail(recipientAddress, subject, message, confirmationUrl);
	}

	@Async
	public void changePassword(User user) {
		String token = UUID.randomUUID().toString();
		service.createVerificationToken(user, token);

		String recipientAddress = user.getEmail();
		String subject = "Сброс пароля на ttg.club";
		String confirmationUrl = createPasswordResetUrl(token);
		String message = "Для сброса пароля перейдите по ссылке и введите новый пароль:";

		sendEmail(recipientAddress, subject, message, confirmationUrl);
	}

	private String createConfirmationUrl(String token) {
		String[] profiles = environment.getActiveProfiles();
		if (profiles[0].equals("dev")) {
			return "https://dev.ttg.club/confirm/email?token=" + token;
		} else {
			return "https://ttg.club/confirm/email?token=" + token;
		}
	}

	private String createPasswordResetUrl(String token) {
		String[] profiles = environment.getActiveProfiles();
		if (profiles[0].equals("dev")) {
			return "https://dev.ttg.club/reset/password?token=" + token;
		} else {
			return "https://ttg.club/reset/password?token=" + token;
		}
	}

	private void sendEmail(String recipientAddress, String subject, String message, String confirmationUrl) {
		SimpleMailMessage email = new SimpleMailMessage();
		email.setTo(recipientAddress);
		email.setFrom("support@ttg.club");
		email.setSubject(subject);
		email.setText(String.format("%s %s", message, confirmationUrl));
		mailSender.send(email);
	}
}
