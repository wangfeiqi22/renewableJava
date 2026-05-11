package com.renewable.ai.controller;

import com.renewable.ai.dto.AiFeedbackDTO;
import com.renewable.ai.dto.AiRecognizeResponse;
import com.renewable.ai.service.MiniMaxAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AiController {
    
    private static final Logger log = LoggerFactory.getLogger(AiController.class);

    @Autowired
    private MiniMaxAiService miniMaxAiService;

    @PostMapping("/recognize-waste")
    public ResponseEntity<AiRecognizeResponse> recognizeWaste(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "userId", required = false) Long userId) {

        log.info("收到垃圾分类识别请求, 文件名: {}, 用户ID: {}", file != null ? file.getOriginalFilename() : "null", userId);

        try {
            AiRecognizeResponse response = miniMaxAiService.recognizeWaste(file, userId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("AI识别失败", e);

            AiRecognizeResponse fallbackResponse = new AiRecognizeResponse();
            fallbackResponse.setItemName("无法识别");
            fallbackResponse.setCategory("dry");
            fallbackResponse.setConfidence(0);
            fallbackResponse.setAdvice("请手动选择垃圾类型");
            fallbackResponse.setEnvironmentalTips("感谢您的配合！");

            return ResponseEntity.ok(fallbackResponse);
        }
    }

    @PostMapping("/feedback")
    public ResponseEntity<String> submitFeedback(@RequestBody AiFeedbackDTO feedback) {
        System.out.println("Received user feedback for AI model correction: " + feedback);
        return ResponseEntity.ok("Feedback received and stored for model optimization.");
    }
}
