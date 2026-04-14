package com.hyeyuns2.kidtale.auth.controller;

import com.hyeyuns2.kidtale.auth.dto.AuthResponse;
import com.hyeyuns2.kidtale.auth.dto.LoginRequest;
import com.hyeyuns2.kidtale.auth.dto.SignupRequest;
import com.hyeyuns2.kidtale.auth.dto.UserResponse;
import com.hyeyuns2.kidtale.auth.service.AuthService;
import com.hyeyuns2.kidtale.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponse>> signup(@Valid @RequestBody SignupRequest request) {
        log.debug("[AuthController] POST /api/auth/signup. username={}", request.username());
        AuthResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("회원가입이 완료되었습니다.", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.debug("[AuthController] POST /api/auth/login. username={}", request.username());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("로그인되었습니다.", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me(@AuthenticationPrincipal UserDetails userDetails) {
        UserResponse response = authService.getMe(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
