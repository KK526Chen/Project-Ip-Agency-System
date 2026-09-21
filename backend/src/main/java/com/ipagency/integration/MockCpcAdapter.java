package com.ipagency.integration;
import org.springframework.stereotype.Component;
@Component
public class MockCpcAdapter extends MockAdapter {
    public String systemCode() { return "CPC"; }
}
