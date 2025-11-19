package com.example.smartlearn.controller.ai;

import com.example.smartlearn.service.teacher.ReportAiScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai-score")
public class AiScoreController {

    @Autowired
    private ReportAiScoreService reportAiScoreService;

    /**
     * 智能批改接口
     * @param requestBody { "submissionId": 123, "criteriaList": [ {index, criteria1, weight1, ...}, ... ] }
     */
    @PostMapping("/task")
    public ResponseEntity<?> aiScoreTask(@RequestBody Map<String, Object> requestBody) {
        try {
            Long taskId = Long.valueOf(requestBody.get("taskId").toString());
            @SuppressWarnings("unchecked")
            Map<String, Object> criteria = (Map<String, Object>) requestBody.get("criteria");

            System.out.println("[AiScoreController] 开始智能批改，taskId=" + taskId);
            reportAiScoreService.aiScoreTask(taskId, criteria);
            System.out.println("[AiScoreController] 智能批改完成");

            // 返回成功响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "智能批改完成");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("[AiScoreController] 智能批改异常: " + e.getMessage());
            e.printStackTrace();

            // 返回详细错误信息
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "智能批改失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 获取任务下所有学生的智能批改结果
     */
    @GetMapping("/task/{taskId}/results")
    public ResponseEntity<?> getTaskAiScoreResults(@PathVariable Long taskId) {
        try {
            com.example.smartlearn.dto.response.TaskAiScoreResultResponse result =
                    reportAiScoreService.getTaskAiScoreResults(taskId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取任务智能批改结果失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
