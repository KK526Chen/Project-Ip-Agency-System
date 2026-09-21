package com.ipagency.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipagency.entity.*;
import com.ipagency.integration.MockCpcAdapter;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;

class V2ValidationTest {
    @Test void ownershipAndWorkflowFieldsAreNotMassAssignable() {
        Input input = new Input(new ObjectMapper());
        for (String key : new String[]{"clientId", "principalAgentId", "status", "isDeleted"})
            assertThatThrownBy(() -> input.apply(Map.of(key, "1"), new CaseInfo(), "caseName caseType")).isInstanceOf(BusinessException.class);
    }
    @Test void hashesAndStoragePathsAreNeverSerialized() throws Exception {
        ObjectMapper json = new ObjectMapper(); SysUser user = new SysUser(); user.setPasswordHash("secret");
        CaseDocument document = new CaseDocument(); document.setFilePath("private/path");
        assertThat(json.writeValueAsString(user)).doesNotContain("passwordHash", "secret");
        assertThat(json.writeValueAsString(document)).doesNotContain("filePath", "private/path");
    }
    @Test void schemaEnumsAndLengthsAreValidated() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            CaseInfo c = new CaseInfo(); c.setClientId(1L); c.setCaseName("Case"); c.setCaseType("INVALID");
            assertThat(factory.getValidator().validate(c)).isNotEmpty();
            c.setCaseType("INVENTION_PATENT"); assertThat(factory.getValidator().validate(c)).isEmpty();
        }
    }
    @Test void mocksHaveExplicitSuccessAndFailureResults() {
        var adapter = new MockCpcAdapter();
        assertThat(adapter.pushCase(1L, "{}").success()).isTrue();
        assertThat(adapter.pullDocument(1L, "{\"simulateFailure\":true}").success()).isFalse();
        assertThat(adapter.pullDocument(1L, "{\"simulateFailure\": true}").success()).isFalse();
    }
}
