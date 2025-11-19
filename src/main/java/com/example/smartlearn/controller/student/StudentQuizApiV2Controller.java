package com.example.smartlearn.controller.student;

import com.example.smartlearn.dto.response.QuizAnswersResponse;
import com.example.smartlearn.dto.response.QuizDetailResponse;
import com.example.smartlearn.dto.response.QuizResponse;
import com.example.smartlearn.service.student.StudentQuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 该控制器类用于学生端试卷相关功能的安全接口风格补全（全部POST+请求体）。
 * 新增V2接口，所有参数均从请求体获取，业务逻辑完整调用原有Service。
 */
@RestController
@RequestMapping("/api/student/quiz-v2")
@CrossOrigin(origins = "*")
public class StudentQuizApiV2Controller {
    @Autowired
    private StudentQuizService studentQuizService;

    /** 获取学生试卷列表 */
    @PostMapping("/list")
    public ResponseEntity<List<QuizResponse>> list(@RequestBody StudentIdRequest request) {
        List<QuizResponse> responses = studentQuizService.getAvailableQuizzes(request.studentId);
        return ResponseEntity.ok(responses);
    }

    /** 获取试卷详情 */
    @PostMapping("/detail")
    public ResponseEntity<QuizDetailResponse> detail(@RequestBody QuizIdRequest request) {
        QuizDetailResponse response = studentQuizService.getQuizDetail(request.quizId, request.studentId);
        return ResponseEntity.ok(response);
    }

    /** 获取学生试卷作答详情 */
    @PostMapping("/answers-detail")
    public ResponseEntity<QuizAnswersResponse> answersDetail(@RequestBody QuizIdRequest request) {
        QuizAnswersResponse response = studentQuizService.getQuizAnswers(request.quizId, request.studentId);
        return ResponseEntity.ok(response);
    }

    // /** 提交试卷答案（Service未实现，仅预留） */
    // @PostMapping("/submit-answers")
    // public ResponseEntity<Void> submitAnswers(@RequestBody SubmitAnswersRequest request) {
    //     // studentQuizService.submitQuizAnswers(request.quizId, request.studentId, request.answers);
    //     return ResponseEntity.ok().build();
    // }

    // 内部请求体
    public static class StudentIdRequest { public Long studentId; }
    public static class QuizIdRequest { public Long quizId; public Long studentId; }
    public static class SubmitAnswersRequest { public Long quizId; public Long studentId; public List<QuizAnswersResponse.QuestionAnswer> answers; }
} 