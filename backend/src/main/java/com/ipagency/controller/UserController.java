package com.ipagency.controller;

import com.ipagency.common.ApiResponse;
import com.ipagency.common.BusinessException;
import com.ipagency.common.CurrentUserContext;
import com.ipagency.common.PageResult;
import com.ipagency.common.SkeletonSupport;
import com.ipagency.entity.SysUser;
import com.ipagency.service.SysUserService;
import com.ipagency.vo.UserSelectorVO;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final SysUserService userService;
    public UserController(SysUserService userService) { this.userService = userService; }

    @GetMapping
    public ApiResponse<PageResult<Object>> list(@RequestParam(defaultValue = "1") long pageNum,
                                                  @RequestParam(defaultValue = "10") long pageSize) {
        requireAdmin();
        return ApiResponse.success(PageResult.empty(pageNum, pageSize));
    }

    @GetMapping("/selector")
    public ApiResponse<List<UserSelectorVO>> selector(@RequestParam(required = false) String keyword) {
        List<UserSelectorVO> result = userService.lambdaQuery().eq(SysUser::getStatus, 1)
                .and(keyword != null && !keyword.isBlank(), q -> q.like(SysUser::getRealName, keyword).or().like(SysUser::getUsername, keyword))
                .list().stream().map(u -> new UserSelectorVO(u.getId(), u.getRealName(), u.getRole())).toList();
        return ApiResponse.success(result);
    }

    @PostMapping public ApiResponse<Void> create(@RequestBody Map<String, Object> request) { requireAdmin(); return SkeletonSupport.notImplemented(); }
    @PutMapping("/{id}") public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> request) { requireAdmin(); return SkeletonSupport.notImplemented(); }
    @PutMapping("/{id}/status") public ApiResponse<Void> status(@PathVariable Long id, @RequestBody Map<String, Object> request) { requireAdmin(); return SkeletonSupport.notImplemented(); }

    private void requireAdmin() {
        if (!CurrentUserContext.require().isAdmin()) throw new BusinessException("仅管理员可执行该操作", HttpStatus.FORBIDDEN);
    }
}
