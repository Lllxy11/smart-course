package com.example.smartlearn.controller.teacher;

import com.example.smartlearn.dto.request.TaskRequest;
import com.example.smartlearn.dto.response.QuizResponse;
import com.example.smartlearn.dto.response.TaskResponse;
import com.example.smartlearn.service.teacher.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks") // 修改：移除 courseId 占位符，因为我们从 request 中获取
public class TaskController {

    @Autowired
    private TaskService taskService;

    // 创建任务
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@RequestBody TaskRequest request) {
        System.out.println("Task created");
        TaskResponse task = taskService.createTask(request); // 只传 request
        return ResponseEntity.ok(task);
    }

    // 获取某课程下的所有任务
//    @GetMapping("/course/{courseId}")
//    public ResponseEntity<List<TaskResponse>> getTasksByCourse(@PathVariable Long courseId) {
//        return ResponseEntity.ok(taskService.getTasksByCourse(courseId));
//    }
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<TaskResponse>> getTasksByCourse(
            @PathVariable Long courseId,
            @RequestParam Long teacherId) {
        try {
            return ResponseEntity.ok(taskService.getTasksByCourse(courseId, teacherId));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build(); // 返回403 Forbidden
        }
    }


    // 删除任务
    @DeleteMapping("/{taskId}")
    public ResponseEntity<?> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    // 获取教师可用的试卷列表（用于创建QUIZ任务时选择）
    @GetMapping("/available-quizzes")
    public ResponseEntity<List<QuizResponse>> getAvailableQuizzes(
            @RequestParam Long teacherId,
            @RequestParam(required = false) Long courseId) {
        return ResponseEntity.ok(taskService.getAvailableQuizzes(teacherId, courseId));
    }

    // 获取指定课程下的试卷列表（用于创建QUIZ任务时选择）
    @GetMapping("/course/{courseId}/quizzes")
    public ResponseEntity<List<QuizResponse>> getQuizzesByCourse(
            @PathVariable Long courseId,
            @RequestParam Long teacherId) {
        try {
            return ResponseEntity.ok(taskService.getAvailableQuizzes(teacherId, courseId));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build(); // 返回403 Forbidden
        }
    }
}
