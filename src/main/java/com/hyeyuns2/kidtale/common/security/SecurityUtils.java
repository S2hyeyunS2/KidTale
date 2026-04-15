package com.hyeyuns2.kidtale.common.security;

import com.hyeyuns2.kidtale.auth.entity.User;
import com.hyeyuns2.kidtale.auth.repository.UserRepository;
import com.hyeyuns2.kidtale.common.exception.ErrorCode;
import com.hyeyuns2.kidtale.common.exception.KidTaleException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    private final UserRepository userRepository;

    public SecurityUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 인증된 사용자의 userId를 반환합니다.
     * 비로그인(userDetails == null)이면 null을 반환합니다.
     */
    public Long resolveUserIdOrNull(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userRepository.findByUsername(userDetails.getUsername())
                .map(User::getId)
                .orElse(null);
    }

    /**
     * 인증된 사용자의 userId를 반환합니다.
     * 비로그인이거나 사용자를 찾을 수 없으면 예외를 발생시킵니다.
     */
    public Long resolveUserIdOrThrow(UserDetails userDetails) {
        if (userDetails == null) {
            throw new KidTaleException(ErrorCode.UNAUTHORIZED);
        }
        return userRepository.findByUsername(userDetails.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new KidTaleException(ErrorCode.USER_NOT_FOUND));
    }
}
