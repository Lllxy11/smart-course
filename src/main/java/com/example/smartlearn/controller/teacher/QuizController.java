package com.example.smartlearn.controller.teacher;

import com.example.smartlearn.dto.request.QuizQuestionRequest;
import com.example.smartlearn.dto.request.QuizRequest;
import com.example.smartlearn.dto.response.QuizDetailResponse;
import com.example.smartlearn.dto.response.QuizResponse;
import com.example.smartlearn.service.teacher.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 该控制器类用于组卷管理的API接口。
 * 提供试卷的CRUD操作和题目管理功能。
 */
@RestController
@RequestMapping("/api/teacher/quiz")
@CrossOrigin(origins = "*")
public class QuizController {

    @Autowired
    private QuizService quizService;

    /**
     * 创建新试卷
     * 该功能用于组卷管理的试卷创建API
     */
    @PostMapping("/quizzes")
    public ResponseEntity<QuizResponse> createQuiz(@RequestBody QuizRequest request) {
        QuizResponse response = quizService.createQuiz(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 根据ID获取试卷详情
     * 该功能用于组卷管理的试卷详情查看API
     */
    @PostMapping("/quizzes/detail")
    public ResponseEntity<QuizDetailResponse> getQuizById(
            @RequestBody QuizIdRequest request,
            @RequestParam Long teacherId) {
        QuizDetailResponse response = quizService.getQuizById(request.getQuizId(), teacherId);
        return ResponseEntity.ok(response);
    }

    /**
     * 更新试卷基本信息
     * 该功能用于组卷管理的试卷编辑API
     */
    @PutMapping("/quizzes/{quizId}")
    public ResponseEntity<QuizResponse> updateQuiz(
            @PathVariable Long quizId,
            @RequestBody QuizRequest request,
            @RequestParam Long teacherId) {
        QuizResponse response = quizService.updateQuiz(quizId, request, teacherId);
        return ResponseEntity.ok(response);
    }

    /**
     * 删除试卷
     * 该功能用于组卷管理的试卷删除API
     */
    @DeleteMapping("/quizzes/{quizId}")
    public ResponseEntity<Void> deleteQuiz(
            @PathVariable Long quizId,
            @RequestParam Long teacherId) {
        quizService.deleteQuiz(quizId, teacherId);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取教师创建的所有试卷
     * 该功能用于组卷管理的试卷列表查看API
     */
    @GetMapping("/quizzes")
    public ResponseEntity<List<QuizResponse>> getQuizzesByTeacher(@RequestParam Long teacherId) {
        List<QuizResponse> responses = quizService.getQuizzesByTeacher(teacherId);
        return ResponseEntity.ok(responses);
    }

    /**
     * 向试卷添加题目
     * 该功能用于组卷管理的题目添加API
     */
    @PostMapping("/quizzes/{quizId}/questions")
    public ResponseEntity<Void> addQuestionToQuiz(
            @PathVariable Long quizId,
            @RequestBody QuizQuestionRequest request,
            @RequestParam Long teacherId) {
        quizService.addQuestionToQuiz(quizId, request, teacherId);
        return ResponseEntity.ok().build();
    }

    /**
     * 从试卷移除题目
     * 该功能用于组卷管理的题目移除API
     */
    @DeleteMapping("/quizzes/{quizId}/questions/{questionId}")
    public ResponseEntity<Void> removeQuestionFromQuiz(
            @PathVariable Long quizId,
            @PathVariable Long questionId,
            @RequestParam Long teacherId) {
        quizService.removeQuestionFromQuiz(quizId, questionId, teacherId);
        return ResponseEntity.ok().build();
    }

    /**
     * 设置题目分数和顺序
     * 该功能用于组卷管理的题目设置API
     */
    @PutMapping("/quizzes/{quizId}/questions/settings")
    public ResponseEntity<Void> setQuestionSettings(
            @PathVariable Long quizId,
            @RequestBody QuizQuestionRequest request,
            @RequestParam Long teacherId) {
        quizService.setQuestionSettings(quizId, request, teacherId);
        return ResponseEntity.ok().build();
    }

    /**
     * 复制试卷
     * 该功能用于组卷管理的试卷复制API
     */
    @PostMapping("/quizzes/{quizId}/copy")
    public ResponseEntity<QuizResponse> copyQuiz(
            @PathVariable Long quizId,
            @RequestBody CopyQuizRequest request,
            @RequestParam Long teacherId) {
        QuizResponse response = quizService.copyQuiz(quizId, request.getNewTitle(), teacherId);
        return ResponseEntity.ok(response);
    }

    /**
     * 通过课程ID获取试卷列表（教师端）
     * 该功能用于教师端按课程查看试卷
     */
    @PostMapping("/quizzes/by-course")
    public ResponseEntity<List<QuizResponse>> getQuizzesByCourse(@RequestBody CourseIdRequest request) {
        List<QuizResponse> responses = quizService.getQuizzesByCourse(request.getCourseId());
        return ResponseEntity.ok(responses);
    }

    // 内部请求类，用于传递试卷ID
    public static class QuizIdRequest {
        private Long quizId;

        public Long getQuizId() {
            return quizId;
        }

        public void setQuizId(Long quizId) {
            this.quizId = quizId;
        }
    }

    // 内部请求类，用于传递新标题
    public static class CopyQuizRequest {
        private String newTitle;

        public String getNewTitle() {
            return newTitle;
        }

        public void setNewTitle(String newTitle) {
            this.newTitle = newTitle;
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
} 