package com.ipagency.common;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class ApiResponseTest {
    @Test
    void createsSuccessResponse() {
        ApiResponse<String> response = ApiResponse.success("ok");
        assertThat(response.success()).isTrue();
        assertThat(response.message()).isEqualTo("操作成功");
        assertThat(response.data()).isEqualTo("ok");
    }

    @Test
    void createsEmptyPage() {
        PageResult<Object> page = PageResult.empty(1, 10);
        assertThat(page.list()).isEmpty();
        assertThat(page.total()).isZero();
        assertThat(page.pageNum()).isEqualTo(1);
    }
}
