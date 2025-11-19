package com.example.smartlearn.controller.teacher;

import com.example.smartlearn.dto.request.QuestionDifficultyAdjustRequest;
import com.example.smartlearn.dto.response.QuestionDifficultyAdjustResponse;
import com.example.smartlearn.service.teacher.QuestionDifficultyAdjustService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 题目难度动态调整-曹雨荷部分
 * API接口：查询建议、确认调整
 * 增强版：包含完整的错误检测和处理机制
 */
@RestController
@RequestMapping("/api/teacher/question-difficulty-adjust")
@CrossOrigin(origins = "*")
public class QuestionDifficultyAdjustController {
    
    private static final Logger logger = LoggerFactory.getLogger(QuestionDifficultyAdjustController.class);
    
    @Autowired
    private QuestionDifficultyAdjustService service;

    /**
     * 健康检查接口 - 用于验证控制器是否正常工作
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        logger.info("题目难度调整API健康检查被调用");
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "QuestionDifficultyAdjustController");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * 查询可调整题目及建议
     * 题目难度动态调整-曹雨荷部分
     * 
     * @param request 查询请求，包含课程ID列表和是否包含全部课程
     * @return 题目统计信息和调整建议
     */
    @PostMapping("/query")
    public ResponseEntity<?> query(@RequestBody QuestionDifficultyAdjustRequest.Query request) {
        
        logger.info("=== 题目难度调整API - 查询请求开始 ===");
        logger.info("请求参数: courseIds={}, includeAllCourses={}", 
                request.getCourseIds(), request.getIncludeAllCourses());
        
        try {
            // 1. 输入验证
            Map<String, String> validationErrors = validateQueryRequest(request);
            if (!validationErrors.isEmpty()) {
                logger.warn("输入验证失败: {}", validationErrors);
                return ResponseEntity.badRequest().body(createErrorResponse(
                    "VALIDATION_ERROR", "输入参数验证失败", validationErrors));
            }

            // 2. 业务逻辑处理
            QuestionDifficultyAdjustResponse.QueryResult result = service.queryAdjustableQuestions(request);
            
            logger.info("查询成功完成 - 总题数: {}, 需调整: {}", 
                result.getStatistics().getTotalQuestions(),
                result.getStatistics().getNeedAdjustment());
            
            return ResponseEntity.ok(result);
            
        } catch (IllegalArgumentException e) {
            logger.error("参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(
                "INVALID_ARGUMENT", "参数错误: " + e.getMessage(), null));
                
        } catch (RuntimeException e) {
            logger.error("业务逻辑异常: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("BUSINESS_ERROR", "业务处理异常: " + e.getMessage(), null));
                
        } catch (Exception e) {
            logger.error("系统异常: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("SYSTEM_ERROR", "系统内部错误", null));
        }
    }

    /**
     * 教师确认调整
     * 题目难度动态调整-曹雨荷部分
     * 
     * @param request 确认请求，包含要调整的题目列表
     * @return 调整结果详情
     */
    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestBody QuestionDifficultyAdjustRequest.Confirm request) {
        
        logger.info("=== 题目难度调整API - 确认调整请求开始 ===");
        logger.info("请求参数: 调整数量={}", 
                request.getAdjustments() != null ? request.getAdjustments().size() : 0);
        
        try {
            // 1. 输入验证
            Map<String, String> validationErrors = validateConfirmRequest(request);
            if (!validationErrors.isEmpty()) {
                logger.warn("输入验证失败: {}", validationErrors);
                return ResponseEntity.badRequest().body(createErrorResponse(
                    "VALIDATION_ERROR", "输入参数验证失败", validationErrors));
            }

            // 2. 业务逻辑处理
            QuestionDifficultyAdjustResponse.ConfirmResult result = service.confirmAdjustments(request);
            
            logger.info("调整完成 - 成功: {}, 失败: {}", 
                result.getSuccessCount(), result.getFailedCount());
            
            return ResponseEntity.ok(result);
            
        } catch (IllegalArgumentException e) {
            logger.error("参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(
                "INVALID_ARGUMENT", "参数错误: " + e.getMessage(), null));
                
        } catch (RuntimeException e) {
            logger.error("业务逻辑异常: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("BUSINESS_ERROR", "业务处理异常: " + e.getMessage(), null));
                
        } catch (Exception e) {
            logger.error("系统异常: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("SYSTEM_ERROR", "系统内部错误", null));
        }
    }

    /**
     * 验证查询请求
     */
    private Map<String, String> validateQueryRequest(QuestionDifficultyAdjustRequest.Query request) {
        Map<String, String> errors = new HashMap<>();
        
        if (request == null) {
            errors.put("request", "请求体不能为空");
            return errors;
        }
        
        // 验证课程ID列表
        if (request.getCourseIds() != null && !request.getCourseIds().isEmpty()) {
            for (Long courseId : request.getCourseIds()) {
                if (courseId == null || courseId <= 0) {
                    errors.put("courseIds", "课程ID必须为正整数");
                    break;
                }
            }
        }
        
        return errors;
    }

    /**
     * 验证确认请求
     */
    private Map<String, String> validateConfirmRequest(QuestionDifficultyAdjustRequest.Confirm request) {
        Map<String, String> errors = new HashMap<>();
        
        if (request == null) {
            errors.put("request", "请求体不能为空");
            return errors;
        }
        
        if (request.getAdjustments() == null || request.getAdjustments().isEmpty()) {
            errors.put("adjustments", "调整列表不能为空");
            return errors;
        }
        
        // 验证每个调整项
        for (int i = 0; i < request.getAdjustments().size(); i++) {
            QuestionDifficultyAdjustRequest.Confirm.Adjustment adj = request.getAdjustments().get(i);
            String prefix = "adjustments[" + i + "]";
            
            if (adj.getQuestionId() == null || adj.getQuestionId() <= 0) {
                errors.put(prefix + ".questionId", "题目ID必须为正整数");
            }
            
            if (Boolean.TRUE.equals(adj.getShouldAdjust())) {
                if (adj.getNewDifficulty() == null || adj.getNewDifficulty() < 1 || adj.getNewDifficulty() > 5) {
                    errors.put(prefix + ".newDifficulty", "新难度必须在1-5之间");
                }
            }
        }
        
        return errors;
    }

    /**
     * 创建统一的错误响应
     */
    private Map<String, Object> createErrorResponse(String errorCode, String message, Map<String, String> details) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorCode", errorCode);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        if (details != null && !details.isEmpty()) {
            response.put("details", details);
        }
        return response;
    }

    /**
     * 处理全局异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(Exception e) {
        logger.error("全局异常处理: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(createErrorResponse("GLOBAL_ERROR", "服务器内部错误", null));
    }

    /**
     * 处理方法参数验证异常
     */
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            org.springframework.web.bind.MethodArgumentNotValidException e) {
        logger.error("参数验证异常: {}", e.getMessage());
        Map<String, String> details = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> 
            details.put(error.getField(), error.getDefaultMessage()));
        
        return ResponseEntity.badRequest()
            .body(createErrorResponse("VALIDATION_ERROR", "参数验证失败", details));
    }
} 