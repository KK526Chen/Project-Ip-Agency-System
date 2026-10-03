package com.ipagency;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipagency.common.*;
import com.ipagency.entity.*;
import com.ipagency.service.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Uses the initialized MySQL schema. Fixtures and business writes roll back; seed data is not required. */
@SpringBootTest(properties = {"app.reminders.enabled=false", "app.file.upload-dir=./target/test-uploads",
    "debug=false", "logging.level.root=WARN", "logging.level.org.springframework=WARN", "logging.level.com.ipagency=WARN"})
@AutoConfigureMockMvc(print = org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint.NONE)
@Transactional
@EnabledIfSystemProperty(named = "v2.integration", matches = "true")
class ModuleAIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired V2Store db;
    @Autowired PublicContentService content;
    @Autowired ProfileService profiles;
    @Autowired JwtUtil jwt;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager transactions;
    private SysUser admin;
    private final String marker = "A_TEST_" + UUID.randomUUID();
    private final List<Long> fixtureUserIds = new ArrayList<>();

    @BeforeEach void setup() { admin = user("ADMIN"); as(admin); }
    @AfterEach void clear() { CurrentUserContext.clear(); }
    @AfterTransaction void fixtureWritesHaveRolledBack() {
        for (Long id : fixtureUserIds) {
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_user WHERE id=?", Long.class, id)).isZero();
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM operation_log WHERE user_id=?", Long.class, id)).isZero();
        }
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM service_product WHERE service_name=?", Long.class, marker)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM success_case WHERE case_name=?", Long.class, marker)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM announcement WHERE title=?", Long.class, marker)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM client_profile WHERE client_name=?", Long.class, marker)).isZero();
    }
    private SysUser user(String role) {
        SysUser u = new SysUser(); u.setUsername("a_" + UUID.randomUUID());
        u.setPasswordHash("integration-fixture-no-login"); u.setRealName("Module A fixture");
        u.setRole(role); u.setStatus(1); db.insert(u); fixtureUserIds.add(u.getId()); return u;
    }
    private void as(SysUser u) { CurrentUserContext.set(new AuthenticatedUser(u.getId(), u.getRole())); }
    private String token(SysUser u) { return "Bearer " + jwt.generate(u.getId(), u.getUsername(), u.getRole()); }
    private Map<String, Object> service(String category, int status) {
        return Map.of("serviceNo", "A_" + UUID.randomUUID(), "serviceName", marker,
            "serviceType", category, "status", status, "officialFee", 0, "agencyFee", 12.34);
    }
    private Map<String, Object> announcement() {
        Map<String, Object> body = new HashMap<>();
        body.put("announcementNo", "A_" + UUID.randomUUID()); body.put("title", marker);
        body.put("announcementType", "POLICY"); body.put("targetScope", "ALL");
        body.put("content", "Integration announcement"); body.put("status", 1);
        body.put("publishTime", LocalDateTime.now().minusHours(1).toString()); return body;
    }
    private long audits(String business, Long id, String operation) {
        return db.count(OperationLog.class, new QueryWrapper<OperationLog>().eq("business_type", business)
            .eq("business_id", id).eq("operation", operation));
    }

    @Test void servicesFilterPaginatePersistAndLogicallyDelete() throws Exception {
        ServiceProduct first = content.save(ServiceProduct.class, null, service("PATENT_APPLICATION", 1));
        ServiceProduct second = content.save(ServiceProduct.class, null, service("PATENT_APPLICATION", 1));
        ServiceProduct hidden = content.save(ServiceProduct.class, null, service("TRADEMARK_REGISTRATION", 0));
        assertThat(first.getCreateTime()).isNotNull();
        assertThat(first.getOfficialFee()).isEqualByComparingTo("0.00");
        assertThat(audits("ServiceProduct", first.getId(), "SAVE_CONTENT")).isEqualTo(1);
        mvc.perform(get("/api/public/services").param("keyword", marker).param("serviceType", "PATENT_APPLICATION")
                .param("pageSize", "1")).andExpect(status().isOk()).andExpect(jsonPath("$.data.total").value(2))
            .andExpect(jsonPath("$.data.list[0].id").value(second.getId()));
        mvc.perform(get("/api/public/services").param("keyword", marker).param("serviceType", ""))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.total").value(2));
        mvc.perform(get("/api/public/services/" + hidden.getId())).andExpect(status().isNotFound());
        as(admin); Map<String, Object> update = new HashMap<>(); update.put("agencyFee", null);
        content.save(ServiceProduct.class, first.getId(), update);
        assertThat(db.get(ServiceProduct.class, first.getId()).getAgencyFee()).isNull();
        content.delete(ServiceProduct.class, first.getId());
        assertThat(jdbc.queryForObject("SELECT is_deleted FROM service_product WHERE id=?", Integer.class, first.getId())).isEqualTo(1);
        assertThat(audits("ServiceProduct", first.getId(), "DELETE_CONTENT")).isEqualTo(1);
        mvc.perform(get("/api/public/services/" + first.getId())).andExpect(status().isNotFound());
    }

    @Test void successCasesRespectPublicationAndAdminPermissions() throws Exception {
        Map<String, Object> body = new HashMap<>(Map.of("caseNo", "A_" + UUID.randomUUID(), "caseName", marker,
            "serviceType", "PATENT_ANALYSIS", "publishStatus", 0, "technicalField", "Test technology"));
        SuccessCase c = content.save(SuccessCase.class, null, body);
        mvc.perform(get("/api/public/success-cases/" + c.getId())).andExpect(status().isNotFound());
        mvc.perform(get("/api/admin/success-cases/" + c.getId()).header("Authorization", token(admin)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.technicalField").value("Test technology"));
        as(admin); content.save(SuccessCase.class, c.getId(), Map.of("publishStatus", 1));
        mvc.perform(get("/api/public/success-cases").param("keyword", marker).param("serviceType", "PATENT_ANALYSIS"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.total").value(1));
        SysUser client = user("CLIENT");
        mvc.perform(post("/api/admin/success-cases").header("Authorization", token(client))
                .contentType("application/json").content(json.writeValueAsString(body))).andExpect(status().isForbidden());
        assertThat(audits("SuccessCase", c.getId(), "SAVE_CONTENT")).isEqualTo(2);
    }

    @Test void announcementsRespectAudiencePublicationAndEffectiveDates() throws Exception {
        Announcement visible = content.save(Announcement.class, null, announcement());
        for (Map<String, Object> changes : List.of(Map.<String, Object>of("targetScope", "CLIENT"),
                Map.<String, Object>of("status", 0), Map.<String, Object>of("publishTime", LocalDateTime.now().plusDays(1).toString()),
                Map.<String, Object>of("startDate", LocalDate.now().plusDays(1).toString()),
                Map.<String, Object>of("endDate", LocalDate.now().minusDays(1).toString()))) {
            as(admin); Map<String, Object> body = announcement(); body.putAll(changes);
            Announcement hidden = content.save(Announcement.class, null, body);
            mvc.perform(get("/api/public/announcements/" + hidden.getId())).andExpect(status().isNotFound());
        }
        mvc.perform(get("/api/public/announcements").param("keyword", marker).param("announcementType", "POLICY"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.total").value(1))
            .andExpect(jsonPath("$.data.list[0].id").value(visible.getId()));
        assertThat(visible.getPublisherUserId()).isEqualTo(admin.getId());
        as(admin); Map<String, Object> invalid = announcement();
        invalid.put("startDate", "2026-02-02"); invalid.put("endDate", "2026-02-01");
        assertThatThrownBy(() -> content.save(Announcement.class, null, invalid)).isInstanceOf(BusinessException.class);
    }

    @Test void firstProfileReadDoesNotInsertAndSavePersistsAndClearsOptionalFields() throws Exception {
        SysUser client = user("CLIENT");
        mvc.perform(get("/api/client/profile").header("Authorization", token(client)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.id").isEmpty())
            .andExpect(jsonPath("$.data.clientType").value("INDIVIDUAL"));
        assertThat(db.count(ClientProfile.class, new QueryWrapper<ClientProfile>().eq("user_id", client.getId()))).isZero();
        mvc.perform(get("/api/client/contacts").header("Authorization", token(client))).andExpect(status().isBadRequest());
        mvc.perform(put("/api/client/profile").header("Authorization", token(client)).contentType("application/json")
                .content(json.writeValueAsString(Map.of("clientName", marker, "clientType", "COMPANY", "primaryContactEmail", "client@example.com"))))
            .andExpect(status().isOk());
        ClientProfile saved = db.one(ClientProfile.class, new QueryWrapper<ClientProfile>().eq("user_id", client.getId()));
        assertThat(saved.getId()).isNotNull(); assertThat(saved.getCreateTime()).isNotNull();
        as(client); Map<String, Object> clear = new HashMap<>(); clear.put("primaryContactEmail", null);
        assertThat(profiles.saveClient(clear).getId()).isEqualTo(saved.getId());
        assertThat(db.get(ClientProfile.class, saved.getId()).getPrimaryContactEmail()).isNull();
        assertThat(db.count(ClientProfile.class, new QueryWrapper<ClientProfile>().eq("user_id", client.getId()))).isEqualTo(1);
        assertThat(audits("CLIENT", saved.getId(), "UPDATE_PROFILE")).isEqualTo(2);
    }

    @Test void contactsAreIsolatedByClientAndDeleteKeepsPhysicalRow() throws Exception {
        SysUser first = user("CLIENT"), second = user("CLIENT");
        as(first); ClientProfile owner = profiles.saveClient(Map.of("clientType", "INDIVIDUAL", "clientName", marker));
        ClientContact contact = profiles.contact(null, Map.of("name", "Contact", "email", "contact@example.com", "permissionScope", "FEE_ONLY"));
        assertThat(contact.getClientId()).isEqualTo(owner.getId());
        as(second); profiles.saveClient(Map.of("clientType", "COMPANY", "clientName", marker));
        mvc.perform(get("/api/client/contacts").header("Authorization", token(second)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.total").value(0));
        mvc.perform(put("/api/client/contacts/" + contact.getId()).header("Authorization", token(second))
                .contentType("application/json").content("{\"name\":\"Attack\"}")).andExpect(status().isForbidden());
        mvc.perform(delete("/api/client/contacts/" + contact.getId()).header("Authorization", token(second))).andExpect(status().isForbidden());
        assertThat(db.get(ClientContact.class, contact.getId()).getName()).isEqualTo("Contact");
        mvc.perform(put("/api/client/contacts/" + contact.getId()).header("Authorization", token(first))
                .contentType("application/json").content("{\"name\":\"Updated\",\"email\":null}"))
            .andExpect(status().isOk());
        assertThat(db.get(ClientContact.class, contact.getId()).getEmail()).isNull();
        mvc.perform(delete("/api/client/contacts/" + contact.getId()).header("Authorization", token(first))).andExpect(status().isOk());
        assertThat(jdbc.queryForObject("SELECT is_deleted FROM client_contact WHERE id=?", Integer.class, contact.getId())).isEqualTo(1);
        mvc.perform(get("/api/client/contacts").header("Authorization", token(first)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.total").value(0));
        assertThat(audits("CLIENT_CONTACT", contact.getId(), "SAVE_CONTACT")).isEqualTo(2);
        assertThat(audits("CLIENT_CONTACT", contact.getId(), "DELETE_CONTACT")).isEqualTo(1);
    }

    @Test void invalidInputAndDatabaseConstraintsDoNotCreateRows() throws Exception {
        SysUser client = user("CLIENT");
        for (Map<String, Object> body : List.of(Map.<String, Object>of("clientType", "COMPANY", "clientName", marker, "primaryContactEmail", "bad-email"),
                Map.<String, Object>of("clientType", "COMPANY", "clientName", marker, "userId", admin.getId()))) {
            mvc.perform(put("/api/client/profile").header("Authorization", token(client)).contentType("application/json")
                    .content(json.writeValueAsString(body))).andExpect(status().isBadRequest());
        }
        assertThat(db.count(ClientProfile.class, new QueryWrapper<ClientProfile>().eq("user_id", client.getId()))).isZero();
        as(admin); ServiceProduct product = content.save(ServiceProduct.class, null, service("PATENT_APPLICATION", 1));
        assertThatThrownBy(() -> jdbc.update("INSERT INTO service_product(service_no,service_name,service_type) VALUES (?,?,?)",
            product.getServiceNo(), marker, "PATENT_APPLICATION")).isInstanceOf(org.springframework.dao.DuplicateKeyException.class);
        assertThatThrownBy(() -> jdbc.update("INSERT INTO client_contact(client_id,name) VALUES (?,?)", Long.MAX_VALUE, marker))
            .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        assertThat(db.count(ServiceProduct.class, new QueryWrapper<ServiceProduct>().eq("service_no", product.getServiceNo()))).isEqualTo(1);
    }

    @Test void auditFailureRollsBackBusinessInsertInRealTransaction() {
        // A nonexistent actor permits the content insert, then fails when audit loads the actor.
        CurrentUserContext.set(new AuthenticatedUser(Long.MAX_VALUE, "ADMIN"));
        TransactionTemplate isolated = new TransactionTemplate(transactions);
        isolated.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        Map<String, Object> body = service("PATENT_APPLICATION", 1);
        assertThatThrownBy(() -> isolated.execute(status -> content.save(ServiceProduct.class, null, body)))
            .isInstanceOf(BusinessException.class).hasMessage("数据不存在");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM service_product WHERE service_no=?", Long.class, body.get("serviceNo"))).isZero();
    }
}
