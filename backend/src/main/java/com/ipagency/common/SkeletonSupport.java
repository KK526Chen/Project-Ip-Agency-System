package com.ipagency.common;

import org.springframework.http.HttpStatus;

public final class SkeletonSupport {
    private SkeletonSupport() { }

    public static <T> T notImplemented() {
        throw new BusinessException("该业务能力将在对应模块中实现", HttpStatus.NOT_IMPLEMENTED);
    }
}
