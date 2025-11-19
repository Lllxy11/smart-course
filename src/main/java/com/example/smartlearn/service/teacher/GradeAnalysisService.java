package com.example.smartlearn.service.teacher;

import com.example.smartlearn.dto.response.CourseGradeReportResponse;
import com.example.smartlearn.dto.response.GradeAnalysisResponse;
import com.example.smartlearn.model.*;
import com.example.smartlearn.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 成绩分析服务
 * 基于现有表结构，无需创建新表
 */
@Service
@Transactional
public class GradeAnalysisService {

    @Autowired
    private StudentCourseRepository studentCourseRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    /**
     * 获取学生成绩分析
     */
    public GradeAnalysisResponse getStudentGradeAnalysis(Long studentId, Long courseId) {
        // 获取学生和课程信息
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("学生不存在"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在"));

        // 获取该课程的所有任务
        List<Task> tasks = taskRepository.findByCourseCourseId(courseId);

        // 获取学生的所有提交记录
        List<Submission> submissions = submissionRepository.findByStudentStudentIdAndTaskCourseCourseId(studentId, courseId);

        // 构建成绩分析响应
        GradeAnalysisResponse response = new GradeAnalysisResponse();
        response.setStudentId(studentId);
        response.setStudentName(student.getStudentName());
        response.setCourseId(courseId);
        response.setCourseName(course.getName());

        // 优先使用 student_course.final_grade 字段
        BigDecimal finalGrade = getStudentFinalGrade(studentId, courseId);
        if (finalGrade == null) {
            finalGrade = calculateTotalGrade(submissions, tasks);
        }
        if (finalGrade == null) {
            finalGrade = BigDecimal.ZERO;
        }
        response.setCurrentGrade(finalGrade);
        response.setMaxGrade(BigDecimal.valueOf(100));

        // 计算成绩百分比
        response.setGradePercentage(finalGrade.doubleValue());
        response.setGradeLevel(getGradeLevel(finalGrade.doubleValue()));

        // 计算班级排名
        calculateClassRank(response, courseId, finalGrade);

        // 重新构建成绩趋势 gradeTrend
        List<GradeAnalysisResponse.GradeTrendData> gradeTrend = new ArrayList<>();
        for (Task task : tasks) {
            BigDecimal taskMaxScore = getTaskMaxScore(task);
            // 查找学生的提交记录
            Optional<Submission> submission = submissions.stream()
                    .filter(s -> s.getTask().getId().equals(task.getId()))
                    .findFirst();
            if (submission.isPresent() && submission.get().getGrade() != null) {
                BigDecimal score = submission.get().getGrade();
                GradeAnalysisResponse.GradeTrendData trendData = new GradeAnalysisResponse.GradeTrendData();
                trendData.setDate(submission.get().getSubmittedAt());
                trendData.setTaskTitle(task.getTitle());
                trendData.setScore(score);
                trendData.setMaxScore(taskMaxScore);
                trendData.setTaskType(task.getType().name());
                trendData.setPercentage(score.divide(taskMaxScore, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue());
                gradeTrend.add(trendData);
            }
        }
        gradeTrend.sort(Comparator.comparing(GradeAnalysisResponse.GradeTrendData::getDate));
        response.setGradeTrend(gradeTrend);

        // 计算各任务类型成绩分布
        response.setTaskTypeGrades(calculateTaskTypeGrades(submissions, tasks));

        // 计算统计信息
        response.setStatistics(calculateGradeStatistics(submissions, tasks));

        // 生成学习建议
        response.setLearningSuggestions(generateLearningSuggestions(response));

        // 设置主响应体的完成度
        response.setCompletionRate(response.getStatistics() != null ? response.getStatistics().getCompletionRate() : null);

        return response;
    }

    /**
     * 获取课程成绩报表
     */
    public CourseGradeReportResponse getCourseGradeReport(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在"));

        // 获取课程的所有学生
        List<StudentCourse> studentCourses = studentCourseRepository.findByCourseCourseId(courseId, Pageable.unpaged()).getContent();
        List<Task> tasks = taskRepository.findByCourseCourseId(courseId);

        CourseGradeReportResponse response = new CourseGradeReportResponse();
        response.setCourseId(courseId);
        response.setCourseName(course.getName());
        response.setCourseCode(course.getCode());
        response.setTeacherName("教师"); // 由于Course模型只有teacherId，这里暂时使用固定值
        response.setGeneratedAt(LocalDateTime.now());

        // 构建学生成绩列表
        List<CourseGradeReportResponse.StudentGradeInfo> studentGrades = new ArrayList<>();
        List<BigDecimal> allGrades = new ArrayList<>();

        for (StudentCourse studentCourse : studentCourses) {
            Student student = studentCourse.getStudent();
            CourseGradeReportResponse.StudentGradeInfo studentGrade = new CourseGradeReportResponse.StudentGradeInfo();
            studentGrade.setStudentId(student.getStudentId());
            studentGrade.setStudentName(student.getStudentName());
            studentGrade.setStudentNumber(student.getStudentId().toString());

            // 优先使用student_course表中的final_grade，如果没有则计算
            BigDecimal finalGrade = getStudentFinalGrade(student.getStudentId(), courseId);
            if (finalGrade == null) {
                // 计算学生总成绩（100分制）
                BigDecimal totalGrade = BigDecimal.ZERO;
                BigDecimal maxGrade = BigDecimal.ZERO;
                int completedTasks = 0;

                for (Task task : tasks) {
                    BigDecimal taskMaxScore = getTaskMaxScore(task);
                    maxGrade = maxGrade.add(taskMaxScore);

                    Optional<Submission> submission = submissionRepository
                            .findByStudentStudentIdAndTaskId(student.getStudentId(), task.getId())
                            .stream().findFirst();

                    if (submission.isPresent() && submission.get().getGrade() != null) {
                        BigDecimal score = submission.get().getGrade();
                        totalGrade = totalGrade.add(score);
                        completedTasks++;
                    }
                }

                // 转换为100分制
                if (maxGrade.compareTo(BigDecimal.ZERO) > 0) {
                    finalGrade = totalGrade.divide(maxGrade, 2, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));
                } else {
                    finalGrade = BigDecimal.ZERO;
                }
            }

            // 获取任务详情
            int completedTasks = 0;
            List<CourseGradeReportResponse.TaskGradeDetail> taskGrades = new ArrayList<>();

            for (Task task : tasks) {
                BigDecimal taskMaxScore = getTaskMaxScore(task);

                Optional<Submission> submission = submissionRepository
                        .findByStudentStudentIdAndTaskId(student.getStudentId(), task.getId())
                        .stream().findFirst();

                CourseGradeReportResponse.TaskGradeDetail taskGrade = new CourseGradeReportResponse.TaskGradeDetail();
                taskGrade.setTaskId(task.getId());
                taskGrade.setTaskTitle(task.getTitle());
                taskGrade.setTaskType(task.getType().name());
                taskGrade.setDueDate(task.getDueDate());
                taskGrade.setMaxScore(taskMaxScore);

                if (submission.isPresent()) {
                    taskGrade.setSubmittedAt(submission.get().getSubmittedAt());
                    if (submission.get().getGrade() != null) {
                        BigDecimal score = submission.get().getGrade();
                        taskGrade.setScore(score);
                        taskGrade.setPercentage(score.divide(taskMaxScore, 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100)).doubleValue());
                        completedTasks++;
                        taskGrade.setStatus("completed");
                    } else {
                        taskGrade.setStatus("submitted");
                    }
                } else {
                    taskGrade.setStatus("not_submitted");
                }
                taskGrades.add(taskGrade);
            }

            studentGrade.setTotalGrade(finalGrade);
            studentGrade.setMaxGrade(BigDecimal.valueOf(100));
            studentGrade.setCompletedTasks(completedTasks);
            studentGrade.setTotalTasks(tasks.size());
            studentGrade.setTaskGrades(taskGrades);

            studentGrade.setGradePercentage(finalGrade.doubleValue());
            studentGrade.setGradeLevel(getGradeLevel(finalGrade.doubleValue()));
            studentGrade.setCompletionRate((double) completedTasks / tasks.size() * 100);
            allGrades.add(finalGrade);

            studentGrades.add(studentGrade);
        }

        // 计算排名
        allGrades.sort(Collections.reverseOrder());
        for (CourseGradeReportResponse.StudentGradeInfo studentGrade : studentGrades) {
            int rank = allGrades.indexOf(studentGrade.getTotalGrade()) + 1;
            studentGrade.setRank(rank);
        }

        response.setStudentGrades(studentGrades);

        // 计算课程统计信息
        response.setCourseStatistics(calculateCourseStatistics(studentGrades, tasks));

        // 计算任务统计信息
        response.setTaskStatistics(calculateTaskStatistics(tasks, studentCourses));

        return response;
    }

    /**
     * 获取任务最大分数
     */
    private BigDecimal getTaskMaxScore(Task task) {
        if (task.getType() == Task.TaskType.QUIZ && task.getQuiz() != null) {
            return BigDecimal.valueOf(task.getQuiz().getTotalPoints());
        } else {
            // 默认作业满分100分
            return BigDecimal.valueOf(100);
        }
    }

    /**
     * 获取学生在课程中的最终成绩（优先从student_course表获取）
     */
    private BigDecimal getStudentFinalGrade(Long studentId, Long courseId) {
        try {
            Optional<StudentCourse> studentCourse = studentCourseRepository
                    .findByCourseIdAndStudentId(courseId, studentId);

            if (studentCourse.isPresent() && studentCourse.get().getFinalGrade() != null
                    && !studentCourse.get().getFinalGrade().trim().isEmpty()) {
                return new BigDecimal(studentCourse.get().getFinalGrade().trim());
            }
        } catch (Exception e) {
            // 如果转换失败，返回null，使用计算方式
            System.out.println("转换finalGrade失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 计算课程总成绩（100分制）
     */
    private BigDecimal calculateTotalGrade(List<Submission> submissions, List<Task> tasks) {
        if (tasks.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalScore = BigDecimal.ZERO;
        BigDecimal totalMaxScore = BigDecimal.ZERO;

        for (Task task : tasks) {
            BigDecimal taskMaxScore = getTaskMaxScore(task);
            totalMaxScore = totalMaxScore.add(taskMaxScore);

            // 查找学生的提交记录
            Optional<Submission> submission = submissions.stream()
                    .filter(s -> s.getTask().getId().equals(task.getId()))
                    .findFirst();

            if (submission.isPresent() && submission.get().getGrade() != null) {
                totalScore = totalScore.add(submission.get().getGrade());
            }
        }

        // 转换为100分制
        if (totalMaxScore.compareTo(BigDecimal.ZERO) > 0) {
            return totalScore.divide(totalMaxScore, 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        return BigDecimal.ZERO;
    }

    /**
     * 计算班级排名
     */
    private void calculateClassRank(GradeAnalysisResponse response, Long courseId, BigDecimal totalScore) {
        List<StudentCourse> studentCourses = studentCourseRepository.findByCourseCourseId(courseId, Pageable.unpaged()).getContent();
        List<BigDecimal> allGrades = new ArrayList<>();

        for (StudentCourse studentCourse : studentCourses) {
            List<Task> tasks = taskRepository.findByCourseCourseId(courseId);
            List<Submission> studentSubmissions = submissionRepository.findByStudentStudentIdAndTaskCourseCourseId(
                    studentCourse.getStudent().getStudentId(), courseId);

            // 使用100分制计算学生成绩
            BigDecimal studentGrade = calculateTotalGrade(studentSubmissions, tasks);
            allGrades.add(studentGrade);
        }

        allGrades.sort(Collections.reverseOrder());
        int rank = allGrades.indexOf(totalScore) + 1;

        response.setClassRank(rank);
        response.setTotalStudents(studentCourses.size());
    }

    /**
     * 计算各任务类型成绩分布
     */
    private Map<String, GradeAnalysisResponse.TaskTypeGrade> calculateTaskTypeGrades(
            List<Submission> submissions, List<Task> tasks) {
        Map<String, List<Submission>> typeSubmissions = submissions.stream()
                .collect(Collectors.groupingBy(s -> s.getTask().getType().name()));

        Map<String, GradeAnalysisResponse.TaskTypeGrade> result = new HashMap<>();

        for (Map.Entry<String, List<Submission>> entry : typeSubmissions.entrySet()) {
            String taskType = entry.getKey();
            List<Submission> typeSubs = entry.getValue();

            GradeAnalysisResponse.TaskTypeGrade taskTypeGrade = new GradeAnalysisResponse.TaskTypeGrade();
            taskTypeGrade.setTaskType(taskType);
            taskTypeGrade.setTaskCount(typeSubs.size());

            BigDecimal totalScore = BigDecimal.ZERO;
            BigDecimal maxScore = BigDecimal.ZERO;
            int completedCount = 0;

            for (Submission submission : typeSubs) {
                BigDecimal taskMaxScore = getTaskMaxScore(submission.getTask());
                maxScore = maxScore.add(taskMaxScore);

                if (submission.getGrade() != null) {
                    totalScore = totalScore.add(submission.getGrade());
                    completedCount++;
                }
            }

            taskTypeGrade.setMaxScore(maxScore);
            if (completedCount > 0) {
                taskTypeGrade.setAverageScore(totalScore.divide(BigDecimal.valueOf(completedCount), 2, RoundingMode.HALF_UP));
            }
            taskTypeGrade.setCompletionRate((double) completedCount / typeSubs.size() * 100);

            result.put(taskType, taskTypeGrade);
        }

        return result;
    }

    /**
     * 计算成绩统计信息
     */
    private GradeAnalysisResponse.GradeStatistics calculateGradeStatistics(
            List<Submission> submissions, List<Task> tasks) {
        GradeAnalysisResponse.GradeStatistics statistics = new GradeAnalysisResponse.GradeStatistics();

        List<BigDecimal> grades = submissions.stream()
                .filter(s -> s.getGrade() != null)
                .map(Submission::getGrade)
                .collect(Collectors.toList());

        if (!grades.isEmpty()) {
            BigDecimal sum = grades.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            statistics.setAverageGrade(sum.divide(BigDecimal.valueOf(grades.size()), 2, RoundingMode.HALF_UP));
            statistics.setHighestGrade(Collections.max(grades));
            statistics.setLowestGrade(Collections.min(grades));

            // 计算标准差
            BigDecimal mean = statistics.getAverageGrade();
            BigDecimal variance = grades.stream()
                    .map(grade -> grade.subtract(mean).pow(2))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(grades.size()), 4, RoundingMode.HALF_UP);
            statistics.setStandardDeviation(BigDecimal.valueOf(Math.sqrt(variance.doubleValue())));
        }

        statistics.setCompletedTasks(grades.size());
        statistics.setTotalTasks(tasks.size());
        statistics.setCompletionRate((double) grades.size() / tasks.size() * 100);

        // 计算成绩分布
        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("优秀", 0);
        distribution.put("良好", 0);
        distribution.put("中等", 0);
        distribution.put("及格", 0);
        distribution.put("不及格", 0);

        for (BigDecimal grade : grades) {
            // 修复：避免 getGrade() 为 null 导致 NPE
            Optional<Submission> submissionOpt = submissions.stream()
                    .filter(s -> s.getGrade() != null && s.getGrade().equals(grade))
                    .findFirst();
            if (submissionOpt.isPresent()) {
                Submission submission = submissionOpt.get();
                BigDecimal taskMaxScore = getTaskMaxScore(submission.getTask());
                double percentage = grade.divide(taskMaxScore, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue();

                if (percentage >= 90) distribution.put("优秀", distribution.get("优秀") + 1);
                else if (percentage >= 80) distribution.put("良好", distribution.get("良好") + 1);
                else if (percentage >= 70) distribution.put("中等", distribution.get("中等") + 1);
                else if (percentage >= 60) distribution.put("及格", distribution.get("及格") + 1);
                else distribution.put("不及格", distribution.get("不及格") + 1);
            }
        }

        statistics.setGradeDistribution(distribution);

        return statistics;
    }

    /**
     * 生成学习建议
     */
    private List<String> generateLearningSuggestions(GradeAnalysisResponse analysis) {
        List<String> suggestions = new ArrayList<>();

        if (analysis.getGradePercentage() < 60) {
            suggestions.add("您的成绩偏低，建议加强基础知识的学习");
            suggestions.add("可以寻求教师或同学的帮助，及时解决学习中的疑问");
        } else if (analysis.getGradePercentage() < 80) {
            suggestions.add("您的成绩良好，但还有提升空间");
            suggestions.add("建议多做练习，巩固已学知识");
        } else {
            suggestions.add("您的成绩优秀，继续保持！");
            suggestions.add("可以尝试挑战更高难度的题目");
        }

        if (analysis.getStatistics().getCompletionRate() < 80) {
            suggestions.add("任务完成率偏低，建议按时完成所有作业");
        }

        // 根据任务类型成绩分布给出建议
        if (analysis.getTaskTypeGrades() != null) {
            for (Map.Entry<String, GradeAnalysisResponse.TaskTypeGrade> entry : analysis.getTaskTypeGrades().entrySet()) {
                String taskType = entry.getKey();
                GradeAnalysisResponse.TaskTypeGrade grade = entry.getValue();

                if (grade.getAverageScore() != null && grade.getMaxScore() != null) {
                    double percentage = grade.getAverageScore().divide(grade.getMaxScore(), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue();

                    if (percentage < 70) {
                        suggestions.add("在" + getTaskTypeName(taskType) + "方面需要加强练习");
                    }
                }
            }
        }

        return suggestions;
    }

    /**
     * 获取成绩等级
     */
    private String getGradeLevel(double percentage) {
        if (percentage >= 90) return "优秀";
        else if (percentage >= 80) return "良好";
        else if (percentage >= 70) return "中等";
        else if (percentage >= 60) return "及格";
        else return "不及格";
    }

    /**
     * 获取任务类型中文名称
     */
    private String getTaskTypeName(String taskType) {
        switch (taskType) {
            case "QUIZ": return "测验";
            case "ASSIGNMENT": return "作业";
            case "PROJECT": return "项目";
            default: return taskType;
        }
    }

    /**
     * 计算课程统计信息
     */
    private CourseGradeReportResponse.CourseStatistics calculateCourseStatistics(
            List<CourseGradeReportResponse.StudentGradeInfo> studentGrades, List<Task> tasks) {
        CourseGradeReportResponse.CourseStatistics statistics = new CourseGradeReportResponse.CourseStatistics();

        statistics.setTotalStudents(studentGrades.size());
        statistics.setEnrolledStudents(studentGrades.size());

        List<BigDecimal> grades = studentGrades.stream()
                .map(CourseGradeReportResponse.StudentGradeInfo::getTotalGrade)
                .collect(Collectors.toList());

        if (!grades.isEmpty()) {
            BigDecimal sum = grades.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            statistics.setClassAverage(sum.divide(BigDecimal.valueOf(grades.size()), 2, RoundingMode.HALF_UP));
            statistics.setHighestGrade(Collections.max(grades));
            statistics.setLowestGrade(Collections.min(grades));

            // 计算标准差
            BigDecimal mean = statistics.getClassAverage();
            BigDecimal variance = grades.stream()
                    .map(grade -> grade.subtract(mean).pow(2))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(grades.size()), 4, RoundingMode.HALF_UP);
            statistics.setStandardDeviation(BigDecimal.valueOf(Math.sqrt(variance.doubleValue())));
        }

        // 计算成绩分布
        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("优秀", 0);
        distribution.put("良好", 0);
        distribution.put("中等", 0);
        distribution.put("及格", 0);
        distribution.put("不及格", 0);

        for (CourseGradeReportResponse.StudentGradeInfo studentGrade : studentGrades) {
            if (studentGrade.getGradeLevel() != null) {
                distribution.put(studentGrade.getGradeLevel(), distribution.get(studentGrade.getGradeLevel()) + 1);
            }
        }

        statistics.setGradeDistribution(distribution);

        return statistics;
    }

    /**
     * 计算任务统计信息
     */
    private List<CourseGradeReportResponse.TaskStatistics> calculateTaskStatistics(
            List<Task> tasks, List<StudentCourse> studentCourses) {
        List<CourseGradeReportResponse.TaskStatistics> taskStatistics = new ArrayList<>();

        for (Task task : tasks) {
            CourseGradeReportResponse.TaskStatistics statistics = new CourseGradeReportResponse.TaskStatistics();
            statistics.setTaskId(task.getId());
            statistics.setTaskTitle(task.getTitle());
            statistics.setTaskType(task.getType().name());
            statistics.setDueDate(task.getDueDate());
            statistics.setTotalStudents(studentCourses.size());
            statistics.setMaxScore(getTaskMaxScore(task));

            List<Submission> taskSubmissions = new ArrayList<>();
            for (StudentCourse studentCourse : studentCourses) {
                List<Submission> submissions = submissionRepository
                        .findByStudentStudentIdAndTaskId(studentCourse.getStudent().getStudentId(), task.getId());
                taskSubmissions.addAll(submissions);
            }

            statistics.setSubmittedStudents(taskSubmissions.size());

            List<BigDecimal> scores = taskSubmissions.stream()
                    .filter(s -> s.getGrade() != null)
                    .map(Submission::getGrade)
                    .collect(Collectors.toList());

            statistics.setCompletedStudents(scores.size());

            if (!scores.isEmpty()) {
                BigDecimal sum = scores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                statistics.setAverageScore(sum.divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP));

                BigDecimal maxScore = getTaskMaxScore(task);
                double averagePercentage = sum.divide(BigDecimal.valueOf(scores.size()), 4, RoundingMode.HALF_UP)
                        .divide(maxScore, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue();
                statistics.setAveragePercentage(averagePercentage);
            }

            statistics.setCompletionRate((double) taskSubmissions.size() / studentCourses.size() * 100);

            taskStatistics.add(statistics);
        }

        return taskStatistics;
    }
} 