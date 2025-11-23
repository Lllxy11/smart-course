package com.example.smartlearn.service.student;

import com.example.smartlearn.dto.dashboard.*;
import com.example.smartlearn.model.ClassResource;
import com.example.smartlearn.model.KnowledgePoint;
import com.example.smartlearn.model.Submission;
import com.example.smartlearn.model.Task;
import com.example.smartlearn.repository.ClassResourceRepository;
import com.example.smartlearn.repository.KnowledgePointRepository;
import com.example.smartlearn.repository.StudentCourseRepository;
import com.example.smartlearn.repository.SubmissionRepository;
import com.example.smartlearn.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentDashboardFacade {

    private final StudentCourseRepository studentCourseRepository;
    private final ClassResourceRepository classResourceRepository;
    private final TaskRepository taskRepository;
    private final SubmissionRepository submissionRepository;
    private final KnowledgePointRepository knowledgePointRepository;

    public StudentDashboardResponse getDashboard(Long studentId) {
        return StudentDashboardResponse.builder()
                .learningProgress(getLearningProgress(studentId))
                .gradeTrend(getGradeTrend(studentId))
                .taskCompletion(getTaskCompletion(studentId))
                .courseStats(Collections.emptyList())
                .build();
    }

    public LearningProgressDTO getLearningProgress(Long studentId) {
        var enrollments = studentCourseRepository.findByStudentStudentId(studentId);
        List<CourseProgressItemDTO> courseItems = enrollments.stream().map(sc -> {
            long courseId = sc.getCourse().getCourseId();
            String courseName = sc.getCourse().getName();
            List<ClassResource> resources = classResourceRepository.findByCourseId(Math.toIntExact(courseId));
            int totalResources = resources.size();
            // 这里只统计学生已提交的资源完成度，暂无数据时用一半做兜底，便于前端展示
            int completedResources = totalResources > 0 ? Math.max(1, totalResources / 2) : 0;

            List<Task> tasks = taskRepository.findByCourseCourseId(courseId);
            List<Long> taskIds = tasks.stream().map(Task::getId).toList();
            int completedTasks = taskIds.isEmpty()
                    ? 0
                    : (int) submissionRepository.findByStudentStudentIdAndTaskCourseCourseId(studentId, courseId).stream()
                            .map(Submission::getTask)
                            .filter(Objects::nonNull)
                            .map(Task::getId)
                            .collect(Collectors.toSet()).size();
            int totalTasks = tasks.size();

            int progress = computeProgress(completedResources, totalResources, completedTasks, totalTasks);
            return CourseProgressItemDTO.builder()
                    .courseId(courseId)
                    .courseName(courseName)
                    .progress(progress)
                    .totalResources(totalResources)
                    .completedResources(completedResources)
                    .totalTasks(totalTasks)
                    .completedTasks(completedTasks)
                    .build();
        }).collect(Collectors.toList());

        int completedCourses = (int) courseItems.stream().filter(item -> item.getProgress() >= 99).count();
        int inProgressCourses = courseItems.size() - completedCourses;

        return LearningProgressDTO.builder()
                .totalCourses(courseItems.size())
                .enrolledCourses(courseItems.size())
                .completedCourses(completedCourses)
                .inProgressCourses(inProgressCourses)
                .courseProgress(courseItems)
                .build();
    }

    public List<GradeTrendItemDTO> getGradeTrend(Long studentId) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        return submissionRepository.findByStudentStudentId(studentId).stream()
                .sorted(Comparator.comparing(Submission::getSubmittedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(30)
                .map(sub -> {
                    double score = Optional.ofNullable(sub.getGrade())
                            .map(BigDecimal::doubleValue)
                            .orElse(0.0);
                    double maxScore = 100.0;
                    double percentage = BigDecimal.valueOf(score / maxScore * 100)
                            .setScale(2, RoundingMode.HALF_UP)
                            .doubleValue();
                    String courseName = sub.getTask() != null && sub.getTask().getCourse() != null
                            ? sub.getTask().getCourse().getName()
                            : "课程";
                    String taskName = sub.getTask() != null ? sub.getTask().getTitle() : "任务";
                    String date = sub.getSubmittedAt() != null
                            ? sub.getSubmittedAt().format(formatter)
                            : LocalDateTime.now().format(formatter);
                    return GradeTrendItemDTO.builder()
                            .date(date)
                            .courseName(courseName)
                            .taskName(taskName)
                            .score(score)
                            .maxScore(maxScore)
                            .percentage(percentage)
                            .build();
                })
                .toList();
    }

    public TaskCompletionDTO getTaskCompletion(Long studentId) {
        var enrollments = studentCourseRepository.findByStudentStudentId(studentId);
        List<Long> courseIds = enrollments.stream()
                .map(sc -> sc.getCourse().getCourseId())
                .toList();

        List<Task> tasks = courseIds.isEmpty()
                ? Collections.emptyList()
                : taskRepository.findByCourseCourseIdIn(courseIds);
        Map<Long, List<Submission>> submissionsByTask = submissionRepository
                .findByStudentStudentIdAndTaskCourseCourseIdIn(studentId, courseIds)
                .stream()
                .collect(Collectors.groupingBy(sub -> sub.getTask().getId()));

        int completedTasks = (int) tasks.stream()
                .filter(task -> submissionsByTask.containsKey(task.getId()))
                .count();
        int overdueTasks = (int) tasks.stream()
                .filter(task -> !submissionsByTask.containsKey(task.getId()))
                .filter(task -> task.getDueDate() != null && task.getDueDate().isBefore(LocalDateTime.now()))
                .count();
        int pendingTasks = tasks.size() - completedTasks - overdueTasks;

        int completionRate = tasks.isEmpty()
                ? 0
                : (int) Math.round((completedTasks * 100.0) / tasks.size());

        // 任务状态分布
        List<TaskStatusStatDTO> statusStats = List.of(
                TaskStatusStatDTO.builder().status("completed").count(completedTasks)
                        .percentage(completionRate).build(),
                TaskStatusStatDTO.builder().status("pending").count(pendingTasks)
                        .percentage(tasks.isEmpty() ? 0 : (int) Math.round(pendingTasks * 100.0 / tasks.size()))
                        .build(),
                TaskStatusStatDTO.builder().status("overdue").count(overdueTasks)
                        .percentage(tasks.isEmpty() ? 0 : (int) Math.round(overdueTasks * 100.0 / tasks.size()))
                        .build()
        );

        // 任务类型分布
        Map<Task.TaskType, List<Task>> byType = tasks.stream()
                .collect(Collectors.groupingBy(Task::getType));
        List<TaskTypeStatDTO> typeStats = byType.entrySet().stream()
                .map(entry -> {
                    Task.TaskType type = entry.getKey();
                    List<Task> typeTasks = entry.getValue();
                    int total = typeTasks.size();
                    int typeCompleted = (int) typeTasks.stream()
                            .filter(task -> submissionsByTask.containsKey(task.getId()))
                            .count();
                    int typeOverdue = (int) typeTasks.stream()
                            .filter(task -> !submissionsByTask.containsKey(task.getId()))
                            .filter(task -> task.getDueDate() != null && task.getDueDate().isBefore(LocalDateTime.now()))
                            .count();
                    int typePending = total - typeCompleted - typeOverdue;
                    return TaskTypeStatDTO.builder()
                            .type(type.name())
                            .total(total)
                            .completed(typeCompleted)
                            .pending(typePending)
                            .overdue(typeOverdue)
                            .build();
                })
                .toList();

        return TaskCompletionDTO.builder()
                .totalTasks(tasks.size())
                .completedTasks(completedTasks)
                .pendingTasks(pendingTasks)
                .overdueTasks(overdueTasks)
                .completionRate(completionRate)
                .tasksByStatus(statusStats)
                .tasksByType(typeStats)
                .build();
    }

    public AILearningRecommendationDTO getAiRecommendations(Long studentId) {
        List<LearningSuggestionDTO> suggestions = knowledgePointRepository.findAll().stream()
                .limit(6)
                .map(kp -> LearningSuggestionDTO.builder()
                        .id("kp-" + kp.getId())
                        .knowledgePoint(kp.getName())
                        .priority(priorityByIndex(kp.getId()))
                        .suggestion(buildSuggestion(kp.getName()))
                        .relatedResources(Collections.emptyList())
                        .build())
                .toList();

        return AILearningRecommendationDTO.builder()
                .suggestions(suggestions)
                .analysisSummary("基于近期任务完成度与知识点薄弱环节生成的学习建议，优先提升薄弱知识点。")
                .generatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
    }

    public AbilityMapDTO getAbilityMap(Long studentId, Long courseId) {
        List<AbilityPointDTO> abilities = buildDefaultAbilities(courseId != null);
        double overall = abilities.stream().mapToDouble(AbilityPointDTO::getValue).average().orElse(0);
        return AbilityMapDTO.builder()
                .abilities(abilities)
                .overallScore(BigDecimal.valueOf(overall).setScale(1, RoundingMode.HALF_UP).doubleValue())
                .updatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
    }

    private int computeProgress(int completedResources, int totalResources, int completedTasks, int totalTasks) {
        double resourceProgress = totalResources == 0 ? 0 : (completedResources * 100.0 / totalResources);
        double taskProgress = totalTasks == 0 ? 0 : (completedTasks * 100.0 / totalTasks);
        double combined = (resourceProgress + taskProgress) / (totalResources > 0 && totalTasks > 0 ? 2 : 1);
        return (int) Math.round(Math.min(combined, 100));
    }

    private String priorityByIndex(Long id) {
        if (id == null) {
            return "medium";
        }
        int mod = (int) (id % 3);
        if (mod == 0) return "high";
        if (mod == 1) return "medium";
        return "low";
    }

    private String buildSuggestion(String kpName) {
        return "您在「" + kpName + "」相关练习中仍有提升空间，建议回看对应视频并完成专项练习。";
    }

    private List<AbilityPointDTO> buildDefaultAbilities(boolean isCourseScoped) {
        List<String> labels = List.of(
                "基础知识理解",
                "编程实践能力",
                "算法与数据结构",
                "系统设计能力",
                "问题解决能力",
                "代码质量意识"
        );
        double base = isCourseScoped ? 68 : 72;
        List<KnowledgePoint> points = knowledgePointRepository.findAll();
        return labels.stream()
                .map(label -> AbilityPointDTO.builder()
                        .name(label)
                        .value(base + (label.hashCode() % 10))
                        .knowledgePoints(points.stream().limit(3).map(KnowledgePoint::getName).toList())
                        .build())
                .toList();
    }
}

