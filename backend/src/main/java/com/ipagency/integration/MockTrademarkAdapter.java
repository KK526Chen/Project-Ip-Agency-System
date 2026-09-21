package com.ipagency.integration;
import org.springframework.stereotype.Component;
@Component
public class MockTrademarkAdapter extends MockAdapter {
    public String systemCode() { return "TRADEMARK"; }
}
