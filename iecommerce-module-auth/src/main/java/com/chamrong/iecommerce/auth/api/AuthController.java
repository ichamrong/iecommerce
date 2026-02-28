package com.chamrong.iecommerce.auth.api;

import com.chamrong.iecommerce.auth.application.command.ChangePasswordCommand;
import com.chamrong.iecommerce.auth.application.command.LoginCommand;
import com.chamrong.iecommerce.auth.application.command.RegisterCommand;
import com.chamrong.iecommerce.auth.application.command.auth.LoginUserHandler;
import com.chamrong.iecommerce.auth.application.command.auth.LogoutCommand;
import com.chamrong.iecommerce.auth.application.command.auth.LogoutHandler;
import com.chamrong.iecommerce.auth.application.command.auth.RefreshTokenCommand;
import com.chamrong.iecommerce.auth.application.command.auth.RefreshTokenHandler;
import com.chamrong.iecommerce.auth.application.command.password.ForgotPasswordCommand;
import com.chamrong.iecommerce.auth.application.command.password.ForgotPasswordHandler;
import com.chamrong.iecommerce.auth.application.command.security.ChangePasswordHandler;
import com.chamrong.iecommerce.auth.application.command.user.RegisterUserHandler;
import com.chamrong.iecommerce.auth.application.dto.AuthResponse;
import com.chamrong.iecommerce.auth.application.query.ListSocialProvidersQueryHandler;
import com.chamrong.iecommerce.auth.domain.idp.SocialProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public authentication endpoints — registration, login, forgot-password. No JWT required. */
@Tag(
    name = "Authentication",
    description =
        "Public endpoints for user registration, login, and password reset. No token required.")
@SecurityRequirements
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final RegisterUserHandler registerUserHandler;
  private final LoginUserHandler loginUserHandler;
  private final ForgotPasswordHandler forgotPasswordHandler;
  private final ChangePasswordHandler changePasswordHandler;
  private final RefreshTokenHandler refreshTokenHandler;
  private final LogoutHandler logoutHandler;
  private final ListSocialProvidersQueryHandler listSocialProvidersQueryHandler;

  public AuthController(
      RegisterUserHandler registerUserHandler,
      LoginUserHandler loginUserHandler,
      ForgotPasswordHandler forgotPasswordHandler,
      ChangePasswordHandler changePasswordHandler,
      RefreshTokenHandler refreshTokenHandler,
      LogoutHandler logoutHandler,
      ListSocialProvidersQueryHandler listSocialProvidersQueryHandler) {
    this.registerUserHandler = registerUserHandler;
    this.loginUserHandler = loginUserHandler;
    this.forgotPasswordHandler = forgotPasswordHandler;
    this.changePasswordHandler = changePasswordHandler;
    this.refreshTokenHandler = refreshTokenHandler;
    this.logoutHandler = logoutHandler;
    this.listSocialProvidersQueryHandler = listSocialProvidersQueryHandler;
  }

  /**
   * Register a new user account.
   *
   * <p>POST /api/v1/auth/register
   */
  @Operation(
      summary = "Register a new user",
      description =
          "Creates a new user account and returns JWT tokens. No authentication required.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "User registered successfully",
        content = @Content(schema = @Schema(implementation = AuthResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = "Username or email already exists",
        content = @Content)
  })
  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterCommand cmd) {
    var response = registerUserHandler.handle(cmd);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Authenticate an existing user and return a JWT.
   *
   * <p>POST /api/v1/auth/login
   *
   * <p>If the response body contains {@code "requires_password_change": true}, the frontend must
   * redirect to the change-password screen before allowing any other action.
   */
  @Operation(
      summary = "Login",
      description =
          "Authenticates a user with username/password and returns JWT tokens. Check"
              + " `requires_password_change` in the response — if true, redirect to"
              + " change-password.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Login successful",
        content = @Content(schema = @Schema(implementation = AuthResponse.class))),
    @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content),
    @ApiResponse(responseCode = "429", description = "Too many requests", content = @Content)
  })
  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginCommand cmd) {
    var response = loginUserHandler.handle(cmd);
    return ResponseEntity.ok(response);
  }

  /**
   * Refresh an access token.
   *
   * <p>POST /api/v1/auth/refresh-token
   */
  @Operation(
      summary = "Refresh Token",
      description =
          "Exchanges a valid refresh token for a new access token and refresh token pair.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Token refreshed successfully",
        content = @Content(schema = @Schema(implementation = AuthResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Invalid or expired refresh token",
        content = @Content)
  })
  @PostMapping("/refresh-token")
  public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenCommand cmd) {
    return ResponseEntity.ok(refreshTokenHandler.handle(cmd));
  }

  /**
   * Log out a user.
   *
   * <p>POST /api/v1/auth/logout
   */
  @Operation(
      summary = "Log out user",
      description = "Logs out a user by revoking their refresh token via the Identity Provider.")
  @ApiResponses({@ApiResponse(responseCode = "204", description = "User logged out successfully")})
  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@Valid @RequestBody LogoutCommand cmd) {
    logoutHandler.handle(cmd);
    return ResponseEntity.noContent().build();
  }

  /**
   * Get social providers.
   *
   * <p>GET /api/v1/auth/social-providers
   */
  @Operation(
      summary = "List social providers",
      description =
          "Retrieves a list of configured and enabled social identity providers (e.g., Google,"
              + " GitHub).")
  @ApiResponses({@ApiResponse(responseCode = "200", description = "List of social providers")})
  @GetMapping("/social-providers")
  public ResponseEntity<List<SocialProvider>> getSocialProviders() {
    return ResponseEntity.ok(listSocialProvidersQueryHandler.handle());
  }

  /**
   * Request a password-reset email.
   *
   * <p>POST /api/v1/auth/forgot-password
   *
   * <p>Always returns {@code 200 OK} regardless of whether the email is registered, to prevent user
   * enumeration (OWASP A07).
   */
  @Operation(
      summary = "Forgot password",
      description =
          "Sends a password-reset email to the provided address if an account exists. "
              + "Always returns 200 OK to prevent user enumeration (OWASP A07).")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Request accepted", content = @Content),
    @ApiResponse(responseCode = "429", description = "Too many requests", content = @Content)
  })
  @PostMapping("/forgot-password")
  public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordCommand cmd) {
    forgotPasswordHandler.handle(cmd);
    return ResponseEntity.ok().build();
  }

  /**
   * Change the authenticated user's own password.
   *
   * <p>POST /api/v1/auth/change-password — requires a valid JWT.
   */
  @Operation(
      summary = "Change password",
      description =
          "Verifies the current password then sets a new one. "
              + "Use this endpoint to fulfil a forced first-login password reset.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "204",
        description = "Password changed successfully",
        content = @Content),
    @ApiResponse(responseCode = "401", description = "Invalid current password", content = @Content)
  })
  @PostMapping("/change-password")
  public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordCommand cmd) {
    changePasswordHandler.handle(cmd);
    return ResponseEntity.noContent().build();
  }
}
