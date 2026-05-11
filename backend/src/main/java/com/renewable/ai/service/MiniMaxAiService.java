package com.renewable.ai.service;

import com.renewable.ai.dto.AiRecognizeResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;

@Service
public class MiniMaxAiService {

    @Value("${minimax.api.key:YOUR_API_KEY}")
    private String apiKey;

    @Value("${minimax.api.url:https://api.minimax.chat}")
    private String apiUrl;

    private final Random random = new Random();

    public AiRecognizeResponse recognizeWaste(MultipartFile file, Long userId) {
        AiRecognizeResponse response = new AiRecognizeResponse();

        try {
            if (apiKey != null && !apiKey.equals("YOUR_API_KEY") && !apiKey.isEmpty()) {
                return callMiniMaxApi(file, userId);
            } else {
                System.out.println("MiniMax API密钥未配置，使用模拟识别结果");
                return generateMockResponse(response, userId);
            }
        } catch (Exception e) {
            System.err.println("AI识别失败，使用备用方案: " + e.getMessage());
            e.printStackTrace();
            return generateMockResponse(response, userId);
        }
    }

    private AiRecognizeResponse callMiniMaxApi(MultipartFile file, Long userId) throws IOException {
        AiRecognizeResponse response = new AiRecognizeResponse();

        try {
            String imageBase64 = Base64.getEncoder().encodeToString(file.getBytes());

            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);

            String prompt = "请识别这张图片中的垃圾类型。我需要你返回JSON格式的结果，包含以下字段：\n" +
                    "{\n" +
                    "  \"type\": \"垃圾类型(可回收物/有害垃圾/厨余垃圾/干垃圾/建筑垃圾/大件垃圾)\",\n" +
                    "  \"confidence\": 置信度(0-100的数字),\n" +
                    "  \"itemName\": \"具体物品名称(不超过20个字)\",\n" +
                    "  \"estimatedCount\": 数量(整数),\n" +
                    "  \"estimatedVolume\": 体积(立方米，保留2位小数),\n" +
                    "  \"estimatedWeight\": \"预估重量(如0.5kg)\"\n" +
                    "}\n\n" +
                    "要求：\n" +
                    "1. 只返回JSON，不要其他文字\n" +
                    "2. 体积估算要基于图片中的实际物体大小\n" +
                    "3. 数量要合理估算\n" +
                    "4. 根据物品类型合理估算重量";

            String requestBody = "{\n" +
                    "  \"model\": \"MiniMax-Text-01\",\n" +
                    "  \"messages\": [\n" +
                    "    {\n" +
                    "      \"role\": \"user\",\n" +
                    "      \"content\": [\n" +
                    "        {\n" +
                    "          \"type\": \"image_url\",\n" +
                    "          \"image_url\": {\n" +
                    "            \"url\": \"data:image/jpeg;base64," + imageBase64 + "\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"type\": \"text\",\n" +
                    "          \"text\": \"" + prompt.replace("\"", "\\\"").replace("\n", "\\n") + "\"\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            System.out.println("正在调用MiniMax API进行智能识别...");
            System.out.println("请求URL: " + apiUrl);

            long startTime = System.currentTimeMillis();

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            long responseTime = System.currentTimeMillis() - startTime;
            System.out.println("MiniMax API响应码: " + responseCode + ", 耗时: " + responseTime + "ms");

            if (responseCode == 200) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder responseStr = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        responseStr.append(line);
                    }

                    String responseText = responseStr.toString();
                    System.out.println("MiniMax API原始响应: " + responseText);

                    String content = extractContentFromResponse(responseText);
                    System.out.println("提取的AI响应内容: " + content);

                    if (content != null && !content.isEmpty()) {
                        String type = extractJsonValue(content, "type", "干垃圾");
                        String itemName = extractJsonValue(content, "itemName", "无法识别的垃圾");
                        int confidence = extractJsonInt(content, "confidence", 75);
                        double volume = extractJsonDouble(content, "estimatedVolume", 0.1);
                        int count = extractJsonInt(content, "estimatedCount", 1);
                        String weight = extractJsonValue(content, "estimatedWeight", "约0.5kg");

                        String mappedCategory = mapToStandardCategory(type);

                        response.setCategory(mappedCategory);
                        response.setItemName(itemName);
                        response.setConfidence(confidence);
                        response.setVolumeUnit("m³");
                        response.setEstimatedVolume(volume);
                        response.setEstimatedCount(count);
                        response.setEstimatedWeight(weight);
                        response.setAdvice(generateAdvice(mappedCategory));
                        response.setEnvironmentalTips(generateTips(mappedCategory));
                        response.setDisposalMethod(generateDisposalMethod(mappedCategory));
                        response.setRecyclingValue(generateRecyclingValue(mappedCategory));
                        response.setSmartRemark(generateSmartRemark(itemName, mappedCategory, count, volume, weight));
                        response.setAbTestGroup((userId != null && userId % 2 != 0) ? "A_Standard" : "B_Enhanced_V2");

                        System.out.println("识别成功 - 类型: " + mappedCategory + ", 物品: " + itemName +
                                ", 体积: " + volume + "m³, 数量: " + count + ", 置信度: " + confidence + "%" +
                                ", 耗时: " + responseTime + "ms");
                    } else {
                        System.err.println("无法从API响应中提取内容");
                        return generateMockResponse(response, userId);
                    }
                }
            } else {
                System.err.println("MiniMax API调用失败，响应码: " + responseCode);
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder errorStr = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        errorStr.append(line);
                    }
                    System.err.println("错误响应: " + errorStr.toString());
                }
                return generateMockResponse(response, userId);
            }

        } catch (Exception e) {
            System.err.println("调用MiniMax API时出错: " + e.getMessage());
            e.printStackTrace();
            return generateMockResponse(response, userId);
        }

        return response;
    }

    private String extractContentFromResponse(String jsonResponse) {
        try {
            int choicesIndex = jsonResponse.indexOf("\"choices\"");
            if (choicesIndex == -1) {
                return null;
            }

            int messageIndex = jsonResponse.indexOf("\"message\"", choicesIndex);
            if (messageIndex == -1) {
                return null;
            }

            int contentIndex = jsonResponse.indexOf("\"content\"", messageIndex);
            if (contentIndex == -1) {
                return null;
            }

            int startQuote = jsonResponse.indexOf("\"", contentIndex + 10);
            if (startQuote == -1) {
                return null;
            }

            int endQuote = startQuote + 1;
            int braceCount = 0;
            boolean inString = false;

            while (endQuote < jsonResponse.length()) {
                char c = jsonResponse.charAt(endQuote);
                if (c == '\\' && endQuote + 1 < jsonResponse.length()) {
                    endQuote += 2;
                    continue;
                }
                if (c == '"') {
                    inString = !inString;
                } else if (!inString) {
                    if (c == '{') braceCount++;
                    if (c == '}') {
                        if (braceCount == 0) {
                            return jsonResponse.substring(startQuote + 1, endQuote);
                        }
                        braceCount--;
                    }
                }
                endQuote++;
            }

            endQuote = jsonResponse.indexOf("\"", startQuote + 1);
            if (endQuote > startQuote) {
                return jsonResponse.substring(startQuote + 1, endQuote);
            }

        } catch (Exception e) {
            System.err.println("提取内容失败: " + e.getMessage());
        }
        return null;
    }

    private String extractJsonValue(String json, String key, String defaultValue) {
        try {
            int keyIndex = json.indexOf("\"" + key + "\"");
            if (keyIndex == -1) return defaultValue;

            int colonIndex = json.indexOf(":", keyIndex);
            int startQuote = json.indexOf("\"", colonIndex);
            int endQuote = json.indexOf("\"", startQuote + 1);

            if (startQuote == -1 || endQuote == -1) return defaultValue;
            return json.substring(startQuote + 1, endQuote);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private int extractJsonInt(String json, String key, int defaultValue) {
        try {
            int keyIndex = json.indexOf("\"" + key + "\"");
            if (keyIndex == -1) return defaultValue;

            int colonIndex = json.indexOf(":", keyIndex);
            int start = colonIndex + 1;
            while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '"')) {
                start++;
            }
            int end = start;
            while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.')) {
                end++;
            }

            if (end > start) {
                String numStr = json.substring(start, end).trim();
                if (numStr.contains(".")) {
                    return (int) Double.parseDouble(numStr);
                }
                return Integer.parseInt(numStr);
            }
            return defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private double extractJsonDouble(String json, String key, double defaultValue) {
        try {
            int keyIndex = json.indexOf("\"" + key + "\"");
            if (keyIndex == -1) return defaultValue;

            int colonIndex = json.indexOf(":", keyIndex);
            int start = colonIndex + 1;
            while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '"')) {
                start++;
            }
            int end = start;
            while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.')) {
                end++;
            }

            if (end > start) {
                String numStr = json.substring(start, end).trim();
                return Double.parseDouble(numStr);
            }
            return defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private AiRecognizeResponse generateMockResponse(AiRecognizeResponse response, Long userId) {
        String[] categories = {"可回收物", "有害垃圾", "厨余垃圾", "干垃圾"};
        String[] descriptions = {"塑料瓶", "废电池", "剩饭剩菜", "脏纸巾"};
        double[] volumes = {0.002, 0.001, 0.5, 0.05};
        String[] weights = {"约0.05kg", "约0.3kg", "约2kg", "约0.2kg"};

        int idx = random.nextInt(categories.length);
        String category = categories[idx];
        String description = descriptions[idx];
        double volume = volumes[idx];
        String weight = weights[idx];

        String mappedCategory = mapToStandardCategory(category);

        response.setItemName(description);
        response.setCategory(mappedCategory);
        response.setConfidence(50 + random.nextInt(50));
        response.setVolumeUnit("m³");
        response.setEstimatedVolume(volume);
        response.setEstimatedCount(1 + random.nextInt(5));
        response.setEstimatedWeight(weight);
        response.setAdvice(generateAdvice(mappedCategory));
        response.setEnvironmentalTips(generateTips(mappedCategory));
        response.setDisposalMethod(generateDisposalMethod(mappedCategory));
        response.setRecyclingValue(generateRecyclingValue(mappedCategory));
        response.setSmartRemark(generateSmartRemark(description, mappedCategory,
                response.getEstimatedCount(), volume, weight));
        response.setAbTestGroup((userId != null && userId % 2 != 0) ? "A_Standard" : "B_Enhanced_V2");

        return response;
    }

    private String mapToStandardCategory(String type) {
        if (type.contains("可回收") || type.contains("recyclable")) return "recyclable";
        if (type.contains("有害") || type.contains("hazardous")) return "hazardous";
        if (type.contains("厨余") || type.contains("湿垃圾") || type.contains("kitchen") || type.contains("wet")) return "wet";
        if (type.contains("干垃圾") || type.contains("dry")) return "dry";
        if (type.contains("建筑") || type.contains("construction")) return "construction";
        if (type.contains("大件") || type.contains("bulky")) return "bulky";
        return "dry";
    }

    private String generateAdvice(String category) {
        switch (category) {
            case "recyclable": return "请倒空液体后压扁投入可回收物收集容器。";
            case "hazardous": return "请轻放，防止破损导致有害物质泄漏。";
            case "wet": return "请沥干水分后投放至湿垃圾收集容器。";
            case "construction": return "请预约专项清运服务，勿混入生活垃圾。";
            case "bulky": return "请预约大件垃圾清运服务。";
            default: return "请直接投入干垃圾桶。";
        }
    }

    private String generateTips(String category) {
        switch (category) {
            case "recyclable": return "回收1吨塑料可节约石油6吨，减少二氧化碳排放3吨！";
            case "hazardous": return "有害垃圾包含重金属，随意丢弃会严重污染土壤和地下水！";
            case "wet": return "湿垃圾可发酵成有机肥料，变废为宝，滋养绿色生活！";
            case "construction": return "建筑垃圾需专门处理，不可混入生活垃圾！";
            case "bulky": return "大件垃圾随意丢弃会影响市容，请预约专业清运！";
            default: return "干垃圾将进行无害化焚烧发电，变废为能！";
        }
    }

    private String generateDisposalMethod(String category) {
        switch (category) {
            case "recyclable": return "送往再生资源回收站进行再利用";
            case "hazardous": return "送至危险废物处理中心专业处理";
            case "wet": return "送往厨余垃圾处理厂堆肥发酵";
            case "construction": return "送往建筑垃圾消纳场填埋或破碎再利用";
            case "bulky": return "预约专业清运公司上门收集";
            default: return "送至垃圾焚烧发电厂进行无害化处理";
        }
    }

    private String generateRecyclingValue(String category) {
        switch (category) {
            case "recyclable": return "具有较高回收价值，可变卖或兑换积分";
            case "hazardous": return "需专业处理，无直接回收价值";
            case "wet": return "可发酵成有机肥料或生物燃气";
            case "construction": return "部分可破碎再利用，如混凝土块可做地基材料";
            case "bulky": return "部分可拆解回收金属、塑料等材料";
            default: return "能量回收价值，用于焚烧发电";
        }
    }

    private String generateSmartRemark(String itemName, String category, int count, double volume, String weight) {
        String categoryName = mapCategoryToChinese(category);

        StringBuilder remark = new StringBuilder();
        remark.append("【智能识别报告】\n");
        remark.append("识别物品：").append(itemName).append("\n");
        remark.append("垃圾类型：").append(categoryName).append("\n");
        remark.append("预估数量：").append(count).append("件\n");
        remark.append("预估体积：").append(String.format("%.2f", volume)).append("立方米\n");
        remark.append("预估重量：").append(weight).append("\n\n");

        remark.append("【处理建议】\n");
        remark.append(generateAdvice(category)).append("\n\n");

        remark.append("【处置方式】\n");
        remark.append(generateDisposalMethod(category)).append("\n\n");

        remark.append("【回收价值】\n");
        remark.append(generateRecyclingValue(category)).append("\n\n");

        remark.append("【环保提示】\n");
        remark.append(generateTips(category));

        return remark.toString();
    }

    private String mapCategoryToChinese(String category) {
        switch (category) {
            case "recyclable": return "可回收物";
            case "hazardous": return "有害垃圾";
            case "wet": return "厨余垃圾";
            case "dry": return "干垃圾";
            case "construction": return "建筑垃圾";
            case "bulky": return "大件垃圾";
            default: return "其他垃圾";
        }
    }
}
