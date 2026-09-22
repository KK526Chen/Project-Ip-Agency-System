package com.ipagency.service;

import com.ipagency.common.CurrentUserContext;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** 预留 Outbox 合同：当前同步记录日志，组长接入 outbox 时只替换实现。 */
@Service
public class DomainEventPublisher {
    private static final Logger LOG = LoggerFactory.getLogger(DomainEventPublisher.class);
    public Map<String, Object> publish(String eventType, Long caseId, Long aggregateId, String title) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("eventId", UUID.randomUUID().toString());
        payload.put("eventType", eventType);
        payload.put("aggregateId", aggregateId);
        payload.put("caseId", caseId);
        var actor = CurrentUserContext.current();
        payload.put("actorUserId", actor == null ? null : actor.userId());
        payload.put("occurredAt", Instant.now().toString());
        payload.put("version", 1);
        payload.put("title", title);
        LOG.debug("domain-event {}", payload);
        return payload;
    }
}
