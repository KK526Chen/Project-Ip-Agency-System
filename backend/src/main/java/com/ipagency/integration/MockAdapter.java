package com.ipagency.integration;

/** Deliberately no HTTP calls or fabricated local document files. */
public abstract class MockAdapter implements ExternalSystemAdapter {
    private SyncResult result(Long caseId, String payload, String operation) {
        try {
            if (payload != null && new com.fasterxml.jackson.databind.ObjectMapper().readTree(payload).path("simulateFailure").asBoolean(false))
                return new SyncResult(false, null, null, "Mock failure requested");
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            return new SyncResult(false, null, null, "Invalid request JSON");
        }
        return new SyncResult(true, "MOCK-" + systemCode() + "-" + caseId, "MOCK-APP-" + caseId, "Mock " + operation + " completed");
    }
    public SyncResult pushCase(Long id, String payload) { return result(id, payload, "push"); }
    public SyncResult pullDocument(Long id, String payload) { return result(id, payload, "pull (metadata only)"); }
    public SyncResult queryStatus(Long id, String payload) { return result(id, payload, "status query"); }
}
