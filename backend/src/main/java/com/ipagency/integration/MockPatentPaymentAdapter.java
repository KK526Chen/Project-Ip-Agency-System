package com.ipagency.integration;
import org.springframework.stereotype.Component;
@Component
public class MockPatentPaymentAdapter extends MockAdapter {
    public String systemCode() { return "PATENT_PAYMENT"; }
}
