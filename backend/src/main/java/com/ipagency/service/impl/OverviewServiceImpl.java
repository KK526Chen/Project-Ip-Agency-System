package com.ipagency.service.impl;
import com.ipagency.common.PageResult;
import com.ipagency.service.OverviewService;
import java.util.Map;
import org.springframework.stereotype.Service;
@Service
public class OverviewServiceImpl implements OverviewService {
    @Override public Map<String, Long> overview() { return Map.of("clients", 0L, "cases", 0L, "processingCases", 0L, "pendingTasks", 0L, "upcomingDeadlines", 0L); }
    @Override public PageResult<Object> myTasks(long pageNum, long pageSize) { return PageResult.empty(pageNum, pageSize); }
    @Override public PageResult<Object> deadlines(long pageNum, long pageSize) { return PageResult.empty(pageNum, pageSize); }
}
