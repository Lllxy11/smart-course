package com.example.smartlearn.service.student;

import com.example.smartlearn.dto.request.QuizSubmissionDetailRequest;
import com.example.smartlearn.dto.request.QuizSubmissionListRequest;
import com.example.smartlearn.dto.request.QuizTaskListRequest;
import com.example.smartlearn.dto.request.SubmitQuizRequest;
import com.example.smartlearn.dto.response.*;
import com.example.smartlearn.exception.ResourceNotFoundException;
import com.example.smartlearn.exception.UnauthorizedException;
import com.example.smartlearn.model.*;
import com.example.smartlearn.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 该服务类用于学生端组卷相关的业务逻辑处理。
 * 包括学生查看试卷、获取答案等功能。
 */
@Service
@Transactional
public class StudentQuizService {

    private static final Logger log = LoggerFactory.getLogger(StudentQuizService.class);

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private StudentAnswerRepository studentAnswerRepository;

    @Autowired
    private StudentCourseRepository studentCourseRepository;

    /**
     * 获取学生测验任务列表
     * 该功能用于学生端测验列表查看功能
     */
    public QuizTaskListResponse getStudentQuizTasks(QuizTaskListRequest request) {
        // 验证学生是否存在
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("学生不存在"));

        // 1. 获取学生已选课程
        List<StudentCourse> studentCourses = studentCourseRepository.findByStudentStudentId(request.getStudentId());
        List<Long> courseIds = studentCourses.stream()
                .map(sc -> sc.getCourse().getCourseId())
                .collect(Collectors.toList());

        if (courseIds.isEmpty()) {
            // 学生没有选任何课程，返回空结果
            QuizTaskListResponse response = new QuizTaskListResponse();
            response.setTotal(0);
            response.setTotalPages(0);
            response.setCurrentPage(request.getPage());
            response.setQuizTasks(new ArrayList<>());
            return response;
        }

        // 2. 获取测验任务
        List<Task> quizTasks;
        if (request.getCourseId() != null) {
            // 验证学生是否有权限访问该课程
            if (!courseIds.contains(request.getCourseId())) {
                throw new UnauthorizedException("您没有权限访问此课程的测验");
            }
            // 按课程筛选
            quizTasks = taskRepository.findByCourseCourseIdAndType(request.getCourseId(), Task.TaskType.QUIZ);
        } else {
            // 所有已选课程的测验
            quizTasks = taskRepository.findByCourseCourseIdInAndType(courseIds, Task.TaskType.QUIZ);
        }

        // 3. 按状态筛选
        if (!"ALL".equals(request.getStatus())) {
            quizTasks = quizTasks.stream()
                    .filter(task -> {
                        List<Submission> submissions = submissionRepository.findByStudentStudentIdAndTaskId(
                                request.getStudentId(), task.getId());
                        String taskStatus = submissions.isEmpty() ? "NOT_STARTED" : "COMPLETED";
                        return taskStatus.equals(request.getStatus());
                    })
                    .collect(Collectors.toList());
        }

        // 4. 分页处理
        int total = quizTasks.size();
        int totalPages = (int) Math.ceil((double) total / request.getSize());
        int start = request.getPage() * request.getSize();
        int end = Math.min(start + request.getSize(), total);

        List<Task> pagedTasks = total > 0 ? quizTasks.subList(start, end) : new ArrayList<>();

        // 5. 转换为响应对象
        List<QuizTaskResponse> responses = pagedTasks.stream()
                .map(task -> convertToQuizTaskResponse(task, request.getStudentId()))
                .collect(Collectors.toList());

        // 6. 构建响应
        QuizTaskListResponse response = new QuizTaskListResponse();
        response.setTotal(total);
        response.setTotalPages(totalPages);
        response.setCurrentPage(request.getPage());
        response.setQuizTasks(responses);

        return response;
    }

    /**
     * 获取学生提交历史列表
     * 该功能用于学生端提交历史查看功能
     */
    public QuizSubmissionListResponse getStudentQuizSubmissions(QuizSubmissionListRequest request) {
        // 验证学生是否存在
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("学生不存在"));

        // 1. 获取学生已选课程
        List<StudentCourse> studentCourses = studentCourseRepository.findByStudentStudentId(request.getStudentId());
        List<Long> courseIds = studentCourses.stream()
                .map(sc -> sc.getCourse().getCourseId())
                .collect(Collectors.toList());

        if (courseIds.isEmpty()) {
            // 学生没有选任何课程，返回空结果
            QuizSubmissionListResponse response = new QuizSubmissionListResponse();
            response.setTotal(0);
            response.setTotalPages(0);
            response.setCurrentPage(request.getPage());
            response.setSubmissions(new ArrayList<>());
            return response;
        }

        // 2. 获取学生的提交记录
        List<Submission> submissions;
        if (request.getCourseId() != null) {
            // 验证学生是否有权限访问该课程
            if (!courseIds.contains(request.getCourseId())) {
                throw new UnauthorizedException("您没有权限访问此课程的提交记录");
            }
            // 按课程筛选提交记录
            submissions = submissionRepository.findByStudentStudentIdAndTaskCourseCourseId(
                    request.getStudentId(), request.getCourseId());
        } else {
            // 所有已选课程的提交记录
            submissions = submissionRepository.findByStudentStudentIdAndTaskCourseCourseIdIn(
                    request.getStudentId(), courseIds);
        }

        // 3. 只保留QUIZ类型的提交记录
        submissions = submissions.stream()
                .filter(submission -> submission.getTask().getType() == Task.TaskType.QUIZ)
                .collect(Collectors.toList());

        // 4. 分页处理
        int total = submissions.size();
        int totalPages = (int) Math.ceil((double) total / request.getSize());
        int start = request.getPage() * request.getSize();
        int end = Math.min(start + request.getSize(), total);

        List<Submission> pagedSubmissions = total > 0 ? submissions.subList(start, end) : new ArrayList<>();

        // 5. 转换为响应对象
        List<QuizSubmissionResponse> responses = pagedSubmissions.stream()
                .map(this::convertToQuizSubmissionResponse)
                .collect(Collectors.toList());

        // 6. 构建响应
        QuizSubmissionListResponse response = new QuizSubmissionListResponse();
        response.setTotal(total);
        response.setTotalPages(totalPages);
        response.setCurrentPage(request.getPage());
        response.setSubmissions(responses);

        return response;
    }

    /**
     * 获取提交详情
     * 该功能用于学生端提交详情查看功能
     */
    public SubmissionResponse getQuizSubmissionDetail(QuizSubmissionDetailRequest request) {
        try {
            log.info("=== 开始获取测验提交详情 ===");
            log.info("请求参数: submissionId={}, studentId={}",
                    request != null ? request.getSubmissionId() : "null",
                    request != null ? request.getStudentId() : "null");

            // 验证请求参数
            if (request == null) {
                log.error("请求对象为空");
                throw new IllegalArgumentException("请求参数不能为空");
            }

            if (request.getSubmissionId() == null) {
                log.error("提交ID为空");
                throw new IllegalArgumentException("提交ID不能为空");
            }

            if (request.getStudentId() == null) {
                log.error("学生ID为空");
                throw new IllegalArgumentException("学生ID不能为空");
            }

            log.info("步骤1: 验证学生是否存在，studentId: {}", request.getStudentId());
            // 验证学生是否存在
            Student student = studentRepository.findById(request.getStudentId())
                    .orElseThrow(() -> {
                        log.error("学生不存在，studentId: {}", request.getStudentId());
                        return new ResourceNotFoundException("学生不存在");
                    });
            log.info("学生验证通过: {}", student.getName());

            log.info("步骤2: 获取提交记录，submissionId: {}", request.getSubmissionId());
            // 获取提交记录
            Submission submission = submissionRepository.findById(request.getSubmissionId())
                    .orElseThrow(() -> {
                        log.error("提交记录不存在，submissionId: {}", request.getSubmissionId());
                        return new ResourceNotFoundException("提交记录不存在");
                    });
            log.info("找到提交记录，ID: {}", submission.getId());

            log.info("步骤3: 验证权限，提交记录的学生ID: {}, 请求的学生ID: {}",
                    submission.getStudent() != null ? submission.getStudent().getStudentId() : "null",
                    request.getStudentId());
            // 验证权限：只能查看自己的提交记录
            if (submission.getStudent() == null) {
                log.error("提交记录中的学生信息为空");
                throw new RuntimeException("数据异常：提交记录中的学生信息为空");
            }

            if (!submission.getStudent().getStudentId().equals(request.getStudentId())) {
                log.error("权限验证失败：提交记录学生ID={}, 请求学生ID={}",
                        submission.getStudent().getStudentId(), request.getStudentId());
                throw new UnauthorizedException("您没有权限查看此提交记录");
            }
            log.info("权限验证通过");

            log.info("步骤4: 验证任务类型，任务类型: {}",
                    submission.getTask() != null ? submission.getTask().getType() : "null");
            // 验证任务类型：只允许查看QUIZ类型的提交
            if (submission.getTask() == null) {
                log.error("提交记录中的任务信息为空");
                throw new RuntimeException("数据异常：提交记录中的任务信息为空");
            }

            if (submission.getTask().getType() != Task.TaskType.QUIZ) {
                log.error("任务类型验证失败，期望: QUIZ, 实际: {}", submission.getTask().getType());
                throw new UnauthorizedException("此提交记录不是测验类型");
            }
            log.info("任务类型验证通过");

            log.info("步骤5: 验证课程权限");
            // 验证课程权限：确保学生已选该课程
            List<StudentCourse> studentCourses = studentCourseRepository.findByStudentStudentId(request.getStudentId());
            log.info("学生选课数量: {}", studentCourses.size());

            List<Long> courseIds = studentCourses.stream()
                    .map(sc -> sc.getCourse().getCourseId())
                    .collect(Collectors.toList());
            log.info("学生选课ID列表: {}", courseIds);

            if (submission.getTask().getCourse() == null) {
                log.error("任务中的课程信息为空");
                throw new RuntimeException("数据异常：任务中的课程信息为空");
            }

            Long taskCourseId = submission.getTask().getCourse().getCourseId();
            log.info("任务所属课程ID: {}", taskCourseId);

            if (!courseIds.contains(taskCourseId)) {
                log.error("学生没有权限访问此课程的提交记录，courseId: {}", taskCourseId);
                throw new UnauthorizedException("您没有权限访问此课程的提交记录");
            }
            log.info("课程权限验证通过");

            log.info("步骤6: 开始构建响应对象");
            // 构建响应（复用现有的SubmissionResponse）
            SubmissionResponse response = new SubmissionResponse();
            response.setSubmissionId(submission.getId());
            log.debug("设置提交ID: {}", submission.getId());

            try {
                response.setQuizId(submission.getTask().getQuiz().getId());
                response.setQuizTitle(submission.getTask().getQuiz().getTitle());
                log.debug("设置试卷信息: ID={}, 标题={}",
                        submission.getTask().getQuiz().getId(),
                        submission.getTask().getQuiz().getTitle());
            } catch (Exception e) {
                log.error("获取试卷信息失败", e);
                throw new RuntimeException("获取试卷信息失败", e);
            }

            response.setStudentId(submission.getStudent().getStudentId());
            response.setStudentName(submission.getStudent().getName());
            response.setSubmittedAt(submission.getSubmittedAt());
            response.setTotalScore(submission.getGrade());

            try {
                response.setMaxScore(calculateMaxScore(submission.getTask().getQuiz()));
                log.debug("计算最大分数: {}", response.getMaxScore());
            } catch (Exception e) {
                log.error("计算最大分数失败", e);
                response.setMaxScore(BigDecimal.ZERO);
            }

            response.setStatus(submission.getGrade() != null ? "graded" : "submitted");
            response.setFeedback(submission.getFeedback());

            log.info("步骤7: 获取题目结果");
            // 获取题目结果
            List<SubmissionResponse.QuestionResult> results = new ArrayList<>();
            if (submission.getStudentAnswers() != null) {
                log.info("学生答案数量: {}", submission.getStudentAnswers().size());
                for (StudentAnswer answer : submission.getStudentAnswers()) {
                    try {
                        log.debug("处理答案，questionId: {}", answer.getQuestion().getId());

                        SubmissionResponse.QuestionResult result = new SubmissionResponse.QuestionResult();
                        result.setQuestionId(answer.getQuestion().getId());
                        result.setQuestionType(answer.getQuestion().getType().name());
                        result.setStudentAnswer(answer.getAnswerContent());
                        result.setIsCorrect(answer.getCorrect());
                        result.setScore(answer.getScore());

                        try {
                            // 获取题目分数，如果为null则使用默认分数
                            Integer questionScore = getQuestionScore(submission.getTask().getQuiz(), answer.getQuestion());
                            result.setMaxScore(new BigDecimal(questionScore));
                            log.debug("题目分数: {}", questionScore);
                        } catch (Exception e) {
                            log.error("获取题目分数失败，questionId: {}", answer.getQuestion().getId(), e);
                            result.setMaxScore(new BigDecimal(5)); // 默认分数
                        }

                        results.add(result);
                    } catch (Exception e) {
                        log.error("处理答案详情失败，answerId: {}", answer.getId(), e);
                        // 继续处理其他答案
                    }
                }
            } else {
                log.warn("提交记录中没有学生答案");
            }
            response.setResults(results);

            // 强制初始化所有需要用到的懒加载字段，防止LazyInitializationException
            submission.getTask().getId();
            submission.getTask().getQuiz().getId();
            submission.getTask().getQuiz().getTitle();
            submission.getStudent().getName();
            if (submission.getStudentAnswers() != null) {
                submission.getStudentAnswers().size();
                for (StudentAnswer answer : submission.getStudentAnswers()) {
                    answer.getQuestion().getId();
                    answer.getQuestion().getType();
                }
            }

            // 状态判断：有主观题未批改则为pending，否则有分数为graded，否则为submitted
            boolean hasUnmarkedSubjective = false;
            if (submission.getStudentAnswers() != null) {
                hasUnmarkedSubjective = submission.getStudentAnswers().stream()
                        .anyMatch(ans -> (ans.getQuestion().getType() == com.example.smartlearn.model.Question.QuestionType.SHORT_ANSWER)
                                && (ans.getScore() == null || ans.getScore().compareTo(BigDecimal.ZERO) == 0));
            }
            if (hasUnmarkedSubjective) {
                response.setStatus("pending");
            } else if (submission.getGrade() != null) {
                response.setStatus("graded");
            } else {
                response.setStatus("submitted");
            }

            log.info("=== 测验提交详情获取成功 ===");
            return response;

        } catch (Exception e) {
            log.error("获取测验提交详情失败", e);
            throw e;
        }
    }

    /**
     * 将Task转换为QuizTaskResponse
     */
    private QuizTaskResponse convertToQuizTaskResponse(Task task, Long studentId) {
        QuizTaskResponse response = new QuizTaskResponse();
        response.setTaskId(task.getId());
        response.setCourseId(task.getCourse().getCourseId());
        response.setCourseName(task.getCourse().getName());
        response.setQuizTitle(task.getTitle());
        response.setDueDate(task.getDueDate());

        // 获取试卷信息
        if (task.getQuiz() != null) {
            response.setTotalQuestions(task.getQuiz().getQuestions() != null ?
                    task.getQuiz().getQuestions().size() : 0);
            response.setTotalScore(task.getQuiz().getTotalPoints());
        } else {
            response.setTotalQuestions(0);
            response.setTotalScore(0);
        }

        // 检查是否已提交
        List<Submission> submissions = submissionRepository.findByStudentStudentIdAndTaskId(studentId, task.getId());
        if (!submissions.isEmpty()) {
            response.setStatus("COMPLETED");
            Submission submission = submissions.get(0);
            response.setSubmissionId(submission.getId());
            response.setSubmittedAt(submission.getSubmittedAt());
            response.setGrade(submission.getGrade());
        } else {
            response.setStatus("NOT_STARTED");
            response.setSubmissionId(null);
            response.setSubmittedAt(null);
            response.setGrade(null);
        }

        return response;
    }

    /**
     * 将Submission转换为QuizSubmissionResponse
     */
    private QuizSubmissionResponse convertToQuizSubmissionResponse(Submission submission) {
        QuizSubmissionResponse response = new QuizSubmissionResponse();
        response.setSubmissionId(submission.getId());
        response.setTaskId(submission.getTask().getId());
        response.setCourseId(submission.getTask().getCourse().getCourseId());
        response.setCourseName(submission.getTask().getCourse().getName());
        response.setQuizTitle(submission.getTask().getTitle());
        response.setSubmittedAt(submission.getSubmittedAt());
        response.setGrade(submission.getGrade());
        response.setMaxScore(calculateMaxScore(submission.getTask().getQuiz()));
        response.setFeedback(submission.getFeedback());
        response.setStatus(submission.getGrade() != null ? "graded" : "submitted");

        return response;
    }

    /**
     * 获取学生可访问的试卷列表
     * 该功能用于学生端试卷列表查看功能
     */
    public List<QuizResponse> getAvailableQuizzes(Long studentId) {
        // 验证学生是否存在
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("学生不存在"));

        // TODO: 这里需要根据实际的业务逻辑来确定学生可以访问哪些试卷
        // 可能需要通过学生选课关系、教师发布状态等来判断
        // 目前暂时返回所有试卷
        List<Quiz> quizzes = quizRepository.findAll();

        return quizzes.stream()
                .map(QuizResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * 获取试卷详情（不含答案）
     * 该功能用于学生端试卷详情查看功能
     */
    public QuizDetailResponse getQuizDetail(Long quizId, Long studentId) {
        // 验证学生是否存在
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("学生不存在"));

        // 获取试卷详情
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("试卷不存在"));

        // TODO: 这里需要验证学生是否有权限查看此试卷
        // 可能需要通过学生选课关系、教师发布状态等来判断

        return new QuizDetailResponse(quiz);
    }

    /**
     * 获取试卷答案
     * 该功能用于学生端试卷答案查看功能
     */
    public QuizAnswersResponse getQuizAnswers(Long quizId, Long studentId) {
        // 验证学生是否存在
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("学生不存在"));

        // 获取试卷详情
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("试卷不存在"));

        // TODO: 这里需要验证学生是否有权限查看此试卷的答案
        // 可能需要通过学生选课关系、教师发布状态等来判断

        // 创建答案响应对象
        QuizAnswersResponse response = new QuizAnswersResponse();
        response.setQuizId(quiz.getId());
        response.setQuizTitle(quiz.getTitle());

        // TODO: 从题目中提取答案信息
        // 这里需要根据实际的题目结构来实现
        // 可能需要解析题目body中的JSON格式来获取答案

        return response;
    }

    /**
     * 通过课程ID获取试卷列表
     * 该功能用于学生端按课程查看试卷
     */
    public List<QuizResponse> getQuizzesByCourse(Long courseId) {
        List<Quiz> quizzes = quizRepository.findAllByCourseCourseId(courseId);
        return quizzes.stream().map(QuizResponse::new).collect(Collectors.toList());
    }

    /**
     * 提交试卷答案
     * 该功能用于学生端试卷答案提交功能
     */
    public SubmissionResponse submitQuizAnswers(SubmitQuizRequest request) {
        try {
            // 第一步：验证数据
            Student student = studentRepository.findById(request.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("学生不存在"));

            Quiz quiz = quizRepository.findById(request.getQuizId())
                    .orElseThrow(() -> new ResourceNotFoundException("试卷不存在"));

            Task task = taskRepository.findById(request.getTaskId())
                    .orElseThrow(() -> new ResourceNotFoundException("任务不存在"));

            // 验证任务是否关联了正确的试卷
            if (task.getQuiz() == null || !task.getQuiz().getId().equals(request.getQuizId())) {
                throw new IllegalArgumentException("任务与试卷不匹配");
            }

            // 检查是否已经提交过
            List<Submission> existingSubmissions = submissionRepository.findByStudentStudentIdAndTaskId(
                    request.getStudentId(), request.getTaskId());
            if (!existingSubmissions.isEmpty()) {
                throw new IllegalArgumentException("您已经提交过此试卷，不能重复提交");
            }

            // 第二步：创建提交记录
            Submission submission = new Submission();
            submission.setStudent(student);
            submission.setTask(task);
            submission.setSubmittedAt(LocalDateTime.now());
            submission.setContent("试卷提交 - " + quiz.getTitle());

            // 保存提交记录，获得ID
            Submission savedSubmission = submissionRepository.save(submission);

            // 第三步：创建每道题的答案记录
            List<StudentAnswer> studentAnswers = new ArrayList<>();
            BigDecimal totalScore = BigDecimal.ZERO;
            List<SubmissionResponse.QuestionResult> results = new ArrayList<>();

            for (SubmitQuizRequest.QuestionAnswer answerReq : request.getAnswers()) {
                Question question = questionRepository.findById(answerReq.getQuestionId())
                        .orElseThrow(() -> new ResourceNotFoundException("题目不存在: " + answerReq.getQuestionId()));

                StudentAnswer answer = new StudentAnswer();
                answer.setSubmission(savedSubmission);
                answer.setQuestion(question);
                answer.setAnswerContent(answerReq.getAnswerContent());

                // 获取题目分数，默认为10分
                Integer questionScore = getQuestionScore(quiz, question);

                // 自动评分（客观题）
                if (question.getType() == Question.QuestionType.SINGLE_CHOICE ||
                        question.getType() == Question.QuestionType.MULTI_CHOICE) {

                    boolean isCorrect = checkAnswer(question, answerReq.getAnswerContent());
                    answer.setCorrect(isCorrect);

                    BigDecimal score = isCorrect ? new BigDecimal(questionScore) : BigDecimal.ZERO;
                    answer.setScore(score);
                    totalScore = totalScore.add(score);

                    // 创建结果对象
                    SubmissionResponse.QuestionResult result = new SubmissionResponse.QuestionResult();
                    result.setQuestionId(question.getId());
                    result.setQuestionType(question.getType().name());
                    result.setStudentAnswer(answerReq.getAnswerContent());
                    result.setCorrectAnswer(extractCorrectAnswer(question));
                    result.setIsCorrect(isCorrect);
                    result.setScore(score);
                    result.setMaxScore(new BigDecimal(questionScore));
                    results.add(result);
                } else {
                    // 主观题，等待教师评分
                    answer.setCorrect(null);
                    answer.setScore(BigDecimal.ZERO);

                    // 创建结果对象
                    SubmissionResponse.QuestionResult result = new SubmissionResponse.QuestionResult();
                    result.setQuestionId(question.getId());
                    result.setQuestionType(question.getType().name());
                    result.setStudentAnswer(answerReq.getAnswerContent());
                    result.setScore(BigDecimal.ZERO);
                    result.setMaxScore(new BigDecimal(questionScore));
                    result.setFeedback("等待教师评分");
                    results.add(result);
                }

                studentAnswers.add(answer);
            }

            // 批量保存所有答案
            studentAnswerRepository.saveAll(studentAnswers);

            // 第四步：更新提交记录的总分
            savedSubmission.setGrade(totalScore);
            submissionRepository.save(savedSubmission);

            // 第五步：构建响应
            SubmissionResponse response = new SubmissionResponse();
            response.setSubmissionId(savedSubmission.getId());
            response.setQuizId(quiz.getId());
            response.setQuizTitle(quiz.getTitle());
            response.setStudentId(student.getStudentId());
            response.setStudentName(student.getName());
            response.setSubmittedAt(savedSubmission.getSubmittedAt());
            response.setTotalScore(totalScore);
            response.setMaxScore(calculateMaxScore(quiz));
            response.setResults(results);

            return response;
        } catch (Exception e) {
            // 记录详细错误信息
            System.err.println("提交测验时发生错误: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("提交测验失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取题目分数，如果数据库中没有设置分数，则使用默认分数
     */
    private Integer getQuestionScore(Quiz quiz, Question question) {
        try {
            Integer score = quiz.getQuestionScore(question);
            if (score == null) {
                // 如果数据库中没有设置分数，根据题目类型设置默认分数
                switch (question.getType()) {
                    case SINGLE_CHOICE:
                        return 10;
                    case MULTI_CHOICE:
                        return 15;
                    case FILL_IN_BLANK:
                        return 8;
                    case SHORT_ANSWER:
                        return 20;
                    default:
                        return 10;
                }
            }
            return score;
        } catch (Exception e) {
            // 如果获取分数失败，返回默认分数
            return 10;
        }
    }

    /**
     * 检查答案是否正确（客观题）
     */
    private boolean checkAnswer(Question question, String studentAnswer) {
        try {
            if (studentAnswer == null) {
                return false;
            }

            String correctAnswer = extractCorrectAnswer(question);

            // 对于多选题，需要忽略选项顺序进行比较
            if (question.getType() == Question.QuestionType.MULTI_CHOICE) {
                return compareMultiChoiceAnswers(studentAnswer, correctAnswer);
            } else {
                // 单选题、填空题、简答题直接比较
                return studentAnswer.equals(correctAnswer);
            }
        } catch (Exception e) {
            // 如果解析失败，返回false
            return false;
        }
    }

    /**
     * 比较多选题答案（忽略选项顺序）
     */
    private boolean compareMultiChoiceAnswers(String studentAnswer, String correctAnswer) {
        try {
            // 将答案字符串转换为选项集合
            Set<String> studentOptions = parseAnswerToSet(studentAnswer);
            Set<String> correctOptions = parseAnswerToSet(correctAnswer);

            // 比较两个集合是否相等（忽略顺序）
            return studentOptions.equals(correctOptions);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 将答案字符串解析为选项集合
     * 支持多种格式：["A","B","C"] 或 A,B,C
     */
    private Set<String> parseAnswerToSet(String answer) {
        Set<String> options = new HashSet<>();

        if (answer == null || answer.trim().isEmpty()) {
            return options;
        }

        // 移除首尾空格
        answer = answer.trim();

        // 如果是JSON数组格式 ["A","B","C"]
        if (answer.startsWith("[") && answer.endsWith("]")) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonArray = objectMapper.readTree(answer);
                if (jsonArray.isArray()) {
                    for (JsonNode node : jsonArray) {
                        String option = node.asText().trim();
                        if (!option.isEmpty()) {
                            options.add(option);
                        }
                    }
                }
            } catch (Exception e) {
                // JSON解析失败，尝试作为普通字符串处理
                String cleaned = answer.replaceAll("[\\[\\]\"]", "");
                String[] parts = cleaned.split(",");
                for (String part : parts) {
                    String option = part.trim();
                    if (!option.isEmpty()) {
                        options.add(option);
                    }
                }
            }
        } else {
            // 普通逗号分隔格式：A,B,C
            String[] parts = answer.split(",");
            for (String part : parts) {
                String option = part.trim();
                if (!option.isEmpty()) {
                    options.add(option);
                }
            }
        }

        return options;
    }

    /**
     * 从题目中提取正确答案
     */
    private String extractCorrectAnswer(Question question) {
        try {
            if (question.getBody() == null || question.getBody().trim().isEmpty()) {
                return "A"; // 默认值
            }

            // 解析JSON格式的题目body
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(question.getBody());

            // 根据题目类型提取答案
            if (question.getType() == Question.QuestionType.SINGLE_CHOICE) {
                return jsonNode.get("answer").asText();
            } else if (question.getType() == Question.QuestionType.MULTI_CHOICE) {
                JsonNode answerArray = jsonNode.get("answer");
                if (answerArray.isArray()) {
                    StringBuilder sb = new StringBuilder();
                    for (JsonNode answer : answerArray) {
                        if (sb.length() > 0) sb.append(",");
                        sb.append(answer.asText());
                    }
                    return sb.toString();
                }
                return answerArray.asText();
            } else {
                // 填空题和简答题
                return jsonNode.get("answer").asText();
            }
        } catch (Exception e) {
            // 如果JSON解析失败，返回默认值
            return "A";
        }
    }

    /**
     * 计算试卷满分
     */
    private BigDecimal calculateMaxScore(Quiz quiz) {
        BigDecimal maxScore = BigDecimal.ZERO;
        List<Question> questions = quiz.getQuestions();
        if (questions != null) {
            for (Question question : questions) {
                Integer score = quiz.getQuestionScore(question);
                if (score != null) {
                    maxScore = maxScore.add(new BigDecimal(score));
                } else {
                    maxScore = maxScore.add(new BigDecimal(10)); // 默认10分
                }
            }
        }
        return maxScore;
    }

    /**
     * 通过taskId获取测验详情（不含答案）
     */
    public QuizDetailResponse getQuizDetailByTaskId(Long taskId, Long studentId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("任务不存在"));
        if (task.getQuiz() == null) {
            throw new ResourceNotFoundException("该任务未关联测验");
        }
        return getQuizDetail(task.getQuiz().getId(), studentId);
    }

    public Optional<Submission> findSubmissionByStudentIdAndTaskId(Long studentId, Long taskId) {
        List<Submission> list = submissionRepository.findByStudentStudentIdAndTaskId(studentId, taskId);
        if (list != null && !list.isEmpty()) {
            return Optional.of(list.get(0));
        } else {
            return Optional.empty();
        }
    }
} 