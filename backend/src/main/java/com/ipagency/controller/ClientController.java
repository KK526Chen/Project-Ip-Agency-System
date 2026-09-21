package com.ipagency.controller;

import com.ipagency.common.*;
import com.ipagency.entity.ClientInfo;
import com.ipagency.service.ClientInfoService;
import com.ipagency.vo.ClientSelectorVO;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clients")
public class ClientController {
    private final ClientInfoService clientService;
    public ClientController(ClientInfoService clientService) { this.clientService = clientService; }

    @GetMapping public ApiResponse<PageResult<ClientInfo>> list(@RequestParam(defaultValue = "1") long pageNum, @RequestParam(defaultValue = "10") long pageSize) { return ApiResponse.success(PageResult.empty(pageNum, pageSize)); }
    @GetMapping("/selector") public ApiResponse<List<ClientSelectorVO>> selector(@RequestParam(required = false) String keyword) {
        if ("ASSISTANT".equals(CurrentUserContext.require().role())) return ApiResponse.success(List.of());
        return ApiResponse.success(clientService.lambdaQuery().like(keyword != null && !keyword.isBlank(), ClientInfo::getClientName, keyword)
                .list().stream().map(c -> new ClientSelectorVO(c.getId(), c.getClientName(), c.getClientType())).toList());
    }
    @GetMapping("/{id}") public ApiResponse<ClientInfo> detail(@PathVariable Long id) { return SkeletonSupport.notImplemented(); }
    @PostMapping public ApiResponse<Void> create(@RequestBody Map<String, Object> request) { return SkeletonSupport.notImplemented(); }
    @PutMapping("/{id}") public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> request) { return SkeletonSupport.notImplemented(); }
    @DeleteMapping("/{id}") public ApiResponse<Void> delete(@PathVariable Long id) { return SkeletonSupport.notImplemented(); }
}
