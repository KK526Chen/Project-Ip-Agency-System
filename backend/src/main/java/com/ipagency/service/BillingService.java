package com.ipagency.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ipagency.common.*;
import com.ipagency.entity.*;
import com.ipagency.service.impl.CaseAccessServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.ipagency.service.impl.CaseAccessServiceImpl.role;

@Service
public class BillingService {
    private final V2Store db; private final CaseAccessServiceImpl access; private final Input input; private final BusinessEvents events;
    public BillingService(V2Store db, CaseAccessServiceImpl access, Input input, BusinessEvents events) {
        this.db = db; this.access = access; this.input = input; this.events = events;
    }
    public PageResult<FeeBill> list(long page, long size, String status, Long caseId) {
        role("ADMIN", "CLIENT"); if (caseId != null) access.requireView(caseId);
        var q = new QueryWrapper<FeeBill>().eq(status != null, "status", status).eq(caseId != null, "case_id", caseId).orderByDesc("id");
        if (!CurrentUserContext.require().isAdmin()) q.eq("client_id", access.client().getId());
        return db.page(FeeBill.class, q, page, size);
    }
    public FeeBill owned(Long id) {
        role("CLIENT"); FeeBill b = db.get(FeeBill.class, id);
        if (!Objects.equals(b.getClientId(), access.client().getId())) CaseAccessServiceImpl.denied();
        access.requireView(b.getCaseId()); return b;
    }
    @Transactional
    public FeeBill save(Long id, Map<String, Object> body) {
        role("ADMIN"); FeeBill b = id == null ? new FeeBill() : db.lock(FeeBill.class, id);
        if (id == null) {
            CaseInfo c = db.get(CaseInfo.class, Input.id(body, "caseId")); b.setCaseId(c.getId()); b.setClientId(c.getClientId());
            b.setBillNo("BILL-" + UUID.randomUUID()); b.setStatus("PENDING_CONFIRM"); b.setDiscountAmount(BigDecimal.ZERO); b.setCreatedByUserId(CurrentUserContext.require().userId());
        } else CaseWorkflowService.state(b.getStatus(), "PENDING_CONFIRM");
        var fields = new LinkedHashMap<>(body);
        if (fields.containsKey("caseId") && !Objects.equals(Input.id(fields, "caseId"), b.getCaseId())) throw new BusinessException("不能更换账单案件");
        fields.remove("caseId"); input.apply(fields, b, "feeType feeItem amount discountAmount dueDate remark");
        if (b.getAmount() == null || b.getDiscountAmount() == null || b.getDiscountAmount().compareTo(b.getAmount()) > 0) throw new BusinessException("账单金额或优惠无效");
        b.setPayableAmount(b.getAmount().subtract(b.getDiscountAmount()));
        if (b.getPayableAmount().signum() <= 0) throw new BusinessException("应付金额必须大于零");
        if (id == null) db.insert(b); else db.update(b);
        events.notifyUser(db.get(ClientProfile.class, b.getClientId()).getUserId(), "PAYMENT", "账单待确认: " + b.getFeeItem(), "BILL", b.getId());
        events.audit(id == null ? "CREATE_BILL" : "UPDATE_BILL", "BILL", b.getId()); return b;
    }
    @Transactional
    public FeeBill confirm(Long id) {
        owned(id); FeeBill b = db.lock(FeeBill.class, id); CaseWorkflowService.state(b.getStatus(), "PENDING_CONFIRM");
        b.setStatus("PENDING_PAYMENT"); db.update(b); events.audit("CONFIRM_BILL", "BILL", id); return b;
    }
    @Transactional
    public PaymentRecord pay(Long id) {
        owned(id); FeeBill b = db.lock(FeeBill.class, id); CaseWorkflowService.state(b.getStatus(), "PENDING_PAYMENT");
        PaymentRecord p = new PaymentRecord(); p.setBillId(id); p.setPaymentNo("SIM-" + UUID.randomUUID());
        p.setPaymentMethod("SIMULATED"); p.setAmount(b.getPayableAmount()); p.setPaymentTime(LocalDateTime.now()); p.setStatus("SUCCESS"); db.insert(p);
        b.setStatus("PAID"); db.update(b); events.audit("PAY_BILL", "BILL", id);
        events.notifyUser(CurrentUserContext.require().userId(), "PAYMENT", "模拟支付成功", "BILL", id); return p;
    }
    @Transactional
    public InvoiceRecord invoice(Long id, Map<String, Object> body) {
        role("ADMIN"); FeeBill b = db.lock(FeeBill.class, id); CaseWorkflowService.state(b.getStatus(), "PAID");
        ClientProfile client = db.get(ClientProfile.class, b.getClientId());
        InvoiceRecord i = new InvoiceRecord(); i.setBillId(id); i.setInvoiceType("NORMAL");
        i.setInvoiceTitle(client.getInvoiceTitle() == null ? client.getClientName() : client.getInvoiceTitle()); i.setTaxpayerNo(client.getTaxpayerNo());
        input.apply(body, i, "invoiceType invoiceTitle taxpayerNo"); i.setInvoiceNo("SIM-INV-" + UUID.randomUUID());
        i.setAmount(b.getPayableAmount()); i.setIssueTime(LocalDateTime.now()); i.setStatus("ISSUED"); db.insert(i);
        b.setStatus("INVOICED"); db.update(b); events.audit("ISSUE_INVOICE", "BILL", id); return i;
    }
    public PageResult<InvoiceRecord> invoices(long page, long size) {
        role("CLIENT", "ADMIN"); var q = new QueryWrapper<InvoiceRecord>().orderByDesc("id");
        if (!CurrentUserContext.require().isAdmin()) q.inSql("bill_id", "SELECT id FROM fee_bill WHERE is_deleted = 0 AND client_id = " + access.client().getId());
        return db.page(InvoiceRecord.class, q, page, size);
    }
    public PageResult<PaymentRecord> payments(long page, long size, Long billId) {
        role("CLIENT", "ADMIN");
        if (billId != null && !CurrentUserContext.require().isAdmin()) owned(billId);
        var q = new QueryWrapper<PaymentRecord>().eq(billId != null, "bill_id", billId).orderByDesc("payment_time", "id");
        if (!CurrentUserContext.require().isAdmin()) q.inSql("bill_id", "SELECT id FROM fee_bill WHERE is_deleted = 0 AND client_id = " + access.client().getId());
        return db.page(PaymentRecord.class, q, page, size);
    }
}
