package club.dnd5.portal.service;

import club.dnd5.portal.config.ValueProvider;
import club.dnd5.portal.exception.ApiException;
import club.dnd5.portal.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class EmailService {
	private final Environment environment;
	private static final String resetText = "/reset/password?token=";
	private final ValueProvider valueProvider;
	@Qualifier("userServiceImpl")
	private final UserService userService;
	private final JavaMailSender mailSender;

	@Async
	public void sendInvitationLink(List<User> users, String invitationLink) {
		String subject = "Приглашение в группу на ttg.club";
		String message = "Вас пригласили в группу на ttg.club. Пожалуйста, перейдите по ссылке для подтверждения:";
		sendEmailToUsers(users, subject, message, invitationLink);
	}

	@Async
	public void confirmRegistration(User user) {
		String token = UUID.randomUUID().toString();
		userService.createVerificationToken(user, token);

		String recipientAddress = user.getEmail();
		String subject = "Подтверждение регистрации";
		String confirmationUrl = "https://dev.ttg.club/confirm/email?token=" + token;
		String message = "Подтвердите ваш email адрес, перейдя по ссылке::";
		sendEmail(recipientAddress, subject, message, confirmationUrl);
	}

	@Async
	public void changePassword(User user) {
		String token = UUID.randomUUID().toString();
		userService.createVerificationToken(user, token);

		String recipientAddress = user.getEmail();
		String subject = "Сброс пароля на ttg.club";
		String[] profiles = this.environment.getActiveProfiles();
		String confirmationUrl;
		if ("dev".equals(profiles[0])) {
			confirmationUrl = valueProvider.getDevUrl() + resetText + token;
		} else {
			confirmationUrl = valueProvider.getTtgUrl() + resetText + token;
		}

		String message = "Для сброса пароля перейдите по ссылке и введите новый пароль:";
		sendEmail(recipientAddress, subject, message, confirmationUrl);
	}

	private void sendEmailToUsers(List<User> users, String subject, String message, String url) {
		for (User user : users) {
			String recipientAddress = user.getEmail();
			sendEmail(recipientAddress, subject, message, url);
		}
	}

	private void sendEmail(String recipientAddress, String subject, String message, String url) {
		SimpleMailMessage email = new SimpleMailMessage();
		email.setTo(recipientAddress);
		email.setFrom("support@ttg.club");
		email.setSubject(subject);
		email.setText(String.format("%s %s", message, url));
		try {
			mailSender.send(email);
		} catch (MailException e) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Error sending email");
		}
	}
}
