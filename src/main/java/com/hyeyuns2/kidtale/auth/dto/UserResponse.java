package com.hyeyuns2.kidtale.auth.dto;

import com.hyeyuns2.kidtale.auth.entity.User;

public record UserResponse(
        Long id,
        String username,
        String name,
        String email,
        String phone
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getPhone()
        );
    }
}
