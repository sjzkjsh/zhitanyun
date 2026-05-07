package com.example.DataBaseController;

import com.example.Entity.ChatEntity.ChatMessage;
import com.example.Entity.ChatEntity.ContextCreateRequest;
import com.example.Entity.ChatEntity.ContextListVO;
import com.example.Entity.ChatEntity.ContextUpdateRequest;
import com.example.Entity.ReultEntity.Result;
import com.example.Service.ChatContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {

        @Autowired
        private ChatContextService contextService;
    // ========== 上下文管理接口 ==========

    // 创建一个新上下文
    @PostMapping("/context")
    public Result<String> createContext(@RequestBody ContextCreateRequest request) {
        return Result.success(contextService.createContext(request));
    }
    // 列出用户所有上下文
    @GetMapping("/contexts")
    public Result<List<ContextListVO>> listContexts(@RequestParam String userId) {
        return Result.success(contextService.listUserContexts(userId));
    }
    // 获取上下文历史记录
    @GetMapping("/context/{contextId}/history")
    public Result<List<ChatMessage>> getHistory(@PathVariable String contextId) {
        return Result.success(contextService.getContextHistory(contextId));
    }
    // 更新上下文
    @PutMapping("/context/{contextId}")
    public void updateContext(
            @PathVariable String contextId,
            @RequestBody ContextUpdateRequest request) {
        contextService.updateContext(contextId, request);
    }
    // 删除上下文
    @DeleteMapping("/context/{contextId}")
    public void deleteContext(@PathVariable String contextId) {
       contextService.deleteContext(contextId);
    }
}
