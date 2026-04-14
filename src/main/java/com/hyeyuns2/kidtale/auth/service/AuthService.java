package com.hyeyuns2.kidtale.auth.service;

import com.hyeyuns2.kidtale.auth.dto.AuthResponse;
import com.hyeyuns2.kidtale.auth.dto.LoginRequest;
import com.hyeyuns2.kidtale.auth.dto.SignupRequest;
import com.hyeyuns2.kidtale.auth.dto.UserResponse;
import com.hyeyuns2.kidtale.auth.entity.User;
import com.hyeyuns2.kidtale.auth.repository.UserRepository;
import com.hyeyuns2.kidtale.auth.security.JwtUtil;
import com.hyeyuns2.kidtale.common.exception.ErrorCode;
import com.hyeyuns2.kidtale.common.exception.KidTaleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new KidTaleException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new KidTaleException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = User.create(request.username(), encodedPassword, request.name(), request.email(), request.phone());
        userRepository.save(user);

        log.info("[AuthService] 회원가입 완료. username={}", user.getUsername());
        String token = jwtUtil.generateToken(user.getUsername());
        return new AuthResponse(token, UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new KidTaleException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new KidTaleException(ErrorCode.INVALID_CREDENTIALS);
        }

        log.info("[AuthService] 로그인 성공. username={}", user.getUsername());
        String token = jwtUtil.generateToken(user.getUsername());
        return new AuthResponse(token, UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public UserResponse getMe(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new KidTaleException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }
}
