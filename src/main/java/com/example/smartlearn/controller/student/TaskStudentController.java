package com.example.smartlearn.controller.student;

import com.example.smartlearn.dto.request.SubmissionRequest;
import com.example.smartlearn.dto.response.TaskDetailResponse;
import com.example.smartlearn.dto.response.TaskResponse;
import com.example.smartlearn.service.student.SubmissionService;
import com.example.smartlearn.service.student.TaskStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/student/{studentId}/tasks")
public class TaskStudentController {

    private final TaskStudentService taskStudentService;
    private final SubmissionService submissionService;

    @Autowired
    public TaskStudentController(TaskStudentService taskStudentService,
                                 SubmissionService submissionService) {
        this.taskStudentService = taskStudentService;
        this.submissionService = submissionService;
    }

    /**
     * 获取学生任务列表
     */
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getTasks(
            @PathVariable Long studentId,
            @RequestParam(required = false) String courseCode,
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) String term,
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) Boolean submitted) {

        List<TaskResponse> tasks = taskStudentService.getStudentTasks(
                studentId, courseCode, courseName, term, taskType, submitted);

        return ResponseEntity.ok(tasks);
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/{taskId}")
    public ResponseEntity<TaskDetailResponse> getTaskDetail(
            @PathVariable Long studentId,
            @PathVariable Long taskId) {

        TaskDetailResponse task = taskStudentService.getTaskDetailForStudent(studentId, taskId);
        return ResponseEntity.ok(task);
    }

    /**
     * 提交任务
     */
    // 修改后的提交接口
    @PostMapping("/{taskId}/submit")
    public ResponseEntity<?> submitTask(
            @PathVariable Long studentId,
            @PathVariable Long taskId,
            @ModelAttribute SubmissionRequest request) throws IOException {

        request.setStudentId(studentId);
        request.setTaskId(taskId); // 确保taskId来自路径而非表单
        submissionService.submitTask(request);
        return ResponseEntity.ok().build();
    }
}
