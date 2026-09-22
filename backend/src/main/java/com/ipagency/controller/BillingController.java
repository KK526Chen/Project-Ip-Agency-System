package com.ipagency.controller;

import com.ipagency.common.*;
import com.ipagency.entity.*;
import com.ipagency.service.*;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class BillingController {
    private final BillingService bills;
    public BillingController(BillingService bills) {
        this.bills = bills;
    }

    @GetMapping({"/admin/bills", "/client/bills", "/bills"})
    public ApiResponse<?> list(@RequestParam(defaultValue="1") long pageNum,
            @RequestParam(defaultValue="10") long pageSize,
            @RequestParam(required=false) String status,
            @RequestParam(required=false) Long caseId) {
        return ApiResponse.success(bills.list(pageNum,pageSize,status,caseId));
    }

    @PostMapping("/admin/bills")
    public ApiResponse<?> create(@RequestBody Map<String,Object> body) {
        return ApiResponse.success(bills.save(null,body));
    }

    @PutMapping("/admin/bills/{id}")
    public ApiResponse<?> update(@PathVariable Long id,
            @RequestBody Map<String,Object> body) {
        return ApiResponse.success(bills.save(id,body));
    }

    @PostMapping("/client/bills/{id}/confirm")
    public ApiResponse<?> confirm(@PathVariable Long id) {
        return ApiResponse.success(bills.confirm(id));
    }

    @PostMapping("/client/bills/{id}/pay")
    public ApiResponse<?> pay(@PathVariable Long id) {
        return ApiResponse.success(bills.pay(id));
    }

    @PostMapping("/admin/bills/{id}/invoice")
    public ApiResponse<?> invoice(@PathVariable Long id,
            @RequestBody(required=false) Map<String,Object> body) {
        return ApiResponse.success(bills.invoice(id,body == null ? Map.of() : body));
    }

    @GetMapping({"/client/invoices", "/admin/invoices"})
    public ApiResponse<?> invoices(@RequestParam(defaultValue="1") long pageNum,
            @RequestParam(defaultValue="10") long pageSize) {
        return ApiResponse.success(bills.invoices(pageNum,pageSize));
    }

    @GetMapping({"/client/payments", "/admin/payments"})
    public ApiResponse<?> payments(@RequestParam(defaultValue="1") long pageNum,
            @RequestParam(defaultValue="10") long pageSize,
            @RequestParam(required=false) Long billId) {
        return ApiResponse.success(bills.payments(pageNum,pageSize,billId));
    }
}
