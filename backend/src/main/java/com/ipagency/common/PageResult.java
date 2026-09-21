package com.ipagency.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.Collections;
import java.util.List;

public record PageResult<T>(List<T> list, long total, long pageNum, long pageSize, long pages) {
    public static <T> PageResult<T> from(IPage<T> page) {
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    public static <T> PageResult<T> empty(long pageNum, long pageSize) {
        return new PageResult<>(Collections.emptyList(), 0, pageNum, pageSize, 0);
    }
}
