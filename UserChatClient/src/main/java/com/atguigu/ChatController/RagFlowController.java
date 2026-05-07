package com.atguigu.ChatController;


import com.atguigu.RagFlowService.BailianKnowledgeService;
import com.atguigu.RagFlowService.RagflowService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ragflow")
public class RagFlowController {

    private final RagflowService ragflowService;
    @Autowired
    private BailianKnowledgeService bailianKnowledgeService;
    @Autowired
    private ChatClient qwenChatClient;

    public RagFlowController(RagflowService ragflowService) {
        this.ragflowService = ragflowService;
    }

    @GetMapping("/chat")
    public String chat(@RequestParam String message) {
        return ragflowService.chat(message);
    }
    @GetMapping(value = "/search", produces =  "text/event-stream;charset=UTF-8")
    public Flux<String> chat(
            @RequestParam(defaultValue = "1", required = false) int id,
            @RequestParam(required = false, defaultValue = "你好") String query
    ) {
        String s = bailianKnowledgeService.searchKnowledgeBase(query);

        String enhancedPrompt = """
                你是一个智能助手，请严格基于下面的【参考资料】回答用户的问题。
                如果参考资料中没有答案，请如实告知用户，不要编造信息。
                
                【参考资料】
                %s
                
                【用户问题】
                %s
                """.formatted(s, query);
        return qwenChatClient.prompt()
                .user(enhancedPrompt).advisors()
                .stream().content();
    }
}