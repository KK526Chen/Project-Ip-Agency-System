package com.ipagency.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class CurrentUserContextTest {
    @AfterEach
    void cleanUp() {
        CurrentUserContext.clear();
    }

    @Test
    void storesAndClearsCurrentUser() {
        CurrentUserContext.set(new AuthenticatedUser(7L, "ASSISTANT"));
        assertThat(CurrentUserContext.require().userId()).isEqualTo(7L);
        CurrentUserContext.clear();
        assertThatThrownBy(CurrentUserContext::require).isInstanceOf(BusinessException.class);
    }
}
