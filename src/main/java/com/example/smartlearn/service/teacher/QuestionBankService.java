package com.example.smartlearn.service.teacher;

import com.example.smartlearn.dto.request.QuestionFilterRequest;
import com.example.smartlearn.dto.request.QuestionRequest;
import com.example.smartlearn.dto.response.QuestionResponse;
import com.example.smartlearn.exception.ResourceNotFoundException;
import com.example.smartlearn.model.Course;
import com.example.smartlearn.model.KnowledgePoint;
import com.example.smartlearn.model.Question;
import com.example.smartlearn.repository.CourseRepository;
import com.example.smartlearn.repository.KnowledgePointRepository;
import com.example.smartlearn.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 该服务类用于题库管理的业务逻辑处理。
 * 包括题目的创建、查询、更新、删除等功能。
 */
@Service
@Transactional
public class QuestionBankService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private KnowledgePointRepository knowledgePointRepository;

    /**
     * 创建新题目
     * 该功能用于题库管理的题目创建功能
     */
    public QuestionResponse createQuestion(QuestionRequest request, Long teacherId) {
        // 验证课程是否存在且属于当前教师
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在"));
        
        if (!course.getTeacherId().equals(teacherId)) {
            throw new IllegalArgumentException("您没有权限在此课程下创建题目");
        }

        // 验证知识点是否存在（如果提供了知识点ID）
        KnowledgePoint knowledgePoint = null;
        if (request.getKnowledgePointId() != null) {
            knowledgePoint = knowledgePointRepository.findById(request.getKnowledgePointId())
                    .orElseThrow(() -> new ResourceNotFoundException("知识点不存在"));
            
            if (!knowledgePoint.getCourse().getCourseId().equals(request.getCourseId())) {
                throw new IllegalArgumentException("知识点不属于指定课程");
            }
        }

        // 创建题目实体
        Question question = new Question();
        question.setCourse(course);
        question.setKnowledgePoint(knowledgePoint);
        question.setType(Question.QuestionType.valueOf(request.getType()));
        question.setBody(request.getBody());
        question.setDifficulty(request.getDifficulty());

        // 保存题目
        Question savedQuestion = questionRepository.save(question);
        
        return new QuestionResponse(savedQuestion);
    }

    /**
     * 根据条件筛选题目
     * 该功能用于题库管理的题目检索和筛选功能
     */
    public Page<QuestionResponse> getQuestions(QuestionFilterRequest request, Long teacherId) {
        // 创建分页对象
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        
        // 转换类型参数
        Question.QuestionType questionType = null;
        if (request.getType() != null && !request.getType().isEmpty()) {
            try {
                questionType = Question.QuestionType.valueOf(request.getType());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("无效的题目类型: " + request.getType());
            }
        }
        
        // 调用Repository的复杂条件查询
        Page<Question> questions = questionRepository.findByConditions(
                request.getCourseId(),
                request.getKnowledgePointId(),
                questionType,
                request.getDifficulty(),
                request.getKeyword(),
                pageable
        );
        
        // 转换为响应对象
        Page<QuestionResponse> responses = questions.map(QuestionResponse::new);
        
        return responses;
    }

    /**
     * 根据ID获取题目详情
     * 该功能用于题库管理的题目详情查看功能
     */
    public QuestionResponse getQuestionById(Long questionId, Long teacherId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("题目不存在"));
        
        // 验证权限：只能查看自己课程下的题目
        if (!question.getCourse().getTeacherId().equals(teacherId)) {
            throw new IllegalArgumentException("您没有权限查看此题目");
        }
        
        return new QuestionResponse(question);
    }

    /**
     * 更新题目
     * 该功能用于题库管理的题目编辑功能
     */
    public QuestionResponse updateQuestion(Long questionId, QuestionRequest request, Long teacherId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("题目不存在"));
        
        // 验证权限：只能修改自己课程下的题目
        if (!question.getCourse().getTeacherId().equals(teacherId)) {
            throw new IllegalArgumentException("您没有权限修改此题目");
        }

        // 验证课程是否存在且属于当前教师
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在"));
        
        if (!course.getTeacherId().equals(teacherId)) {
            throw new IllegalArgumentException("您没有权限在此课程下修改题目");
        }

        // 验证知识点是否存在（如果提供了知识点ID）
        KnowledgePoint knowledgePoint = null;
        if (request.getKnowledgePointId() != null) {
            knowledgePoint = knowledgePointRepository.findById(request.getKnowledgePointId())
                    .orElseThrow(() -> new ResourceNotFoundException("知识点不存在"));
            
            if (!knowledgePoint.getCourse().getCourseId().equals(request.getCourseId())) {
                throw new IllegalArgumentException("知识点不属于指定课程");
            }
        }

        // 更新题目信息
        question.setCourse(course);
        question.setKnowledgePoint(knowledgePoint);
        question.setType(Question.QuestionType.valueOf(request.getType()));
        question.setBody(request.getBody());
        question.setDifficulty(request.getDifficulty());

        // 保存更新
        Question updatedQuestion = questionRepository.save(question);
        
        return new QuestionResponse(updatedQuestion);
    }

    /**
     * 删除题目
     * 该功能用于题库管理的题目删除功能
     */
    public void deleteQuestion(Long questionId, Long teacherId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("题目不存在"));
        
        // 验证权限：只能删除自己课程下的题目
        if (!question.getCourse().getTeacherId().equals(teacherId)) {
            throw new IllegalArgumentException("您没有权限删除此题目");
        }

        // 检查题目是否被试卷使用
        // TODO: 这里需要检查题目是否被试卷使用，如果被使用需要提示用户
        
        questionRepository.delete(question);
    }

    /**
     * 批量删除题目
     * 该功能用于题库管理的批量删除功能
     */
    public void deleteQuestions(List<Long> questionIds, Long teacherId) {
        List<Question> questions = questionRepository.findAllById(questionIds);
        
        // 验证权限：只能删除自己课程下的题目
        for (Question question : questions) {
            if (!question.getCourse().getTeacherId().equals(teacherId)) {
                throw new IllegalArgumentException("您没有权限删除题目ID: " + question.getId());
            }
        }
        
        questionRepository.deleteAll(questions);
    }

    /**
     * 根据课程ID获取题目列表
     * 该功能用于题库管理的按课程筛选功能
     */
    public List<QuestionResponse> getQuestionsByCourse(Long courseId, Long teacherId) {
        // 验证课程是否存在且属于当前教师
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在"));
        
        if (!course.getTeacherId().equals(teacherId)) {
            throw new IllegalArgumentException("您没有权限查看此课程的题目");
        }
        
        List<Question> questions = questionRepository.findByCourseCourseId(courseId);
        
        return questions.stream()
                .map(QuestionResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * 根据关键词搜索题目
     * 该功能用于题库管理的关键词搜索功能
     */
    public List<QuestionResponse> searchQuestionsByKeyword(String keyword, Long teacherId) {
        List<Question> questions = questionRepository.findByKeyword(keyword);
        
        // 过滤出属于当前教师的题目
        List<Question> filteredQuestions = questions.stream()
                .filter(question -> question.getCourse().getTeacherId().equals(teacherId))
                .collect(Collectors.toList());
        
        return filteredQuestions.stream()
                .map(QuestionResponse::new)
                .collect(Collectors.toList());
    }
} 