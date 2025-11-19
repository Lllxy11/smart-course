package com.example.smartlearn.controller.teacher;

import com.example.smartlearn.dto.request.QuizAdvancedRequest;
import com.example.smartlearn.dto.response.QuizResponse;
import com.example.smartlearn.service.teacher.QuizAdvancedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 该控制器类用于组卷管理的高级功能（题目替换、批量排序、智能组卷等）。
 */
@RestController
@RequestMapping("/api/teacher/quiz/advanced")
@CrossOrigin(origins = "*")
public class QuizAdvancedController {
    @Autowired
    private QuizAdvancedService quizAdvancedService;

    /**
     * 替换试卷中的题目
     * 该接口用于组卷管理的题目替换功能
     */
    @PostMapping("/replace-question")
    public ResponseEntity<Void> replaceQuestion(@RequestBody QuizAdvancedRequest.ReplaceQuestionRequest request) {
        quizAdvancedService.replaceQuestion(request);
        return ResponseEntity.ok().build();
    }

    /**
     * 批量重新排序试卷题目
     * 该接口用于组卷管理的题目批量排序功能
     */
    @PostMapping("/reorder-questions")
    public ResponseEntity<Void> reorderQuestions(@RequestBody QuizAdvancedRequest.ReorderQuestionsRequest request) {
        quizAdvancedService.reorderQuestions(request);
        return ResponseEntity.ok().build();
    }

    /**
     * 智能组卷
     * 该接口用于组卷管理的智能组卷功能
     */
    @PostMapping("/auto-generate")
    public ResponseEntity<QuizResponse> autoGenerateQuiz(@RequestBody QuizAdvancedRequest.AutoGenerateQuizRequest request) {
        QuizResponse response = quizAdvancedService.autoGenerateQuiz(request);
        return ResponseEntity.ok(response);
    }
} 