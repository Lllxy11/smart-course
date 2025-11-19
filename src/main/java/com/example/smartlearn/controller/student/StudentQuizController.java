package com.example.smartlearn.controller.student;

import com.example.smartlearn.dto.request.QuizSubmissionDetailRequest;
import com.example.smartlearn.dto.request.QuizSubmissionListRequest;
import com.example.smartlearn.dto.request.QuizTaskListRequest;
import com.example.smartlearn.dto.request.SubmitQuizRequest;
import com.example.smartlearn.dto.response.*;
import com.example.smartlearn.service.student.StudentQuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 该控制器类用于学生端组卷相关的API接口。
 * 提供学生查看试卷、获取答案等功能。
 */
@RestController
@RequestMapping("/api/student")
public class StudentQuizController {

    @Autowired
    private StudentQuizService studentQuizService;

    /**
     * 获取学生可访问的试卷列表
     * 该功能用于学生端试卷列表查看功能
     */
    @GetMapping("/quiz/quizzes")
    public ResponseEntity<List<QuizResponse>> getStudentQuizzes(@RequestParam Long studentId) {
        List<QuizResponse> quizzes = studentQuizService.getAvailableQuizzes(studentId);
        return ResponseEntity.ok(quizzes);
    }

    /**
     * 获取试卷详情（不含答案）
     * 该功能用于学生端试卷详情查看功能
     */
    @PostMapping("/quiz/quizzes/detail")
    public ResponseEntity<QuizDetailResponse> getQuizDetail(@RequestBody QuizIdRequest request, @RequestParam Long studentId) {
        QuizDetailResponse detail = studentQuizService.getQuizDetail(request.getQuizId(), studentId);
        return ResponseEntity.ok(detail);
    }

    /**
     * 按课程获取试卷列表
     * 该功能用于学生端按课程查看试卷功能
     */
    @PostMapping("/quiz/quizzes/by-course")
    public ResponseEntity<List<QuizResponse>> getQuizzesByCourse(@RequestBody CourseIdRequest request) {
        List<QuizResponse> quizzes = studentQuizService.getQuizzesByCourse(request.getCourseId());
        return ResponseEntity.ok(quizzes);
    }

    /**
     * 获取试卷答案
     * 该功能用于学生端查看试卷答案功能
     */
    @PostMapping("/quiz/quizzes/answers")
    public ResponseEntity<QuizAnswersResponse> getQuizAnswers(@RequestBody QuizIdRequest request, @RequestParam Long studentId) {
        QuizAnswersResponse answers = studentQuizService.getQuizAnswers(request.getQuizId(), studentId);
        return ResponseEntity.ok(answers);
    }

    /**
     * 提交试卷答案
     * 该功能用于学生端提交试卷答案功能
     */
    @PostMapping("/quiz/submit")
    public ResponseEntity<SubmissionResponse> submitQuiz(@RequestBody SubmitQuizRequest request) {
        SubmissionResponse response = studentQuizService.submitQuizAnswers(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取学生测验任务列表
     * 该功能用于学生端课堂测验模块的测验列表查看功能
     */
    @PostMapping("/quiz/tasks")
    public ResponseEntity<QuizTaskListResponse> getStudentQuizTasks(@RequestBody QuizTaskListRequest request) {
        QuizTaskListResponse response = studentQuizService.getStudentQuizTasks(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取学生提交历史列表
     * 该功能用于学生端课堂测验模块的提交历史查看功能
     */
    @PostMapping("/quiz/submissions")
    public ResponseEntity<QuizSubmissionListResponse> getStudentQuizSubmissions(@RequestBody QuizSubmissionListRequest request) {
        QuizSubmissionListResponse response = studentQuizService.getStudentQuizSubmissions(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取提交详情
     * 该功能用于学生端课堂测验模块的提交详情查看功能
     */
    @PostMapping("/quiz/submission/detail")
    public ResponseEntity<SubmissionResponse> getQuizSubmissionDetail(@RequestBody QuizSubmissionDetailRequest request) {
        SubmissionResponse response = studentQuizService.getQuizSubmissionDetail(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 通过taskId获取测验详情（不含答案）
     * 该功能用于学生端通过任务直接查看测验详情
     */
    @PostMapping("/quiz/quizzes/detail-by-task")
    public ResponseEntity<QuizDetailResponse> getQuizDetailByTask(@RequestBody TaskIdRequest request, @RequestParam Long studentId) {
        QuizDetailResponse detail = studentQuizService.getQuizDetailByTaskId(request.getTaskId(), studentId);
        return ResponseEntity.ok(detail);
    }

    /**
     * 查询某个学生某个任务是否完成
     */
    @GetMapping("/student/{studentId}/task/{taskId}/status")
    public ResponseEntity<Map<String, Object>> getTaskCompletionStatus(@PathVariable Long studentId, @PathVariable Long taskId) {
        // 直接用repository查找是否有提交
        Optional<com.example.smartlearn.model.Submission> submissionOpt = studentQuizService.findSubmissionByStudentIdAndTaskId(studentId, taskId);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("studentId", studentId);
        result.put("taskId", taskId);
        if (submissionOpt.isPresent()) {
            com.example.smartlearn.model.Submission submission = submissionOpt.get();
            result.put("completed", true);
            result.put("submissionId", submission.getId());
            result.put("submittedAt", submission.getSubmittedAt());
        } else {
            result.put("completed", false);
            result.put("submissionId", null);
            result.put("submittedAt", null);
        }
        return ResponseEntity.ok(result);
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

    // 新增：内部请求类，用于传递任务ID
    public static class TaskIdRequest {
        private Long taskId;

        public Long getTaskId() {
            return taskId;
        }

        public void setTaskId(Long taskId) {
            this.taskId = taskId;
        }
    }
} 