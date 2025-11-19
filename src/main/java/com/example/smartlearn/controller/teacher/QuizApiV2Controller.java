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
 * 该控制器类用于组卷管理的安全接口风格补全（全部POST+请求体）。
 * 新增V2接口，所有参数均从请求体获取，业务逻辑完整调用原有Service。
 */
@RestController
@RequestMapping("/api/teacher/quiz-v2")
@CrossOrigin(origins = "*")
public class QuizApiV2Controller {
    @Autowired
    private QuizService quizService;

    /** 创建试卷 */
    @PostMapping("/create")
    public ResponseEntity<QuizResponse> create(@RequestBody CreateRequest request) {
        QuizResponse response = quizService.createQuiz(request.data);
        return ResponseEntity.ok(response);
    }

    /** 获取试卷详情 */
    @PostMapping("/detail")
    public ResponseEntity<QuizDetailResponse> detail(@RequestBody IdWithTeacherRequest request) {
        QuizDetailResponse response = quizService.getQuizById(request.quizId, request.teacherId);
        return ResponseEntity.ok(response);
    }

    /** 编辑试卷 */
    @PostMapping("/update")
    public ResponseEntity<QuizResponse> update(@RequestBody UpdateRequest request) {
        QuizResponse response = quizService.updateQuiz(request.quizId, request.data, request.teacherId);
        return ResponseEntity.ok(response);
    }

    /** 删除试卷 */
    @PostMapping("/delete")
    public ResponseEntity<Void> delete(@RequestBody IdWithTeacherRequest request) {
        quizService.deleteQuiz(request.quizId, request.teacherId);
        return ResponseEntity.ok().build();
    }

    /** 获取教师试卷列表 */
    @PostMapping("/list-by-teacher")
    public ResponseEntity<List<QuizResponse>> listByTeacher(@RequestBody TeacherIdRequest request) {
        List<QuizResponse> responses = quizService.getQuizzesByTeacher(request.teacherId);
        return ResponseEntity.ok(responses);
    }

    /** 向试卷添加题目 */
    @PostMapping("/add-question")
    public ResponseEntity<Void> addQuestion(@RequestBody AddQuestionRequest request) {
        quizService.addQuestionToQuiz(request.quizId, request.data, request.teacherId);
        return ResponseEntity.ok().build();
    }

    /** 从试卷移除题目 */
    @PostMapping("/remove-question")
    public ResponseEntity<Void> removeQuestion(@RequestBody RemoveQuestionRequest request) {
        quizService.removeQuestionFromQuiz(request.quizId, request.questionId, request.teacherId);
        return ResponseEntity.ok().build();
    }

    /** 设置题目分数和顺序 */
    @PostMapping("/set-question-settings")
    public ResponseEntity<Void> setQuestionSettings(@RequestBody AddQuestionRequest request) {
        quizService.setQuestionSettings(request.quizId, request.data, request.teacherId);
        return ResponseEntity.ok().build();
    }

    /** 复制试卷 */
    @PostMapping("/copy")
    public ResponseEntity<QuizResponse> copy(@RequestBody CopyRequest request) {
        QuizResponse response = quizService.copyQuiz(request.quizId, request.newTitle, request.teacherId);
        return ResponseEntity.ok(response);
    }

    /** 通过课程ID获取试卷列表 */
    @PostMapping("/by-course")
    public ResponseEntity<List<QuizResponse>> byCourse(@RequestBody ByCourseRequest request) {
        List<QuizResponse> responses = quizService.getQuizzesByCourse(request.courseId);
        return ResponseEntity.ok(responses);
    }

    // 内部请求体
    public static class CreateRequest { public QuizRequest data; }
    public static class UpdateRequest { public Long quizId; public QuizRequest data; public Long teacherId; }
    public static class IdWithTeacherRequest { public Long quizId; public Long teacherId; }
    public static class TeacherIdRequest { public Long teacherId; }
    public static class AddQuestionRequest { public Long quizId; public QuizQuestionRequest data; public Long teacherId; }
    public static class RemoveQuestionRequest { public Long quizId; public Long questionId; public Long teacherId; }
    public static class CopyRequest { public Long quizId; public String newTitle; public Long teacherId; }
    public static class ByCourseRequest { public Long courseId; }
} 