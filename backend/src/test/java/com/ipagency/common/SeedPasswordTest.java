package com.ipagency.common;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class SeedPasswordTest {
    @Test
    void demoPasswordMatchesSeedHash() {
        String seedHash = "$2a$10$XG3SqtDPAk5jvvnAG9wUB.fGP2Fbi7noKJJQzFrTxphNdk/m8skl.";
        assertThat(new BCryptPasswordEncoder().matches("password", seedHash)).isTrue();
    }
}
