package com.ipagency.common;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class SeedPasswordTest {
    @Test
    void demoPasswordMatchesSeedHash() throws Exception {
        String seed = java.nio.file.Files.readString(java.nio.file.Path.of("../sql/seed.sql"));
        var matcher = java.util.regex.Pattern.compile("\\$2a\\$10\\$[./A-Za-z0-9]{53}").matcher(seed);
        int count = 0;
        while (matcher.find()) {
            assertThat(new BCryptPasswordEncoder().matches("123456", matcher.group())).isTrue();
            count++;
        }
        assertThat(count).isGreaterThanOrEqualTo(3);
    }
}
