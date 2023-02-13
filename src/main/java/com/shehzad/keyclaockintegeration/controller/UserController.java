package com.shehzad.keyclaockintegeration.controller;

import com.shehzad.keyclaockintegeration.restModels.ApiResponse;
import com.shehzad.keyclaockintegeration.restModels.directMappings.AppUserInfoDTO;
import com.shehzad.keyclaockintegeration.restModels.user.UpdateInputDTO;
import com.shehzad.keyclaockintegeration.services.AppUserService;
import com.shehzad.keyclaockintegeration.services.KeycloakAdminClientService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Slf4j
@AllArgsConstructor
public class UserController {

    private final KeycloakAdminClientService kcAdminClient;
    private final AppUserService appUserService;

    @DeleteMapping("/delete/{userid}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<Long>> deleteUser(@PathVariable("userid") Long userId) {
        ApiResponse<Long> apiResponse = kcAdminClient.deleteKeycloakUser(userId);
        return ResponseEntity.status(HttpStatusCode.valueOf(apiResponse.getResponseCode())).body(apiResponse);
    }

    @PutMapping("/update/{userid}")
    @PreAuthorize("hasRole('admin') or hasRole('user')")
    public ResponseEntity<ApiResponse<AppUserInfoDTO>> updateUser(@PathVariable("userid") Long userId, @RequestBody UpdateInputDTO updateInputDTO) {
        ApiResponse<AppUserInfoDTO> apiResponse = kcAdminClient.updateKeycloakUser(userId, updateInputDTO);
        return ResponseEntity.status(HttpStatusCode.valueOf(apiResponse.getResponseCode())).body(apiResponse);
    }

    @GetMapping("/getAll")
    @PreAuthorize("hasRole('admin') or hasRole('user')")
    public ResponseEntity<ApiResponse<List<AppUserInfoDTO>>> getAllUsers(@RequestParam(value = "page", defaultValue = "1") int page, @RequestParam(value = "size", defaultValue = "20") int size, @RequestParam(value = "sort_by", defaultValue = "DESC") String sortBy, @RequestParam("sort_with") String sortWith) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(HttpStatus.OK.value(), appUserService.getAllUserInfo(page, size, sortBy, sortWith), "List of users"));
    }
}
