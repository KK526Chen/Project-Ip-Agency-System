package com.ipagency.service;
import com.ipagency.common.PageResult;
import java.util.Map;
public interface OverviewService {
    Map<String, Long> overview();
    PageResult<Object> myTasks(long pageNum, long pageSize);
    PageResult<Object> deadlines(long pageNum, long pageSize);
}
