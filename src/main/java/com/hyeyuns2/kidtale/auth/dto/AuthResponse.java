package com.hyeyuns2.kidtale.auth.dto;

public record AuthResponse(
        String token,
        UserResponse user
) {}
