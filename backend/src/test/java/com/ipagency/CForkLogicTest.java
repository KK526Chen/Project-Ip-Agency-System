package com.ipagency;

import com.ipagency.common.*;
import com.ipagency.entity.AgentProfile;
import com.ipagency.service.WorkItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class CForkLogicTest {
    @Test void dagCycleIsRejected() {
        Map<Long, List<Long>> graph = new HashMap<>();
        graph.put(2L, List.of(1L));
        graph.put(1L, new ArrayList<>(List.of(2L)));
        assertThat(WorkItemService.createsCycle(graph)).isTrue();
        graph.put(1L, new ArrayList<>(List.of(3L)));
        graph.put(3L, List.of());
        assertThat(WorkItemService.createsCycle(graph)).isFalse();
    }
    @Test void agentProfileRejectsIdentityFields() {
        Input input = new Input(new ObjectMapper());
        AgentProfile p = new AgentProfile(); p.setEmployeeNo("A1");
        assertThatThrownBy(() -> input.apply(Map.of("employeeNo", "HACK"), p, "licenseNo department professionalField practiceYears education ipcScope profile"))
            .isInstanceOf(BusinessException.class);
        input.apply(Map.of("professionalField", "AI"), p, "licenseNo department professionalField practiceYears education ipcScope profile");
        assertThat(p.getProfessionalField()).isEqualTo("AI");
        assertThat(p.getEmployeeNo()).isEqualTo("A1");
    }
}
