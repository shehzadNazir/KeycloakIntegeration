package com.shehzad.keyclaockintegeration.controller;

import com.shehzad.keyclaockintegeration.restModels.ApiResponse;
import com.shehzad.keyclaockintegeration.restModels.user.LoginInputDTO;
import com.shehzad.keyclaockintegeration.restModels.user.SignupInputDTO;
import com.shehzad.keyclaockintegeration.restModels.user.SignupOutputDTO;
import com.shehzad.keyclaockintegeration.services.KeycloakAdminClientService;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
@Slf4j
@AllArgsConstructor
public class AuthController {
    private final KeycloakAdminClientService kcAdminClient;

    @PostMapping(value = "/signup")
    public ResponseEntity<ApiResponse<SignupOutputDTO>> createUser(@RequestBody SignupInputDTO user) {
        ApiResponse<SignupOutputDTO> createdResponse = kcAdminClient.createKeycloakUser(user);
        return ResponseEntity.status(createdResponse.getResponseCode()).body(createdResponse);
    }

    @GetMapping("/refreshToken")
    public ResponseEntity<?> getRefreshToken(@NotNull @RequestParam("token") String token) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(200, kcAdminClient.refreshToken(token), "Refresh token created"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(HttpStatus.UNAUTHORIZED.value(), e.getMessage(), "wrong credentials"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@NotNull @RequestBody LoginInputDTO loginRequest) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(200, kcAdminClient.login(loginRequest.getEmail(), loginRequest.getPassword()), "Logged in successfully"));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(HttpStatus.FORBIDDEN.value(), ex.getMessage(), "wrong credentials"));
        }
    }

    @GetMapping("/login")
    public ResponseEntity<?> login(@RequestParam("subject_token") String subjectToken) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(200, kcAdminClient.loginUserByGoogle(subjectToken), "Logged in successfully"));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(HttpStatus.FORBIDDEN.value(), ex.getMessage(), "wrong credentials"));
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(200, kcAdminClient.logout(), "Logged in successfully"));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(HttpStatus.FORBIDDEN.value(), ex.getMessage(), "wrong credentials"));
        }
    }
}
