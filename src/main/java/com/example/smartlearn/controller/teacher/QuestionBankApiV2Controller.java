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
 * 该控制器类用于题库管理的安全接口风格补全（全部POST+请求体）。
 * 新增V2接口，所有参数均从请求体获取，业务逻辑完整调用原有Service。
 */
@RestController
@RequestMapping("/api/teacher/question-bank-v2")
@CrossOrigin(origins = "*")
public class QuestionBankApiV2Controller {
    @Autowired
    private QuestionBankService questionBankService;

    /** 创建题目 */
    @PostMapping("/create")
    public ResponseEntity<QuestionResponse> create(@RequestBody CreateRequest request) {
        QuestionResponse response = questionBankService.createQuestion(request.data, request.teacherId);
        return ResponseEntity.ok(response);
    }

    /** 筛选题目 */
    @PostMapping("/search")
    public ResponseEntity<Page<QuestionResponse>> search(@RequestBody SearchRequest request) {
        Page<QuestionResponse> responses = questionBankService.getQuestions(request.data, request.teacherId);
        return ResponseEntity.ok(responses);
    }

    /** 获取题目详情 */
    @PostMapping("/detail")
    public ResponseEntity<QuestionResponse> detail(@RequestBody IdRequest request) {
        QuestionResponse response = questionBankService.getQuestionById(request.id, request.teacherId);
        return ResponseEntity.ok(response);
    }

    /** 编辑题目 */
    @PostMapping("/update")
    public ResponseEntity<QuestionResponse> update(@RequestBody UpdateRequest request) {
        QuestionResponse response = questionBankService.updateQuestion(request.id, request.data, request.teacherId);
        return ResponseEntity.ok(response);
    }

    /** 删除题目 */
    @PostMapping("/delete")
    public ResponseEntity<Void> delete(@RequestBody IdRequest request) {
        questionBankService.deleteQuestion(request.id, request.teacherId);
        return ResponseEntity.ok().build();
    }

    /** 批量删除题目 */
    @PostMapping("/batch-delete")
    public ResponseEntity<Void> batchDelete(@RequestBody IdsRequest request) {
        questionBankService.deleteQuestions(request.ids, request.teacherId);
        return ResponseEntity.ok().build();
    }

    /** 按课程获取题目 */
    @PostMapping("/by-course")
    public ResponseEntity<List<QuestionResponse>> byCourse(@RequestBody ByCourseRequest request) {
        List<QuestionResponse> responses = questionBankService.getQuestionsByCourse(request.courseId, request.teacherId);
        return ResponseEntity.ok(responses);
    }

    /** 关键词搜索题目 */
    @PostMapping("/search-by-keyword")
    public ResponseEntity<List<QuestionResponse>> searchByKeyword(@RequestBody KeywordRequest request) {
        List<QuestionResponse> responses = questionBankService.searchQuestionsByKeyword(request.keyword, request.teacherId);
        return ResponseEntity.ok(responses);
    }

    // 内部请求体
    public static class CreateRequest { public QuestionRequest data; public Long teacherId; }
    public static class SearchRequest { public QuestionFilterRequest data; public Long teacherId; }
    public static class IdRequest { public Long id; public Long teacherId; }
    public static class UpdateRequest { public Long id; public QuestionRequest data; public Long teacherId; }
    public static class IdsRequest { public List<Long> ids; public Long teacherId; }
    public static class ByCourseRequest { public Long courseId; public Long teacherId; }
    public static class KeywordRequest { public String keyword; public Long teacherId; }
} 