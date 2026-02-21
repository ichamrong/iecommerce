package com.chamrong.iecommerce.auth.api;

import com.chamrong.iecommerce.auth.application.command.LoginCommand;
import com.chamrong.iecommerce.auth.application.command.LoginUserHandler;
import com.chamrong.iecommerce.auth.application.command.RegisterCommand;
import com.chamrong.iecommerce.auth.application.command.RegisterUserHandler;
import com.chamrong.iecommerce.auth.application.dto.AuthResponse;
import com.chamrong.iecommerce.auth.application.exception.DuplicateUserException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public authentication endpoints — registration and login. No JWT required. */
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
