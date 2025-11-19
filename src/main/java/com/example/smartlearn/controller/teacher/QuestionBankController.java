package com.example.smartlearn.controller.teacher;

import com.example.smartlearn.dto.request.QuestionFilterRequest;
import com.example.smartlearn.dto.request.QuestionRequest;
import com.example.smartlearn.dto.response.QuestionResponse;
import com.example.smartlearn.service.teacher.QuestionBankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 该控制器类用于题库管理的API接口。
 * 提供题目的CRUD操作和筛选功能。
 */
@RestController
@RequestMapping("/api/teacher/question-bank")
@CrossOrigin(origins = "*")
public class QuestionBankController {

    @Autowired
    private QuestionBankService questionBankService;

    /**
     * 创建新题目
     * 该功能用于题库管理的题目创建API
     */
    @PostMapping("/questions")
    public ResponseEntity<QuestionResponse> createQuestion(
            @RequestBody QuestionRequest request,
            @RequestParam Long teacherId) {
        QuestionResponse response = questionBankService.createQuestion(request, teacherId);
        return ResponseEntity.ok(response);
    }

    /**
     * 根据条件筛选题目
     * 该功能用于题库管理的题目检索和筛选API
     */
    @PostMapping("/questions/search")
    public ResponseEntity<Page<QuestionResponse>> getQuestions(
            @RequestBody QuestionFilterRequest request,
            @RequestParam Long teacherId) {
        Page<QuestionResponse> responses = questionBankService.getQuestions(request, teacherId);
        return ResponseEntity.ok(responses);
    }

    /**
     * 根据ID获取题目详情
     * 该功能用于题库管理的题目详情查看API
     */
    @PostMapping("/questions/detail")
    public ResponseEntity<QuestionResponse> getQuestionById(
            @RequestBody QuestionIdRequest request,
            @RequestParam Long teacherId) {
        QuestionResponse response = questionBankService.getQuestionById(request.getQuestionId(), teacherId);
        return ResponseEntity.ok(response);
    }

    /**
     * 更新题目
     * 该功能用于题库管理的题目编辑API
     */
    @PutMapping("/questions/{questionId}")
    public ResponseEntity<QuestionResponse> updateQuestion(
            @PathVariable Long questionId,
            @RequestBody QuestionRequest request,
            @RequestParam Long teacherId) {
        QuestionResponse response = questionBankService.updateQuestion(questionId, request, teacherId);
        return ResponseEntity.ok(response);
    }

    /**
     * 删除题目
     * 该功能用于题库管理的题目删除API
     */
    @DeleteMapping("/questions/{questionId}")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable Long questionId,
            @RequestParam Long teacherId) {
        questionBankService.deleteQuestion(questionId, teacherId);
        return ResponseEntity.ok().build();
    }

    /**
     * 批量删除题目
     * 该功能用于题库管理的批量删除API
     */
    @DeleteMapping("/questions/batch")
    public ResponseEntity<Void> deleteQuestions(
            @RequestBody QuestionIdsRequest request,
            @RequestParam Long teacherId) {
        questionBankService.deleteQuestions(request.getQuestionIds(), teacherId);
        return ResponseEntity.ok().build();
    }

    /**
     * 根据课程ID获取题目列表
     * 该功能用于题库管理的按课程筛选API
     */
    @PostMapping("/questions/by-course")
    public ResponseEntity<List<QuestionResponse>> getQuestionsByCourse(
            @RequestBody CourseIdRequest request,
            @RequestParam Long teacherId) {
        List<QuestionResponse> responses = questionBankService.getQuestionsByCourse(request.getCourseId(), teacherId);
        return ResponseEntity.ok(responses);
    }

    /**
     * 根据关键词搜索题目
     * 该功能用于题库管理的关键词搜索API
     */
    @PostMapping("/questions/search-by-keyword")
    public ResponseEntity<List<QuestionResponse>> searchQuestionsByKeyword(
            @RequestBody KeywordRequest request,
            @RequestParam Long teacherId) {
        List<QuestionResponse> responses = questionBankService.searchQuestionsByKeyword(request.getKeyword(), teacherId);
        return ResponseEntity.ok(responses);
    }

    // 内部请求类，用于传递单个ID
    public static class QuestionIdRequest {
        private Long questionId;

        public Long getQuestionId() {
            return questionId;
        }

        public void setQuestionId(Long questionId) {
            this.questionId = questionId;
        }
    }

    // 内部请求类，用于传递多个ID
    public static class QuestionIdsRequest {
        private List<Long> questionIds;

        public List<Long> getQuestionIds() {
            return questionIds;
        }

        public void setQuestionIds(List<Long> questionIds) {
            this.questionIds = questionIds;
        }
    }

    // 内部请求类，用于传递课程ID
    public static class CourseIdRequest {
        private Long courseId;

        public Long getCourseId() {
            return courseId;
        }

        public void setCourseId(Long courseId) {
            this.courseId = courseId;
        }
    }

    // 内部请求类，用于传递关键词
    public static class KeywordRequest {
        private String keyword;

        public String getKeyword() {
            return keyword;
        }

        public void setKeyword(String keyword) {
            this.keyword = keyword;
        }
    }
} 