# AI Waste Recognition Optimization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement the AI waste recognition optimization feature including UI for detailed reports, multi-level confidence validation (<85% fallback to manual review), A/B testing flag, and a user feedback loop.

**Architecture:** 
- Frontend: Vue 3 Composition API, Element Plus. Update `OrderCreateView.vue` and `DriverTaskView.vue` with new categories and complex Mock AI logic. Add `AiRecognitionReport` and `AiFeedbackDialog` components.
- Backend: Java Spring Boot. Add Mock endpoints for `/api/ai/recognize-waste` and `/api/ai/feedback` in a new `AiController.java` to support A/B testing flags and feedback storage.

**Tech Stack:** Vue 3, Element Plus, Java, Spring Boot, Axios.

---

### Task 1: Add AI Controller and Endpoints (Backend)

**Files:**
- Create: `backend/src/main/java/com/renewable/ai/controller/AiController.java`
- Create: `backend/src/main/java/com/renewable/ai/dto/AiFeedbackDTO.java`
- Create: `backend/src/main/java/com/renewable/ai/dto/AiRecognizeResponse.java`

- [ ] **Step 1: Create DTOs**

```java
// backend/src/main/java/com/renewable/ai/dto/AiFeedbackDTO.java
package com.renewable.ai.dto;
import lombok.Data;

@Data
public class AiFeedbackDTO {
    private String originalImageUrl;
    private String predictedType;
    private String userCorrectedType;
    private Integer confidence;
}

// backend/src/main/java/com/renewable/ai/dto/AiRecognizeResponse.java
package com.renewable.ai.dto;
import lombok.Data;

@Data
public class AiRecognizeResponse {
    private String itemName;
    private String category;
    private Integer confidence;
    private String advice;
    private String environmentalTips;
    private String abTestGroup;
}
```

- [ ] **Step 2: Create Controller**

```java
// backend/src/main/java/com/renewable/ai/controller/AiController.java
package com.renewable.ai.controller;

import com.renewable.ai.dto.AiFeedbackDTO;
import com.renewable.ai.dto.AiRecognizeResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Random;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AiController {

    private final Random random = new Random();
    private final String[] categories = {"recyclable", "hazardous", "wet", "dry"};
    private final String[] itemNames = {"塑料瓶", "废电池", "剩饭剩菜", "脏纸巾"};

    @PostMapping("/recognize-waste")
    public ResponseEntity<AiRecognizeResponse> recognizeWaste(@RequestParam(value = "file", required = false) MultipartFile file, @RequestParam(value = "userId", required = false) Long userId) {
        // Mock processing delay
        try { Thread.sleep(1500); } catch (InterruptedException e) {}

        AiRecognizeResponse response = new AiRecognizeResponse();
        int idx = random.nextInt(categories.length);
        response.setItemName(itemNames[idx]);
        response.setCategory(categories[idx]);
        // Random confidence between 60 and 99
        response.setConfidence(60 + random.nextInt(40));
        response.setAdvice("请按照标准分类规范投放");
        response.setEnvironmentalTips("正确分类有助于资源循环利用！");
        
        // A/B test split based on userId (even = B, odd = A)
        response.setAbTestGroup(userId != null && userId % 2 == 0 ? "B_Enhanced" : "A_Standard");
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/feedback")
    public ResponseEntity<String> submitFeedback(@RequestBody AiFeedbackDTO feedback) {
        System.out.println("Received user feedback: " + feedback);
        return ResponseEntity.ok("Feedback received and stored for model optimization.");
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/renewable/ai/controller/AiController.java backend/src/main/java/com/renewable/ai/dto/AiFeedbackDTO.java backend/src/main/java/com/renewable/ai/dto/AiRecognizeResponse.java
git commit -m "feat(ai): add mock ai recognition and feedback endpoints"
```

### Task 2: Update Frontend Categories

**Files:**
- Modify: `frontend/src/views/OrderCreateView.vue`
- Modify: `frontend/src/views/DriverTaskView.vue`
- Modify: `frontend/src/views/OrderListView.vue`

- [ ] **Step 1: Update Categories in OrderCreateView.vue**

Replace existing `wasteTypes` array with the new 4 standard categories.

```javascript
const wasteTypes = [
  { label: '可回收物', value: 'recyclable', icon: '♻️' },
  { label: '有害垃圾', value: 'hazardous', icon: '☠️' },
  { label: '湿垃圾', value: 'wet', icon: '🍲' },
  { label: '干垃圾', value: 'dry', icon: '🗑️' }
]
```

Also change default `orderForm.wasteType` to `'recyclable'`.

- [ ] **Step 2: Update Categories in DriverTaskView.vue**

Same change to `wasteTypes` array in `DriverTaskView.vue`.

- [ ] **Step 3: Update Category mapping in OrderListView.vue**

Update `getWasteTypeName` mapping to include new categories.

- [ ] **Step 4: Commit**

```bash
git add frontend/src/views/OrderCreateView.vue frontend/src/views/DriverTaskView.vue frontend/src/views/OrderListView.vue
git commit -m "feat(ui): update waste categories to standard 4 types"
```

### Task 3: Implement AI Recognition UI & Feedback Loop in OrderCreateView

**Files:**
- Modify: `frontend/src/views/OrderCreateView.vue`

- [ ] **Step 1: Add new reactive state and UI dialogs**

Add reactive state for AI results and Dialogs.

```javascript
const showAiResultDialog = ref(false)
const showFeedbackDialog = ref(false)
const aiResult = reactive({
  itemName: '',
  category: '',
  confidence: 0,
  advice: '',
  environmentalTips: '',
  abTestGroup: ''
})
const feedbackForm = reactive({
  originalImageUrl: 'mock_url.jpg',
  predictedType: '',
  userCorrectedType: ''
})
```

- [ ] **Step 2: Update `handleAiAnalyze` method**

Replace the current `setTimeout` mock with an actual API call to our new backend endpoint.

```javascript
const handleAiAnalyze = async (options) => {
  aiAnalyzing.value = true
  try {
    const formData = new FormData()
    formData.append('file', options.file)
    formData.append('userId', user.value.id || 0)
    
    const res = await api.post('/ai/recognize-waste', formData)
    const data = res.data
    Object.assign(aiResult, data)
    
    if (data.confidence >= 85) {
      ElMessage.success(`AI 高置信度识别完成 (${data.confidence}%)`)
      orderForm.wasteType = data.category
      orderForm.wasteDesc = `AI 识别结果：${data.itemName}，建议：${data.advice}`
    } else {
      ElMessage.warning(`AI 置信度较低 (${data.confidence}%)，请人工核对`)
      showAiResultDialog.value = true
    }
  } catch (error) {
    ElMessage.error('AI 识别失败')
  } finally {
    aiAnalyzing.value = false
  }
}
```

- [ ] **Step 3: Add Dialog Templates to the Template**

Add the `<el-dialog>` for Low Confidence Review and User Feedback.

```html
    <el-dialog v-model="showAiResultDialog" title="智能识别结果确认" width="400px">
      <div class="ai-report">
        <p><strong>识别物品：</strong> {{ aiResult.itemName }}</p>
        <p><strong>推荐分类：</strong> {{ getWasteTypeName(aiResult.category) }} (置信度: {{ aiResult.confidence }}%)</p>
        <p><strong>处理建议：</strong> {{ aiResult.advice }}</p>
        <p><strong>环保提示：</strong> {{ aiResult.environmentalTips }}</p>
        <p><em>(实验组标识：{{ aiResult.abTestGroup }})</em></p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="openFeedbackDialog">识别有误，我要纠错</el-button>
          <el-button type="primary" @click="confirmAiResult">确认分类</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="showFeedbackDialog" title="模型纠错反馈" width="400px">
      <el-form :model="feedbackForm">
        <el-form-item label="实际分类">
          <el-select v-model="feedbackForm.userCorrectedType" placeholder="请选择正确的分类">
            <el-option v-for="type in wasteTypes" :key="type.value" :label="type.label" :value="type.value"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showFeedbackDialog = false">取消</el-button>
          <el-button type="primary" @click="submitFeedback">提交反馈</el-button>
        </span>
      </template>
    </el-dialog>
```

- [ ] **Step 4: Implement Dialog Action Methods**

```javascript
const confirmAiResult = () => {
  orderForm.wasteType = aiResult.category
  orderForm.wasteDesc = `AI 识别结果：${aiResult.itemName}，建议：${aiResult.advice}`
  showAiResultDialog.value = false
}

const openFeedbackDialog = () => {
  feedbackForm.predictedType = aiResult.category
  feedbackForm.userCorrectedType = ''
  showFeedbackDialog.value = true
}

const submitFeedback = async () => {
  if (!feedbackForm.userCorrectedType) {
    ElMessage.warning('请选择正确的分类')
    return
  }
  try {
    await api.post('/ai/feedback', {
      originalImageUrl: feedbackForm.originalImageUrl,
      predictedType: feedbackForm.predictedType,
      userCorrectedType: feedbackForm.userCorrectedType,
      confidence: aiResult.confidence
    })
    ElMessage.success('感谢您的反馈！模型已记录该次纠错。')
    
    // Apply the user's correction to the form
    orderForm.wasteType = feedbackForm.userCorrectedType
    orderForm.wasteDesc = `人工修正 AI 识别结果为：${getWasteTypeName(feedbackForm.userCorrectedType)}`
    
    showFeedbackDialog.value = false
    showAiResultDialog.value = false
  } catch (e) {
    ElMessage.error('反馈提交失败')
  }
}
```

- [ ] **Step 5: Commit**

```bash
git add frontend/src/views/OrderCreateView.vue
git commit -m "feat(ui): implement multi-level confidence review and user feedback dialogs"
```