package com.ipagency.common;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class JwtUtilTest {
    @Test
    void roundTripsIdentity() {
        JwtUtil jwtUtil = new JwtUtil("test-secret-with-at-least-thirty-two-bytes", 60);
        AuthenticatedUser user = jwtUtil.parse(jwtUtil.generate(42L, "AGENT"));
        assertThat(user.userId()).isEqualTo(42L);
        assertThat(user.role()).isEqualTo("AGENT");
    }
}
