package com.chamrong.iecommerce.auth.api;

import com.chamrong.iecommerce.auth.application.command.LoginCommand;
import com.chamrong.iecommerce.auth.application.command.LoginUserHandler;
import com.chamrong.iecommerce.auth.application.command.RegisterCommand;
import com.chamrong.iecommerce.auth.application.command.RegisterUserHandler;
import com.chamrong.iecommerce.auth.application.dto.AuthResponse;
import com.chamrong.iecommerce.auth.application.exception.DuplicateUserException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public authentication endpoints — registration and login. No JWT required. */
@Tag(
    name = "Authentication",
    description = "Public endpoints for user registration and login. No token required.")
@SecurityRequirements
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final RegisterUserHandler registerUserHandler;
  private final LoginUserHandler loginUserHandler;

  public AuthController(
      RegisterUserHandler registerUserHandler, LoginUserHandler loginUserHandler) {
    this.registerUserHandler = registerUserHandler;
    this.loginUserHandler = loginUserHandler;
  }

  /**
   * Register a new user account.
   *
   * <p>POST /api/v1/auth/register
   *
   * @return 201 Created + {@link AuthResponse} with JWT
   */
  @Operation(
      summary = "Register a new user",
      description =
          "Creates a new user account and returns a JWT token. No authentication required.")
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
  public ResponseEntity<?> register(@RequestBody RegisterCommand cmd) {
    try {
      var response = registerUserHandler.handle(cmd);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (DuplicateUserException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
  }

  /**
   * Authenticate an existing user and return a JWT.
   *
   * <p>POST /api/v1/auth/login
   *
   * @return 200 OK + {@link AuthResponse} with JWT
   */
  @Operation(
      summary = "Login",
      description = "Authenticates a user with username/password and returns a JWT token.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Login successful",
        content = @Content(schema = @Schema(implementation = AuthResponse.class))),
    @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content)
  })
  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginCommand cmd) {
    try {
      var response = loginUserHandler.handle(cmd);
      return ResponseEntity.ok(response);
    } catch (BadCredentialsException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }
  }
}
