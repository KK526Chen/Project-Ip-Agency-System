package com.ipagency.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import org.springframework.stereotype.Component;

/** Explicit allowlists prevent request bodies from changing ownership and workflow fields. */
@Component
public class Input {
    private final ObjectMapper json;
    public Input(ObjectMapper json) { this.json = json; }
    public <T> T apply(Map<String, Object> body, T target, String fields) {
        Set<String> allowed = Set.of(fields.split(" "));
        for (String key : body.keySet()) if (!allowed.contains(key)) throw new BusinessException("不允许修改字段: " + key);
        try { return json.readerForUpdating(target).readValue(json.writeValueAsBytes(body)); }
        catch (Exception e) { throw new BusinessException("字段格式不正确"); }
    }
    public static String text(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (!(value instanceof String s) || s.isBlank()) throw new BusinessException("缺少字段: " + key);
        return s;
    }
    public static Long id(Map<String, Object> body, String key) {
        try { return Long.valueOf(body.get(key).toString()); }
        catch (Exception e) { throw new BusinessException("无效 ID: " + key); }
    }
}
