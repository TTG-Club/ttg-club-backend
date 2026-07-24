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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@RequiredArgsConstructor
@Tag(name = "Authorization and registration", description = "The User API")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthApiController {
	private static final String ACCESS_TOKEN_COOKIE = "dnd5_user_token";
	private static final String REFRESH_TOKEN_COOKIE = "dnd5_refresh_token";
	private static final String REFRESH_COOKIE_PATH = "/api/v1/auth";
	private static final String REMEMBER_PREFIX = "1.";
	private static final String SESSION_PREFIX = "0.";

	private final ExternalAuthClient externalAuthClient;
	private final ExternalAuthUserSynchronizer userSynchronizer;

	@Operation(summary = "User authorization by nickname or email address")
	@PostMapping("/signin")
	public ResponseEntity<JWTAuthResponse> authenticateUser(@RequestBody LoginDto loginDto, HttpServletRequest request, HttpServletResponse response) {
		try {
			JWTAuthResponse authResponse = externalAuthClient.login(loginDto);
			ExternalAuthUser externalUser = externalAuthClient.me(authResponse.getAccessToken());
			userSynchronizer.sync(externalUser);

			boolean remember = Boolean.TRUE.equals(loginDto.getRemember());
			addTokenCookies(response, authResponse, remember, isSecure(request));

			return ResponseEntity.ok(authResponse);
		}
		catch (HttpStatusCodeException exception) {
			return ResponseEntity.status(exception.getStatusCode()).build();
		}
	}

	@Operation(summary = "Refresh user access token")
	@PostMapping("/refresh")
	public ResponseEntity<JWTAuthResponse> refresh(
			@CookieValue(value = REFRESH_TOKEN_COOKIE, required = false) String storedRefreshToken,
			HttpServletRequest request,
			HttpServletResponse response) {
		if (!StringUtils.hasText(storedRefreshToken) || storedRefreshToken.length() < 3) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		boolean remember = storedRefreshToken.startsWith(REMEMBER_PREFIX);
		String refreshToken = storedRefreshToken.substring(2);

		try {
			JWTAuthResponse authResponse = externalAuthClient.refresh(refreshToken);
			addTokenCookies(response, authResponse, remember, isSecure(request));
			return ResponseEntity.ok(authResponse);
		}
		catch (HttpStatusCodeException exception) {
			clearTokenCookies(response, isSecure(request));
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
	public ResponseEntity<?> signout(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
		clearTokenCookies(response, isSecure(request));
		session.invalidate();
		return ResponseEntity.ok().build();
	}

	private void addTokenCookies(HttpServletResponse response, JWTAuthResponse authResponse, boolean remember, boolean secure) {
		addCookie(response, ACCESS_TOKEN_COOKIE, authResponse.getAccessToken(), "/",
				cookieMaxAge(remember, authResponse.getExpiresIn()), secure);
		String refreshValue = (remember ? REMEMBER_PREFIX : SESSION_PREFIX) + authResponse.getRefreshToken();
		addCookie(response, REFRESH_TOKEN_COOKIE, refreshValue, REFRESH_COOKIE_PATH,
				cookieMaxAge(remember, authResponse.getRefreshExpiresIn()), secure);
	}

	private long cookieMaxAge(boolean remember, long tokenLifetime) {
		return remember && tokenLifetime > 0 ? tokenLifetime : -1;
	}

	private void clearTokenCookies(HttpServletResponse response, boolean secure) {
		addCookie(response, ACCESS_TOKEN_COOKIE, "", "/", 0, secure);
		addCookie(response, REFRESH_TOKEN_COOKIE, "", REFRESH_COOKIE_PATH, 0, secure);
	}

	private void addCookie(HttpServletResponse response, String name, String value, String path, long maxAge, boolean secure) {
		ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
				.httpOnly(true)
				.secure(secure)
				// Lax, а не Strict: при переходе на сайт по внешней ссылке Strict не отдаёт куку
				// на первую навигацию — пользователь видит «разлогин»-мигание. Lax это устраняет,
				// оставаясь защитой от CSRF (кука не уходит на кросс-сайтовых POST/подзапросах).
				.sameSite("Lax")
				.path(path);
		if (maxAge >= 0) {
			builder.maxAge(maxAge);
		}
		response.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
	}

	private boolean isSecure(HttpServletRequest request) {
		return request.isSecure() || "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"));
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
