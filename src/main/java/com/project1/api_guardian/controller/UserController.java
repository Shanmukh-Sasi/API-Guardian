package com.project1.api_guardian.controller;

import com.project1.api_guardian.dto.ApiResponse;
import com.project1.api_guardian.dto.CreateUserRequest;
import com.project1.api_guardian.dto.UserResponse;
import com.project1.api_guardian.service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserManagementService userManagementService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> create(
            @Valid @RequestBody CreateUserRequest request
    ) {
        UserResponse user = userManagementService.create(request);
        return ResponseEntity.created(URI.create("/api/users/" + user.getId()))
                .body(ApiResponse.success("User created", user));
    }
}
