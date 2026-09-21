package com.ipagency.common;

public record AuthenticatedUser(Long userId, String role) {
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
