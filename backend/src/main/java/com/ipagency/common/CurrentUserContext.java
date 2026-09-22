package com.ipagency.common;

import org.springframework.http.HttpStatus;

public final class CurrentUserContext {
    private static final ThreadLocal<AuthenticatedUser> CURRENT = new ThreadLocal<>();

    private CurrentUserContext() {
    }

    public static void set(AuthenticatedUser user) {
        CURRENT.set(user);
    }

    public static AuthenticatedUser current() {
        return CURRENT.get();
    }

    public static AuthenticatedUser require() {
        AuthenticatedUser user = CURRENT.get();
        if (user == null) {
            throw new BusinessException("未登录或登录已失效", HttpStatus.UNAUTHORIZED);
        }
        return user;
    }

    public static void clear() {
        CURRENT.remove();
    }
}
