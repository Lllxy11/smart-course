package com.example.smartlearn.service.student;

import com.example.smartlearn.dto.response.TaskDetailResponse;
import com.example.smartlearn.dto.response.TaskResponse;
import com.example.smartlearn.dto.response.Task_ResourceResponse;
import com.example.smartlearn.exception.ResourceNotFoundException;
import com.example.smartlearn.exception.SecurityException;
import com.example.smartlearn.model.Submission;
import com.example.smartlearn.model.Task;
import com.example.smartlearn.model.Task_Resource;
import com.example.smartlearn.repository.StudentCourseRepository;
import com.example.smartlearn.repository.SubmissionRepository;
import com.example.smartlearn.repository.TaskRepository;
import com.example.smartlearn.repository.Task_ResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskStudentService {

    private final TaskRepository taskRepository;
    private final StudentCourseRepository studentCourseRepository;
    private final SubmissionRepository submissionRepository;
    private final Task_ResourceRepository taskResourceRepository;

    @Autowired
    public TaskStudentService(TaskRepository taskRepository,
                              StudentCourseRepository studentCourseRepository,
                              SubmissionRepository submissionRepository,
                              Task_ResourceRepository taskResourceRepository) {
        this.taskRepository = taskRepository;
        this.studentCourseRepository = studentCourseRepository;
        this.submissionRepository = submissionRepository;
        this.taskResourceRepository = taskResourceRepository;
    }

    /**
     * 获取学生任务列表（带筛选条件）
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getStudentTasks(Long studentId,
                                              String courseCode,
                                              String courseName,
                                              String term,
                                              String taskType,
                                              Boolean submitted) {

        // 获取学生已选课程的所有任务
        List<Task> tasks = taskRepository.findByCourseCourseIdIn(
                studentCourseRepository.findByStudentStudentId(studentId)
                        .stream()
                        .map(sc -> sc.getCourse().getCourseId())
                        .collect(Collectors.toList())
        );

        return tasks.stream()
                .filter(task -> applyFilters(task, courseCode, courseName, term, taskType))
                .map(task -> convertToTaskResponse(task, studentId))
                .filter(response -> filterBySubmittedStatus(response, submitted))
                .collect(Collectors.toList());
    }

    /**
     * 获取任务详情（学生视角）
     */
    @Transactional(readOnly = true)
    public TaskDetailResponse getTaskDetailForStudent(Long studentId, Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("任务不存在"));

        // 验证学生是否属于该课程
        validateStudentCourseAccess(studentId, task.getCourse().getCourseId());

        TaskDetailResponse response = new TaskDetailResponse();
        populateTaskDetailResponse(task, studentId, response);
        populateTaskResources(taskId, response);

        // 新增：仅homework和report类型任务展示评分和评语
        if (task.getType() == Task.TaskType.HOMEWORK || task.getType() == Task.TaskType.REPORT) {
            Submission submission = submissionRepository.findByStudentStudentIdAndTaskId(studentId, task.getId())
                    .stream().findFirst().orElse(null);
            if (submission != null) {
                response.setGrade(submission.getGrade());
                response.setFeedback(submission.getFeedback());
            }
        }

        return response;
    }

    // ============ 私有方法 ============

    private boolean applyFilters(Task task, String courseCode, String courseName,
                                 String term, String taskType) {
        // 课程编号筛选
        if (courseCode != null && !task.getCourse().getCode().contains(courseCode)) {
            return false;
        }
        // 课程名称筛选
        if (courseName != null && !task.getCourse().getName().contains(courseName)) {
            return false;
        }
        // 学期筛选
        if (term != null && !task.getCourse().getTerm().equals(term)) {
            return false;
        }
        // 任务类型筛选
        return taskType == null || task.getType().name().equals(taskType);
    }

    private TaskResponse convertToTaskResponse(Task task, Long studentId) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setCourseId(task.getCourse().getCourseId());
        response.setCourseName(task.getCourse().getName());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setType(task.getType().name());
        response.setDueDate(task.getDueDate());
        response.setCreatedAt(task.getCreatedAt());

        // 设置是否过期
        response.setExpired(isTaskExpired(task));

        // 设置是否提交
        response.setSubmitted(isTaskSubmitted(task.getId(), studentId));

        return response;
    }

    private boolean filterBySubmittedStatus(TaskResponse response, Boolean submitted) {
        return submitted == null || response.isSubmitted() == submitted;
    }

    private boolean isTaskExpired(Task task) {
        return task.getDueDate() != null && task.getDueDate().isBefore(LocalDateTime.now());
    }

    private boolean isTaskSubmitted(Long taskId, Long studentId) {
        return submissionRepository.existsByTaskIdAndStudentStudentId(taskId, studentId);
    }

    private void validateStudentCourseAccess(Long studentId, Long courseId) {
        if (!studentCourseRepository.existsByStudentStudentIdAndCourseCourseId(
                studentId, courseId)) {
            throw new SecurityException("无权访问此任务");
        }
    }

    private void populateTaskDetailResponse(Task task, Long studentId, TaskDetailResponse response) {
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setType(task.getType().name());
        response.setDueDate(task.getDueDate());
        response.setExpired(isTaskExpired(task));
        response.setSubmitted(isTaskSubmitted(task.getId(), studentId));
    }

    private void populateTaskResources(Long taskId, TaskDetailResponse response) {
        List<Task_Resource> resources = taskResourceRepository.findByTaskId(taskId);
        response.setResources(resources.stream()
                .map(this::convertToResourceResponse)
                .collect(Collectors.toList()));
    }

    private Task_ResourceResponse convertToResourceResponse(Task_Resource resource) {
        Task_ResourceResponse response = new Task_ResourceResponse();
        response.setId(resource.getId());
        response.setName(resource.getName());
        response.setFileType(resource.getFileType());
        response.setFilePath(resource.getFilePath());
        response.setUploadDate(resource.getUploadDate());
        return response;
    }
}
