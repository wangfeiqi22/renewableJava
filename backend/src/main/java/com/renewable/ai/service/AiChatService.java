package com.renewable.ai.service;

import com.renewable.ai.entity.AiChatMessage;
import com.renewable.ai.entity.AiChatSession;
import com.renewable.ai.repository.AiChatMessageRepository;
import com.renewable.ai.repository.AiChatSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AiChatService {

    @Autowired
    private AiChatSessionRepository sessionRepository;

    @Autowired
    private AiChatMessageRepository messageRepository;

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    public AiChatSession createSession(Long userId) {
        AiChatSession session = new AiChatSession();
        session.setId(UUID.randomUUID().toString());
        session.setUserId(userId);
        return sessionRepository.save(session);
    }

    public List<AiChatSession> getUserSessions(Long userId) {
        return sessionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public AiChatMessage sendMessage(String sessionId, String content) {
        // 1. Save User Message
        AiChatMessage userMsg = new AiChatMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setSenderType("user");
        userMsg.setContent(content);
        messageRepository.save(userMsg);

        // 2. Generate Smart Response
        AiChatMessage aiMsg = generateSmartAiResponse(sessionId, content);
        
        return messageRepository.save(aiMsg);
    }

    public List<AiChatMessage> getHistory(String sessionId) {
        return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    private AiChatMessage generateSmartAiResponse(String sessionId, String input) {
        AiChatMessage response = new AiChatMessage();
        response.setSessionId(sessionId);
        response.setSenderType("ai");
        
        String lowerInput = input.toLowerCase();

        // Intent Recognition & Routing
        if (lowerInput.contains("下单") || lowerInput.contains("预约") || lowerInput.contains("清运单")) {
            response.setIntent("CREATE_ORDER");
            response.setActionPayload("/order/create");
            response.setContent("已为您识别到【预约清运】需求，即将为您跳转到创建订单页面...");
            return response;
        } else if (lowerInput.contains("车队") || lowerInput.contains("车辆") || lowerInput.contains("司机")) {
            response.setIntent("VIEW_FLEET");
            response.setActionPayload("/station/fleet");
            response.setContent("已为您识别到【车队管理】需求，即将为您跳转到车队大屏...");
            return response;
        } else if (lowerInput.contains("商城") || lowerInput.contains("兑换") || lowerInput.contains("买东西")) {
            response.setIntent("VIEW_MALL");
            response.setActionPayload("/mall");
            response.setContent("已为您识别到【积分商城】需求，马上带您去逛逛...");
            return response;
        } else if (lowerInput.contains("知识库") || lowerInput.contains("学习") || lowerInput.contains("查资料") || lowerInput.contains("分类")) {
            response.setIntent("VIEW_KNOWLEDGE");
            response.setActionPayload("/knowledge");
            response.setContent("已为您识别到【知识库查询】需求，正在跳转...");
            return response;
        } else if (lowerInput.contains("投诉") || lowerInput.contains("人工")) {
            response.setIntent("HUMAN_SERVICE");
            response.setContent("很抱歉给您带来不便，已为您转接人工客服，请稍候...");
            return response;
        }

        // 1. Check Knowledge Base first
        String kbAnswer = knowledgeBaseService.findAnswer(input);
        if (kbAnswer != null) {
            response.setContent(kbAnswer);
            return response;
        }

        // 2. Fallback to general greeting
        if (lowerInput.contains("你好") || lowerInput.contains("hello")) {
            response.setContent("您好！我是智能客服助手。您可以使用文字或语音输入，我会为您解答问题或帮您快速办理业务。");
        } else if (lowerInput.contains("价格") || lowerInput.contains("费用")) {
            response.setContent("我们的清运费用根据垃圾类型和重量计算，生活垃圾约 50元/吨，建筑垃圾约 80元/吨。");
        } else {
            response.setContent("我还在学习中，您能具体描述一下需求吗？比如您可以说：“我想预约清运” 或 “我要去积分商城”。");
        }
        
        return response;
    }
}
