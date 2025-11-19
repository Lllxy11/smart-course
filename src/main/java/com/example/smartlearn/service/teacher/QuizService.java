package com.example.smartlearn.service.teacher;

import com.example.smartlearn.dto.request.QuizQuestionRequest;
import com.example.smartlearn.dto.request.QuizRequest;
import com.example.smartlearn.dto.response.QuizDetailResponse;
import com.example.smartlearn.dto.response.QuizResponse;
import com.example.smartlearn.exception.ResourceNotFoundException;
import com.example.smartlearn.model.Course;
import com.example.smartlearn.model.Question;
import com.example.smartlearn.model.Quiz;
import com.example.smartlearn.model.Teacher;
import com.example.smartlearn.repository.CourseRepository;
import com.example.smartlearn.repository.QuestionRepository;
import com.example.smartlearn.repository.QuizRepository;
import com.example.smartlearn.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 该服务类用于组卷管理的业务逻辑处理。
 * 包括试卷的创建、查询、更新、删除以及题目管理等功能。
 */
@Service
@Transactional
public class QuizService {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private CourseRepository courseRepository;

    /**
     * 创建新试卷
     * 该功能用于组卷管理的试卷创建功能
     */
    public QuizResponse createQuiz(QuizRequest request) {
        // 验证创建者是否存在
        Teacher creator = teacherRepository.findById(request.getCreatorId())
                .orElseThrow(() -> new ResourceNotFoundException("教师不存在"));

        // 验证课程是否存在
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在"));

        // 创建试卷实体
        Quiz quiz = new Quiz();
        quiz.setTitle(request.getTitle());
        quiz.setCreator(creator);
        quiz.setCourse(course);
        quiz.setTotalPoints(request.getTotalPoints());

        // 保存试卷
        Quiz savedQuiz = quizRepository.save(quiz);

        return new QuizResponse(savedQuiz);
    }

    /**
     * 根据ID获取试卷详情
     * 该功能用于组卷管理的试卷详情查看功能
     */
    public QuizDetailResponse getQuizById(Long quizId, Long teacherId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("试卷不存在"));

        // 验证权限：只能查看自己创建的试卷
        if (!quiz.getCreator().getTeacherId().equals(teacherId)) {
            throw new IllegalArgumentException("您没有权限查看此试卷");
        }

        return new QuizDetailResponse(quiz);
    }

    /**
     * 更新试卷基本信息
     * 该功能用于组卷管理的试卷编辑功能
     */
    public QuizResponse updateQuiz(Long quizId, QuizRequest request, Long teacherId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("试卷不存在"));

        // 验证权限：只能修改自己创建的试卷
        if (!quiz.getCreator().getTeacherId().equals(teacherId)) {
            throw new IllegalArgumentException("您没有权限修改此试卷");
        }

        // 验证课程是否存在（如果提供了courseId）
        if (request.getCourseId() != null) {
            Course course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("课程不存在"));
            quiz.setCourse(course);
        }

        // 更新试卷信息
        quiz.setTitle(request.getTitle());
        quiz.setTotalPoints(request.getTotalPoints());

        // 保存更新
        Quiz updatedQuiz = quizRepository.save(quiz);

        return new QuizResponse(updatedQuiz);
    }

    /**
     * 删除试卷
     * 该功能用于组卷管理的试卷删除功能
     */
    public void deleteQuiz(Long quizId, Long teacherId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("试卷不存在"));

        // 验证权限：只能删除自己创建的试卷
        if (!quiz.getCreator().getTeacherId().equals(teacherId)) {
            throw new IllegalArgumentException("您没有权限删除此试卷");
        }

        quizRepository.delete(quiz);
    }

    /**
     * 获取教师创建的所有试卷
     * 该功能用于组卷管理的试卷列表查看功能
     */
    public List<QuizResponse> getQuizzesByTeacher(Long teacherId) {
        List<Quiz> quizzes = quizRepository.findByCreatorTeacherId(teacherId);

        return quizzes.stream()
                .map(QuizResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * 向试卷添加题目
     * 该功能用于组卷管理的题目添加功能
     */
    public void addQuestionToQuiz(Long quizId, QuizQuestionRequest request, Long teacherId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("试卷不存在"));

        // 验证权限：只能修改自己创建的试卷
        if (!quiz.getCreator().getTeacherId().equals(teacherId)) {
            throw new IllegalArgumentException("您没有权限修改此试卷");
        }

        // 验证题目是否存在
        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("题目不存在"));

        // 检查题目是否已经在试卷中
        boolean exists = quiz.getQuestions().stream()
                .anyMatch(q -> q.getId().equals(request.getQuestionId()));

        if (exists) {
            throw new IllegalArgumentException("该题目已在试卷中");
        }

        // 方法1：先添加到ManyToMany关系
        quiz.getQuestions().add(question);
        quizRepository.save(quiz);

        // 方法2：然后使用原生SQL更新score和order_index
        quizRepository.updateQuestionScoreAndOrder(
                quizId,
                request.getQuestionId(),
                request.getScore(),
                request.getOrderIndex()
        );
    }

    /**
     * 从试卷移除题目
     * 该功能用于组卷管理的题目移除功能
     */
    public void removeQuestionFromQuiz(Long quizId, Long questionId, Long teacherId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("试卷不存在"));

        // 验证权限：只能修改自己创建的试卷
        if (!quiz.getCreator().getTeacherId().equals(teacherId)) {
            throw new IllegalArgumentException("您没有权限修改此试卷");
        }

        // 从试卷中移除题目
        Question questionToRemove = quiz.getQuestions().stream()
                .filter(q -> q.getId().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("题目不在试卷中"));

        // 移除题目（这会自动删除quiz_questions表中的记录）
        quiz.getQuestions().remove(questionToRemove);
        quizRepository.save(quiz);
    }

    /**
     * 设置题目分数和顺序
     * 该功能用于组卷管理的题目设置功能
     */
    public void setQuestionSettings(Long quizId, QuizQuestionRequest request, Long teacherId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("试卷不存在"));

        // 验证权限：只能修改自己创建的试卷
        if (!quiz.getCreator().getTeacherId().equals(teacherId)) {
            throw new IllegalArgumentException("您没有权限修改此试卷");
        }

        // 验证题目是否在试卷中
        boolean exists = quiz.getQuestions().stream()
                .anyMatch(q -> q.getId().equals(request.getQuestionId()));

        if (!exists) {
            throw new IllegalArgumentException("题目不在试卷中");
        }

        // 使用原生SQL更新分数和顺序
        quizRepository.updateQuestionScoreAndOrder(
                quizId,
                request.getQuestionId(),
                request.getScore(),
                request.getOrderIndex()
        );
    }

    /**
     * 复制试卷
     * 该功能用于组卷管理的试卷复制功能
     */
    public QuizResponse copyQuiz(Long quizId, String newTitle, Long teacherId) {
        Quiz originalQuiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("试卷不存在"));

        // 验证权限：只能复制自己创建的试卷
        if (!originalQuiz.getCreator().getTeacherId().equals(teacherId)) {
            throw new IllegalArgumentException("您没有权限复制此试卷");
        }

        // 创建新试卷
        Quiz newQuiz = new Quiz();
        newQuiz.setTitle(newTitle);
        newQuiz.setCreator(originalQuiz.getCreator());
        newQuiz.setCourse(originalQuiz.getCourse());
        newQuiz.setTotalPoints(originalQuiz.getTotalPoints());
        newQuiz.setQuestions(originalQuiz.getQuestions());

        // 保存新试卷
        Quiz savedQuiz = quizRepository.save(newQuiz);

        return new QuizResponse(savedQuiz);
    }

    /**
     * 通过课程ID获取试卷列表（教师端）
     * 该功能用于教师端按课程查看试卷
     */
    public List<QuizResponse> getQuizzesByCourse(Long courseId) {
        List<Quiz> quizzes = quizRepository.findAllByCourseCourseId(courseId);
        return quizzes.stream().map(QuizResponse::new).collect(Collectors.toList());
    }
}