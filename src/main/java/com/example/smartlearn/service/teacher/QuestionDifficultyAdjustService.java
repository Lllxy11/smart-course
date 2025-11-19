package com.example.smartlearn.service.teacher;

import com.example.smartlearn.dto.request.QuestionDifficultyAdjustRequest;
import com.example.smartlearn.dto.response.QuestionDifficultyAdjustResponse;
import com.example.smartlearn.model.Question;
import com.example.smartlearn.model.StudentAnswer;
import com.example.smartlearn.repository.CourseRepository;
import com.example.smartlearn.repository.KnowledgePointRepository;
import com.example.smartlearn.repository.QuestionRepository;
import com.example.smartlearn.repository.StudentAnswerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 题目难度动态调整-曹雨荷部分
 * 业务逻辑：查询建议、确认调整
 */
@Service
public class QuestionDifficultyAdjustService {
    
    private static final Logger logger = LoggerFactory.getLogger(QuestionDifficultyAdjustService.class);
    
    // 题目难度动态调整-曹雨荷部分：依赖注入
    @Autowired
    private QuestionRepository questionRepository;
    
    @Autowired
    private StudentAnswerRepository studentAnswerRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private KnowledgePointRepository knowledgePointRepository;

    /**
     * 查询可调整题目及统计信息
     * 题目难度动态调整-曹雨荷部分
     */
    public QuestionDifficultyAdjustResponse.QueryResult queryAdjustableQuestions(QuestionDifficultyAdjustRequest.Query request) {
        logger.info("题目难度动态调整-曹雨荷部分：开始查询可调整题目，课程IDs: {}", request.getCourseIds());
        
        try {
            // 1. 根据课程ID筛选题目
            List<Long> courseIds = request.getCourseIds();
            List<Question> questions;
            
            if (courseIds == null || courseIds.isEmpty() || Boolean.TRUE.equals(request.getIncludeAllCourses())) {
                // 查询所有题目
                questions = questionRepository.findAll();
                logger.info("题目难度动态调整-曹雨荷部分：查询所有题目，共{}道", questions.size());
            } else {
                // 按课程ID筛选题目
                questions = courseIds.stream()
                        .flatMap(cid -> questionRepository.findByCourseCourseId(cid).stream())
                        .collect(Collectors.toList());
                logger.info("题目难度动态调整-曹雨荷部分：按课程筛选题目，课程IDs: {}, 共{}道", courseIds, questions.size());
            }
            
            // 2. 统计每道题的信息
            List<QuestionDifficultyAdjustResponse.QueryResult.QuestionStat> stats = new ArrayList<>();
            int needAdjustment = 0;
            double totalCorrectRate = 0;
            
            for (Question question : questions) {
                try {
                    // 使用优化的查询方法获取该题目的所有答题记录
                    List<StudentAnswer> answers = studentAnswerRepository.findByQuestionId(question.getId());
                    
                    // 计算基础统计信息
                    int totalAttempts = answers.size();
                    int wrongAttempts = (int) answers.stream()
                            .filter(answer -> Boolean.FALSE.equals(answer.getCorrect()))
                            .count();
                    int correctAttempts = totalAttempts - wrongAttempts;
                    double correctRate = totalAttempts > 0 ? (double) correctAttempts / totalAttempts : 0.0;
                    totalCorrectRate += correctRate;
                    
                    // 3. 选项分布统计（仅对选择题）
                    Map<String, Integer> optionStats = new HashMap<>();
                    if (question.getType() == Question.QuestionType.SINGLE_CHOICE || 
                        question.getType() == Question.QuestionType.MULTI_CHOICE) {
                        for (StudentAnswer answer : answers) {
                            String answerContent = answer.getAnswerContent();
                            if (answerContent != null && !answerContent.trim().isEmpty()) {
                                optionStats.put(answerContent, optionStats.getOrDefault(answerContent, 0) + 1);
                            }
                        }
                    }
                    
                    // 4. 建议难度算法（区间映射） 题目难度动态调整-曹雨荷部分
                    int currentDifficulty = question.getDifficulty() == null ? 3 : question.getDifficulty();
                    int suggestedDifficulty = getSuggestedDifficultyByCorrectRate(correctRate); // 题目难度动态调整-曹雨荷部分
                    String changeReason = null;

                    if (totalAttempts >= 10 && suggestedDifficulty != currentDifficulty) {
                        if (suggestedDifficulty > currentDifficulty) {
                            changeReason = "正确率高于当前难度区间，建议提升难度";
                        } else {
                            changeReason = "正确率低于当前难度区间，建议降低难度";
                        }
                        
                        // 增加需要调整的题目计数
                        needAdjustment++;
                        
                        // 5. 获取课程和知识点名称
                        String courseName = question.getCourse() != null ? question.getCourse().getName() : "未知课程";
                        String knowledgePointName = question.getKnowledgePoint() != null ? 
                                question.getKnowledgePoint().getName() : "未知知识点";
                        // 6. 组装题目统计信息
                        QuestionDifficultyAdjustResponse.QueryResult.QuestionStat stat = 
                                new QuestionDifficultyAdjustResponse.QueryResult.QuestionStat();
                        stat.setId(question.getId());
                        stat.setBody(question.getBody());
                        stat.setCurrentDifficulty(currentDifficulty);
                        stat.setSuggestedDifficulty(suggestedDifficulty);
                        stat.setCorrectRate(correctRate);
                        stat.setTotalAttempts(totalAttempts);
                        stat.setWrongAttempts(wrongAttempts);
                        stat.setOptionStats(optionStats);
                        stat.setCourseName(courseName);
                        stat.setKnowledgePointName(knowledgePointName);
                        stat.setChangeReason(changeReason);
                        stats.add(stat);
                    }
                    
                } catch (Exception e) {
                    logger.error("题目难度动态调整-曹雨荷部分：处理题目{}时出错: {}", question.getId(), e.getMessage());
                    // 继续处理下一题，不中断整个流程
                }
            }
            
            // 7. 汇总统计信息
            QuestionDifficultyAdjustResponse.QueryResult.Statistics statistics = 
                    new QuestionDifficultyAdjustResponse.QueryResult.Statistics();
            statistics.setTotalQuestions(questions.size());
            statistics.setNeedAdjustment(needAdjustment);
            statistics.setAverageCorrectRate(questions.size() > 0 ? totalCorrectRate / questions.size() : 0.0);
            
            // 8. 组装返回结果
            QuestionDifficultyAdjustResponse.QueryResult result = new QuestionDifficultyAdjustResponse.QueryResult();
            result.setQuestions(stats);
            result.setStatistics(statistics);
            
            logger.info("题目难度动态调整-曹雨荷部分：查询完成，总题数: {}, 需调整: {}, 平均正确率: {:.2f}", 
                    questions.size(), needAdjustment, statistics.getAverageCorrectRate());
            
            return result;
            
        } catch (Exception e) {
            logger.error("题目难度动态调整-曹雨荷部分：查询可调整题目时发生异常: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 批量确认调整题目难度
     * 题目难度动态调整-曹雨荷部分
     */
    @Transactional
    public QuestionDifficultyAdjustResponse.ConfirmResult confirmAdjustments(QuestionDifficultyAdjustRequest.Confirm request) {
        logger.info("题目难度动态调整-曹雨荷部分：开始批量调整题目难度，调整数量: {}", request.getAdjustments().size());
        
        List<QuestionDifficultyAdjustResponse.ConfirmResult.Detail> details = new ArrayList<>();
        int successCount = 0;
        int failedCount = 0;
        
        for (QuestionDifficultyAdjustRequest.Confirm.Adjustment adjustment : request.getAdjustments()) {
            QuestionDifficultyAdjustResponse.ConfirmResult.Detail detail = 
                    new QuestionDifficultyAdjustResponse.ConfirmResult.Detail();
            detail.setQuestionId(adjustment.getQuestionId());
            
            try {
                if (Boolean.TRUE.equals(adjustment.getShouldAdjust())) {
                    // 需要调整的题目
                    Optional<Question> questionOpt = questionRepository.findById(adjustment.getQuestionId());
                    if (questionOpt.isPresent()) {
                        Question question = questionOpt.get();
                        int oldDifficulty = question.getDifficulty() == null ? 3 : question.getDifficulty();
                        
                        // 更新题目难度
                        question.setDifficulty(adjustment.getNewDifficulty());
                        questionRepository.save(question);
                        
                        // 设置调整详情
                        detail.setOldDifficulty(oldDifficulty);
                        detail.setNewDifficulty(adjustment.getNewDifficulty());
                        detail.setStatus("success");
                        successCount++;
                        
                        logger.info("题目难度动态调整-曹雨荷部分：题目{}调整成功，{} -> {}", 
                                adjustment.getQuestionId(), oldDifficulty, adjustment.getNewDifficulty());
                    } else {
                        // 题目不存在
                        detail.setStatus("failed");
                        failedCount++;
                        logger.warn("题目难度动态调整-曹雨荷部分：题目{}不存在", adjustment.getQuestionId());
                    }
                } else {
                    // 跳过调整
                    detail.setStatus("skipped");
                    logger.debug("题目难度动态调整-曹雨荷部分：题目{}跳过调整", adjustment.getQuestionId());
                }
            } catch (Exception e) {
                // 调整失败
                detail.setStatus("failed");
                failedCount++;
                logger.error("题目难度动态调整-曹雨荷部分：题目{}调整失败: {}", adjustment.getQuestionId(), e.getMessage());
            }
            
            details.add(detail);
        }
        
        // 组装返回结果
        QuestionDifficultyAdjustResponse.ConfirmResult result = new QuestionDifficultyAdjustResponse.ConfirmResult();
        result.setSuccessCount(successCount);
        result.setFailedCount(failedCount);
        result.setDetails(details);
        
        logger.info("题目难度动态调整-曹雨荷部分：批量调整完成，成功: {}, 失败: {}", successCount, failedCount);
        
        return result;
    }

    /**
     * 获取题目难度星级区间下界
     * 题目难度动态调整-曹雨荷部分
     */
    private double getLowerBound(int difficulty) {
        switch (difficulty) {
            case 1: return 0.85; // 1星题目：正确率应≥85%
            case 2: return 0.70; // 2星题目：正确率应≥70%
            case 3: return 0.50; // 3星题目：正确率应≥50%
            case 4: return 0.30; // 4星题目：正确率应≥30%
            case 5: return 0.00; // 5星题目：正确率应≥0%
            default: return 0.50;
        }
    }
    
    /**
     * 获取题目难度星级区间上界
     * 题目难度动态调整-曹雨荷部分
     */
    private double getUpperBound(int difficulty) {
        switch (difficulty) {
            case 1: return 1.00; // 1星题目：正确率应≤100%
            case 2: return 0.85; // 2星题目：正确率应≤85%
            case 3: return 0.70; // 3星题目：正确率应≤70%
            case 4: return 0.50; // 4星题目：正确率应≤50%
            case 5: return 0.30; // 5星题目：正确率应≤30%
            default: return 0.70;
        }
    }

    /**
     * 正确率区间映射建议难度
     * 题目难度动态调整-曹雨荷部分
     */
    private int getSuggestedDifficultyByCorrectRate(double correctRate) {
        if (correctRate >= 0.85) return 1;
        if (correctRate >= 0.70) return 2;
        if (correctRate >= 0.50) return 3;
        if (correctRate >= 0.30) return 4;
        return 5;
    }
} 