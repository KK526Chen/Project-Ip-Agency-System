package com.ipagency.integration;

public interface ExternalSystemAdapter {
    String systemCode();
    SyncResult pushCase(Long caseId, String requestPayload);
    SyncResult pullDocument(Long caseId, String requestPayload);
    SyncResult queryStatus(Long caseId, String requestPayload);
    record SyncResult(boolean success, String externalCaseId, String applicationNo, String message) { }
}
