package com.example.smartlearn.service.teacher;

import com.example.smartlearn.dto.request.TaskRequest;
import com.example.smartlearn.dto.response.QuizDetailResponse;
import com.example.smartlearn.dto.response.QuizResponse;
import com.example.smartlearn.dto.response.TaskResponse;
import com.example.smartlearn.exception.ResourceNotFoundException;
import com.example.smartlearn.model.Course;
import com.example.smartlearn.model.Quiz;
import com.example.smartlearn.model.Task;
import com.example.smartlearn.repository.CourseRepository;
import com.example.smartlearn.repository.QuizRepository;
import com.example.smartlearn.repository.TaskRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final CourseRepository courseRepository;
    private final QuizRepository quizRepository;

    private TaskResponse convertToResponse(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setCourseId(task.getCourse().getCourseId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setType(task.getType().name());
        response.setQuizId(task.getQuiz() != null ? task.getQuiz().getId() : null);
        response.setDueDate(task.getDueDate());
        response.setCreatedAt(task.getCreatedAt());
        return response;
    }

    @Autowired
    public TaskService(TaskRepository taskRepository, CourseRepository courseRepository, QuizRepository quizRepository) {
        this.taskRepository = taskRepository;
        this.courseRepository = courseRepository;
        this.quizRepository = quizRepository;
    }

    @Transactional
    public TaskResponse createTask(TaskRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在：" + request.getCourseId()));

        Task task = new Task();
        task.setCourse(course);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setType(Task.TaskType.valueOf(request.getType()));

        if (request.getQuizId() != null) {
            Quiz quiz = quizRepository.findById(request.getQuizId())
                    .orElseThrow(() -> new ResourceNotFoundException("试卷不存在"));
            
            // 验证试卷是否属于指定课程
            if (quiz.getCourse() == null || !quiz.getCourse().getCourseId().equals(request.getCourseId())) {
                throw new IllegalArgumentException("试卷不属于指定课程");
            }
            
            task.setQuiz(quiz);
        }

        task.setDueDate(request.getDueDate());

        Task saved = taskRepository.save(task);

        TaskResponse response = convertToResponse(saved);

        // 如果任务类型是QUIZ且有关联的试卷，则获取试卷详情
        if (saved.getType() == Task.TaskType.QUIZ && saved.getQuiz() != null) {
            QuizDetailResponse quizDetail = new QuizDetailResponse(saved.getQuiz());
            response.setQuizDetail(quizDetail);
        }

        return response;
    }

//    public List<TaskResponse> getTasksByCourse(Long courseId) {
//        return taskRepository.findByCourseCourseId(courseId).stream().map(task -> {
//            TaskResponse response = new TaskResponse();
//            response.setId(task.getId());
//            response.setCourseId(courseId);
//            response.setTitle(task.getTitle());
//            response.setDescription(task.getDescription());
//            response.setType(task.getType().name());
//            response.setDueDate(task.getDueDate());
//            response.setCreatedAt(task.getCreatedAt());
//            return response;
//        }).collect(Collectors.toList());
//    }
// 修改：添加教师权限验证
public List<TaskResponse> getTasksByCourse(Long courseId, Long teacherId) {
    // 验证教师是否有权限访问该课程
    Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("课程不存在：" + courseId));

    if (course.getTeacherId() == null || !course.getTeacherId().equals(teacherId)) {
        throw new SecurityException("无权访问此课程的任务");
    }

    return taskRepository.findByCourseCourseId(courseId).stream().map(task -> {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setCourseId(courseId);
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setType(task.getType().name());
        response.setQuizId(task.getQuiz() != null ? task.getQuiz().getId() : null);
        response.setDueDate(task.getDueDate());
        response.setCreatedAt(task.getCreatedAt());
        
        // 新增：如果任务类型是QUIZ且有关联的试卷，则获取试卷详情
        if (task.getType() == Task.TaskType.QUIZ && task.getQuiz() != null) {
            QuizDetailResponse quizDetail = new QuizDetailResponse(task.getQuiz());
            response.setQuizDetail(quizDetail);
        }
        
        return response;
    }).collect(Collectors.toList());
}

    @Transactional
    public void deleteTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("任务不存在"));
        taskRepository.delete(task);
    }

    /**
     * 获取教师可用的试卷列表
     * 用于创建QUIZ任务时选择试卷
     */
    public List<QuizResponse> getAvailableQuizzes(Long teacherId, Long courseId) {
        // 验证课程是否存在且教师有权限
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在：" + courseId));

        if (course.getTeacherId() == null || !course.getTeacherId().equals(teacherId)) {
            throw new SecurityException("无权访问此课程的试卷");
        }

        List<Quiz> quizzes;
        
        if (courseId != null) {
            // 获取该课程下的试卷，并且创建者是当前教师
            quizzes = quizRepository.findAllByCourseCourseId(courseId).stream()
                    .filter(quiz -> quiz.getCreator().getTeacherId().equals(teacherId))
                    .collect(Collectors.toList());
        } else {
            // 获取教师创建的所有试卷
            quizzes = quizRepository.findByCreatorTeacherId(teacherId);
        }
        
        return quizzes.stream()
                .map(QuizResponse::new)
                .collect(Collectors.toList());
    }
}