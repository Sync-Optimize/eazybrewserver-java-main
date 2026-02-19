
package com.eazybrew.vend.controller;

import com.eazybrew.vend.apiresponse.ApiResponse;
import com.eazybrew.vend.config.JwtUtils;
import com.eazybrew.vend.dto.request.LoginRequest;
import com.eazybrew.vend.dto.request.ResetPasswordRequest;
import com.eazybrew.vend.dto.request.SignupRequest;
import com.eazybrew.vend.dto.response.JwtResponse;
import com.eazybrew.vend.exceptions.CustomException;
import com.eazybrew.vend.model.Role;
import com.eazybrew.vend.model.User;
import com.eazybrew.vend.model.enums.ERole;
import com.eazybrew.vend.model.enums.UserStatus;
import com.eazybrew.vend.repository.RoleRepository;
import com.eazybrew.vend.repository.UserRepository;
import com.eazybrew.vend.security.UserPrincipal;
import com.eazybrew.vend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.eazybrew.vend.dto.request.ForgotPasswordRequest;
import com.eazybrew.vend.dto.request.ResetForgotPasswordRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {
    private final AuthService authService;

    @Operation(
            summary = "User Login",
            description = "Authenticate a user and generate JWT token"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully authenticated",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = JwtResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Authentication failed",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<JwtResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        JwtResponse response = authService.signIn(loginRequest);

        ApiResponse<JwtResponse> apiResponse = new ApiResponse<>(HttpStatus.OK);
        apiResponse.setMessage("Login successful");
        apiResponse.setData(response);

        return ResponseEntity.ok(apiResponse);
    }

    @Operation(
            summary = "User Registration",
            description = "Register a new user account"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User registered successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Bad request - email already in use or invalid input",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<String>> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {

        String result = authService.signup(signUpRequest);
        ApiResponse<String> response = new ApiResponse<>(HttpStatus.OK);
        response.setMessage("User registered successfully!");
        response.setData(result);

        return new ResponseEntity<>(response, response.getStatus());
    }

    @Operation(
            summary = "Reset initial password",
            description = "Allows a user to reset their initial password after first login",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Password updated successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiResponse.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found", content = @Content)
            }
    )
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        ApiResponse<Void> response = new ApiResponse<>(HttpStatus.OK);
        response.setMessage("Password updated successfully");

        return new ResponseEntity<>(response, response.getStatus());

    }

    @Operation(summary = "Initiate forgot password", description = "Sends password reset link to user email")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reset link sent successfully", content = @Content)
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        ApiResponse<Void> response = new ApiResponse<>(HttpStatus.OK);
        response.setMessage("If the email exists, a reset link has been sent");
        return new ResponseEntity<>(response, response.getStatus());
    }

    @Operation(summary = "Reset forgotten password", description = "Resets password using token from email link")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password reset successfully", content = @Content),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid token or expired", content = @Content)
    })
    @PostMapping("/reset-forgot-password")
    public ResponseEntity<ApiResponse<Void>> resetForgotPassword(@Valid @RequestBody ResetForgotPasswordRequest request) {
        authService.resetForgotPassword(request);
        ApiResponse<Void> response = new ApiResponse<>(HttpStatus.OK);
        response.setMessage("Password has been reset successfully");
        return new ResponseEntity<>(response, response.getStatus());
    }
}  

