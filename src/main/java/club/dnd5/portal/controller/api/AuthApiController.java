package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.TokenValidationApi;
import club.dnd5.portal.dto.api.UserApi;
import club.dnd5.portal.dto.user.ChangePassword;
import club.dnd5.portal.dto.user.LoginDto;
import club.dnd5.portal.dto.user.SignUpDto;
import club.dnd5.portal.dto.user.UserDto;
import club.dnd5.portal.security.ExternalAuthClient;
import club.dnd5.portal.security.ExternalAuthUser;
import club.dnd5.portal.security.ExternalAuthUserSynchronizer;
import club.dnd5.portal.security.JWTAuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@RequiredArgsConstructor
@Tag(name = "Authorization and registration", description = "The User API")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthApiController {
	private final ExternalAuthClient externalAuthClient;
	private final ExternalAuthUserSynchronizer userSynchronizer;

	@Operation(summary = "User authorization by nickname or email address")
	@PostMapping("/signin")
	public ResponseEntity<JWTAuthResponse> authenticateUser(@RequestBody LoginDto loginDto, HttpServletResponse response) {
		try {
			JWTAuthResponse authResponse = externalAuthClient.login(loginDto);
			ExternalAuthUser externalUser = externalAuthClient.me(authResponse.getAccessToken());
			userSynchronizer.sync(externalUser);

			Cookie cookie = new Cookie("dnd5_user_token", authResponse.getAccessToken());
			if (Boolean.TRUE.equals(loginDto.getRemember())) {
				cookie.setMaxAge(365 * 24 * 60 * 60);
			}
			else {
				cookie.setMaxAge(24 * 60 * 60);
			}
			cookie.setPath("/");
			cookie.setHttpOnly(true);
			cookie.setSecure(true);
			response.addCookie(cookie);

			return ResponseEntity.ok(authResponse);
		}
		catch (HttpStatusCodeException exception) {
			return ResponseEntity.status(exception.getStatusCode()).build();
		}
	}

	@Operation(summary = "New user registration")
	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@RequestBody SignUpDto signUpDto) {
		try {
			ExternalAuthUser externalUser = externalAuthClient.register(signUpDto);
			userSynchronizer.sync(externalUser);
			return new ResponseEntity<>("User registered successfully", HttpStatus.OK);
		}
		catch (HttpStatusCodeException exception) {
			return ResponseEntity.status(exception.getStatusCode()).body(exception.getResponseBodyAsString());
		}
	}

	@Operation(summary = "Log out user current session")
	@PostMapping("/signout")
	public ResponseEntity<?> signout(HttpSession session, HttpServletResponse response) {
		Cookie cookie = new Cookie("dnd5_user_token", "");
		cookie.setMaxAge(0);
		cookie.setPath("/");
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		response.addCookie(cookie);
		session.invalidate();
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "Nickname and mailing address check")
	@PostMapping("/exist")
	public ResponseEntity<?> isUserNotExist(@RequestBody UserDto user) {
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "Change password by token")
	@PostMapping("/change/password")
	public ResponseEntity<?> changePassword(
			@RequestBody ChangePassword passwordDto,
			@CookieValue(value = "dnd5_user_token", required = false) String cookieToken) {
		try {
			if (StringUtils.hasText(passwordDto.getResetToken())) {
				externalAuthClient.confirmPasswordReset(passwordDto.getResetToken(), passwordDto.getPassword());
				return ResponseEntity.ok().build();
			}
			String accessToken = StringUtils.hasText(passwordDto.getUserToken())
					? passwordDto.getUserToken()
					: cookieToken;
			if (StringUtils.hasText(accessToken) && StringUtils.hasText(passwordDto.getCurrentPassword())) {
				externalAuthClient.changePassword(accessToken, passwordDto);
				return ResponseEntity.ok().build();
			}
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
		catch (HttpStatusCodeException exception) {
			return ResponseEntity.status(exception.getStatusCode()).body(exception.getResponseBodyAsString());
		}
	}

	@Operation(summary = "Send token for reset password by email")
	@GetMapping("/change/password")
	public ResponseEntity<UserApi> changeUserPassword(String email) {
		try {
			externalAuthClient.requestPasswordReset(email);
			return ResponseEntity.ok().build();
		}
		catch (HttpStatusCodeException exception) {
			return ResponseEntity.status(exception.getStatusCode()).build();
		}
	}

	@Operation(summary = "Verify password reset token")
	@GetMapping("/password/reset-token/validate")
	public ResponseEntity<TokenValidationApi> validatePasswordResetToken(@RequestParam String token) {
		try {
			externalAuthClient.validatePasswordResetToken(token);
			return ResponseEntity.ok(new TokenValidationApi(true, ""));
		}
		catch (HttpStatusCodeException exception) {
			return ResponseEntity.status(exception.getStatusCode())
					.body(new TokenValidationApi(false, exception.getResponseBodyAsString()));
		}
	}

	@Operation(summary = "Verify user email by token")
	@GetMapping("/verify-email")
	public ResponseEntity<?> verifyEmail(@RequestParam String token) {
		try {
			externalAuthClient.verifyEmail(token);
			return ResponseEntity.ok().build();
		}
		catch (HttpStatusCodeException exception) {
			return ResponseEntity.status(exception.getStatusCode()).body(exception.getResponseBodyAsString());
		}
	}

	@Operation(summary = "Check token exist")
	@GetMapping("/token/validate")
	public ResponseEntity<TokenValidationApi> existToken(@RequestParam String token) {
		try {
			return ResponseEntity.ok(new TokenValidationApi(externalAuthClient.validateAccessToken(token), ""));
		}
		catch (HttpStatusCodeException exception) {
			return ResponseEntity.status(exception.getStatusCode())
					.body(new TokenValidationApi(false, exception.getResponseBodyAsString()));
		}
	}
}
