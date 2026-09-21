package com.ipagency;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.*;
import com.ipagency.common.*;
import com.ipagency.entity.*;
import com.ipagency.service.*;
import com.ipagency.service.impl.CaseAccessServiceImpl;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Opt-in tests against the already initialized V2 database. No schema/seed execution. Every write rolls back. */
@SpringBootTest(properties = {"app.reminders.enabled=false", "app.file.upload-dir=./target/test-uploads",
    "debug=false", "logging.level.root=WARN", "logging.level.org.springframework=WARN", "logging.level.com.ipagency=WARN"})
@AutoConfigureMockMvc(print = org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint.NONE)
@Transactional
@EnabledIfSystemProperty(named = "v2.integration", matches = "true")
class V2IntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired V2Store db;
    @Autowired CaseWorkflowService cases;
    @Autowired CaseAccessServiceImpl access;
    @Autowired BillingService bills;
    @Autowired DocumentStorageService files;
    @Autowired DocumentService documents;
    @Autowired DeadlineService deadlines;
    @Autowired ExternalSyncService sync;
    @Autowired NotificationService notifications;
    @Autowired DashboardService dashboard;
    @Autowired ProfileService profiles;
    @Autowired BusinessEvents events;

    @AfterEach void clear() { CurrentUserContext.clear(); }
    private SysUser user(String username) { return db.one(SysUser.class, new QueryWrapper<SysUser>().eq("username", username)); }
    private void as(String username) {
        SysUser u = user(username); assertThat(u).isNotNull(); CurrentUserContext.set(new AuthenticatedUser(u.getId(), u.getRole()));
    }
    private String login(String username) throws Exception {
        String response = mvc.perform(post("/api/auth/login").contentType("application/json")
                .content(json.writeValueAsString(Map.of("username", username, "password", "123456"))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.user.passwordHash").doesNotExist()).andReturn().getResponse().getContentAsString();
        return json.readTree(response).path("data").path("token").asText();
    }

    @Test void seededAccountsPublicRoutesAndObjectPermissions() throws Exception {
        String admin = login("admin"), agent = login("agent_zhang"), client = login("client_huawei");
        assertThat(admin).isNotBlank(); assertThat(agent).isNotBlank(); assertThat(client).isNotBlank();
        for (String route : List.of("services", "success-cases", "announcements"))
            mvc.perform(get("/api/public/" + route)).andExpect(status().isOk()).andExpect(jsonPath("$.data.list").isArray());
        mvc.perform(get("/api/cases")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + client)).andExpect(status().isForbidden());
        as("client_huawei"); Long clientId = access.client().getId();
        var own = db.one(CaseInfo.class, new QueryWrapper<CaseInfo>().eq("client_id", clientId).last("LIMIT 1"));
        var other = db.one(CaseInfo.class, new QueryWrapper<CaseInfo>().ne("client_id", clientId).last("LIMIT 1"));
        mvc.perform(get("/api/client/cases/" + own.getId()).header("Authorization", "Bearer " + client)).andExpect(status().isOk());
        mvc.perform(get("/api/client/cases/" + other.getId()).header("Authorization", "Bearer " + client)).andExpect(status().isForbidden());
        as("agent_zhang"); Long agentId = access.agent().getId();
        var unassigned = db.one(CaseInfo.class, new QueryWrapper<CaseInfo>().and(q -> q.isNull("principal_agent_id").or().ne("principal_agent_id", agentId))
            .notInSql("id", "SELECT case_id FROM case_assignment WHERE is_current=1 AND is_deleted=0 AND agent_id=" + agentId).last("LIMIT 1"));
        mvc.perform(get("/api/agent/cases/" + unassigned.getId()).header("Authorization", "Bearer " + agent)).andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/cases/" + other.getId()).header("Authorization", "Bearer " + admin)).andExpect(status().isOk());
        mvc.perform(get("/api/notifications/unread-count").header("Authorization", "Bearer " + client)).andExpect(status().isOk()).andExpect(jsonPath("$.data").isNumber());
        mvc.perform(get("/api/cases?pageSize=101").header("Authorization", "Bearer " + admin)).andExpect(status().isBadRequest());
        for (String route : List.of("/api/admin/users", "/api/admin/agents", "/api/admin/clients", "/api/admin/operation-logs", "/api/admin/dashboard", "/api/admin/statistics", "/api/external-sync/tasks"))
            mvc.perform(get(route).header("Authorization", "Bearer " + admin)).andExpect(status().isOk());
        for (String route : List.of("/api/client/profile", "/api/client/contacts", "/api/client/bills", "/api/client/invoices", "/api/client/dashboard"))
            mvc.perform(get(route).header("Authorization", "Bearer " + client)).andExpect(status().isOk());
        String upload = mvc.perform(multipart("/api/documents/upload")
            .file(new MockMultipartFile("file", "http-test.txt", "text/plain", "http upload".getBytes()))
            .param("caseId", own.getId().toString()).param("documentType", "SUPPLEMENT").header("Authorization", "Bearer " + client))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.fileSize").value(11)).andReturn().getResponse().getContentAsString();
        long documentId = json.readTree(upload).path("data").path("id").asLong();
        String otherClient = login("client_bio");
        mvc.perform(get("/api/documents/" + documentId + "/download").header("Authorization", "Bearer " + otherClient)).andExpect(status().isForbidden());
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andExpect(jsonPath("$.paths").exists());
        mvc.perform(post("/api/auth/password").header("Authorization", "Bearer " + client).contentType("application/json")
            .content(json.writeValueAsString(Map.of("oldPassword", "123456", "newPassword", "V2-test-password"))))
            .andExpect(status().isOk());
        mvc.perform(post("/api/auth/login").contentType("application/json")
            .content(json.writeValueAsString(Map.of("username", "client_huawei", "password", "V2-test-password"))))
            .andExpect(status().isOk());
        assertThat(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
            .matches("V2-test-password", user("client_huawei").getPasswordHash())).isTrue();
    }

    @Test void completeBusinessWorkflowPersistsRowsAndHistory() throws Exception {
        as("client_huawei");
        var c = cases.save(null, Map.of("caseName", "V2 transactional integration", "caseType", "INVENTION_PATENT",
            "parties", List.of(Map.of("partyType", "APPLICANT", "name", "Test applicant")),
            "priorities", List.of(Map.of("country", "CN", "priorityNo", "TEST-" + UUID.randomUUID(), "priorityDate", "2026-01-01"))));
        assertThat(c.getClientId()).isEqualTo(access.client().getId());
        assertThat(c.getCreateTime()).isNotNull();
        assertThat(cases.submit(c.getId()).getStatus()).isEqualTo("PENDING_REVIEW");
        as("admin"); cases.review(c.getId(), Map.of("reviewResult", "APPROVED", "reviewComment", "Test"));
        assertThat(db.get(CaseInfo.class, c.getId()).getCaseNo()).isNotBlank();
        AgentProfile first = db.one(AgentProfile.class, new QueryWrapper<AgentProfile>().eq("user_id", user("agent_zhang").getId()));
        AgentProfile second = db.one(AgentProfile.class, new QueryWrapper<AgentProfile>().eq("user_id", user("agent_li").getId()));
        CaseAssignment initial = cases.assign(c.getId(), first.getId(), "Initial", false);
        cases.assign(c.getId(), second.getId(), "Change", true);
        assertThat(db.get(CaseAssignment.class, initial.getId()).getIsCurrent()).isZero();
        assertThat(db.get(CaseAssignment.class, initial.getId()).getEndTime()).isNotNull();
        as("agent_zhang"); assertThat(access.canViewCase(CurrentUserContext.require(), c.getId())).isFalse();
        as("agent_li");
        assertThat(cases.detail(c.getId()).getStatus()).isEqualTo("PROCESSING");
        CaseStage stage = cases.stage(c.getId(), null, Map.of("stageType", "PRELIMINARY_EXAM", "stageName", "Initial examination"));
        MockMultipartFile file = new MockMultipartFile("file", "application.txt", "text/plain", "test application".getBytes());
        CaseDocument document = files.upload(file, c.getId(), "APPLICATION", stage.getId(), "integration");
        assertThat(db.get(CaseDocument.class, document.getId()).getFileSize()).isEqualTo(file.getSize());
        assertThat(files.download(document.getId()).resource().getContentAsByteArray()).isEqualTo(file.getBytes());
        as("admin"); documents.review(Map.of("targetType", "DOCUMENT", "targetId", document.getId(), "reviewResult", "APPROVED"));
        CaseDocument official = files.upload(new MockMultipartFile("file", "official.txt", "text/plain", "official".getBytes()), c.getId(), "OFFICIAL", null, null);
        documents.ocr(official.getId(), Map.of("officialDeadline", LocalDate.now().plusDays(5).toString(), "ocrText", "Manually confirmed"));
        DeadlineTask task = db.one(DeadlineTask.class, new QueryWrapper<DeadlineTask>().eq("document_id", official.getId()));
        assertThat(task).isNotNull(); assertThat(task.getAgentId()).isEqualTo(second.getId());
        CaseAssignment collaborator = new CaseAssignment(); collaborator.setCaseId(c.getId()); collaborator.setAgentId(first.getId());
        collaborator.setAssignmentRole("COLLABORATOR"); collaborator.setAssignedByUserId(user("admin").getId());
        collaborator.setIsCurrent(1); db.insert(collaborator);
        as("agent_zhang");
        assertThatThrownBy(() -> deadlines.save(task.getId(), Map.of("agentId", first.getId()))).isInstanceOf(BusinessException.class);
        assertThat(db.get(DeadlineTask.class, task.getId()).getAgentId()).isEqualTo(second.getId());
        as("admin");
        var reminder = new DeadlineReminderService(db, events);
        reminder.remind();
        long reminders = db.count(Notification.class, new QueryWrapper<Notification>().eq("business_type", "DEADLINE_REMINDER").eq("business_id", task.getId()));
        assertThat(reminders).isEqualTo(2);
        reminder.remind();
        assertThat(db.count(Notification.class, new QueryWrapper<Notification>().eq("business_type", "DEADLINE_REMINDER").eq("business_id", task.getId()))).isEqualTo(reminders);
        as("agent_li"); assertThat(deadlines.complete(task.getId()).getCompletedTime()).isNotNull();
        as("admin");
        FeeBill bill = bills.save(null, Map.of("caseId", c.getId(), "feeType", "AGENCY", "feeItem", "Test fee", "amount", 100, "discountAmount", 10));
        as("client_huawei"); bills.confirm(bill.getId()); PaymentRecord payment = bills.pay(bill.getId());
        assertThat(payment.getAmount()).isEqualByComparingTo("90.00"); assertThat(payment.getPaymentMethod()).isEqualTo("SIMULATED");
        assertThat(db.count(PaymentRecord.class, new QueryWrapper<PaymentRecord>().eq("bill_id", bill.getId()))).isEqualTo(1);
        as("admin"); InvoiceRecord invoice = bills.invoice(bill.getId(), Map.of()); assertThat(invoice.getStatus()).isEqualTo("ISSUED");
        for (String code : List.of("CPC", "TRADEMARK", "PATENT_PAYMENT")) {
            ExternalSyncTask pending = sync.create(Map.of("caseId", c.getId(), "systemCode", code, "syncType", "PUSH"));
            assertThat(pending.getStatus()).isEqualTo("PENDING");
            assertThat(sync.execute(pending.getId(), false).getStatus()).isEqualTo("SUCCESS");
        }
        assertThat(db.count(ExternalCaseBinding.class, new QueryWrapper<ExternalCaseBinding>().eq("case_id", c.getId()))).isEqualTo(3);
        assertThat(db.count(OperationLog.class, new QueryWrapper<OperationLog>().eq("business_id", c.getId()).eq("business_type", "CASE"))).isGreaterThan(3);
        as("client_huawei"); assertThat(notifications.unread()).isPositive(); assertThat(dashboard.overview().get("cases")).isNotNull();
        notifications.read(null); assertThat(notifications.unread()).isZero();
    }

    @Test void rejectsDuplicatePaymentAndForeignDocumentsWithoutWrites() {
        as("admin");
        var c = db.one(CaseInfo.class, new QueryWrapper<CaseInfo>().eq("client_id", db.one(ClientProfile.class,
            new QueryWrapper<ClientProfile>().eq("user_id", user("client_huawei").getId())).getId()).last("LIMIT 1"));
        FeeBill b = bills.save(null, Map.of("caseId", c.getId(), "feeType", "AGENCY", "feeItem", "Duplicate guard", "amount", 10));
        as("client_huawei"); bills.confirm(b.getId()); bills.pay(b.getId());
        assertThatThrownBy(() -> bills.pay(b.getId())).isInstanceOf(BusinessException.class);
        assertThat(db.count(PaymentRecord.class, new QueryWrapper<PaymentRecord>().eq("bill_id", b.getId()))).isEqualTo(1);
        as("client_bio"); assertThatThrownBy(() -> bills.owned(b.getId())).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> profiles.contact(null, Map.of("clientId", c.getClientId(), "name", "Attack"))).isInstanceOf(BusinessException.class);
    }

    @Test void syncFailureIsPersistedAndRetryIsBounded() {
        as("admin"); CaseInfo c = db.one(CaseInfo.class, new QueryWrapper<CaseInfo>().last("LIMIT 1"));
        ExternalSyncTask pending = sync.create(Map.of("caseId", c.getId(), "systemCode", "CPC", "syncType", "PULL", "simulateFailure", true));
        ExternalSyncTask failed = sync.execute(pending.getId(), false);
        assertThat(db.get(ExternalSyncTask.class, failed.getId()).getStatus()).isEqualTo("FAILED");
        assertThat(failed.getLastError()).isNotBlank();
        failed.setNextRetryTime(LocalDateTime.now().minusSeconds(1)); db.update(failed);
        assertThat(sync.execute(failed.getId(), true).getRetryCount()).isEqualTo(1);
    }
}
