package com.example.smartlearn.service.student;

import com.example.smartlearn.model.*;
import com.example.smartlearn.repository.KnowledgePointResourceRepository;
import com.example.smartlearn.repository.QuizQuestionRepository;
import com.example.smartlearn.repository.StudentAnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final StudentAnswerRepository studentAnswerRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final KnowledgePointResourceRepository knowledgePointResourceRepository;

    @Override
    public List<WeightedResource> getWeightedRecommendations(Long studentId) {
        // 1. 获取学生的所有错题（包含题目信息）
        List<StudentAnswer> wrongAnswers = studentAnswerRepository
                .findWrongAnswersWithQuestions(studentId);
        Map<KnowledgePoint, Double> knowledgePointWeights = calculateKnowledgePointWeights(wrongAnswers);

        // 3. 获取推荐资源并计算权重
        return getWeightedResources(knowledgePointWeights);
    }

    @Override
    public List<WeightedResource> getWeightedRecommendations(Long studentId, Long courseId) {
        // 1. 获取学生在特定课程的所有错题（包含题目信息）
        List<StudentAnswer> wrongAnswers = studentAnswerRepository
                .findWrongAnswersWithQuestionsByCourse(studentId, courseId);

        // 2. 计算知识点权重（考虑题目难度）
        Map<KnowledgePoint, Double> knowledgePointWeights = calculateKnowledgePointWeights(wrongAnswers);

        // 3. 获取推荐资源并计算权重
        return getWeightedResources(knowledgePointWeights);
    }

    /**
     * 计算知识点权重（考虑题目难度和错误次数）
     */
    private Map<KnowledgePoint, Double> calculateKnowledgePointWeights(List<StudentAnswer> wrongAnswers) {
        Map<KnowledgePoint, Double> weights = new HashMap<>();

        for (StudentAnswer answer : wrongAnswers) {
            Question question = answer.getQuestion();
            KnowledgePoint kp = question.getKnowledgePoint();
            if (kp == null) continue;

            // 基础权重（错误次数）
            double weight = 1.0;

            // 难度加权（假设难度1-5，1最简单，5最难）
            Integer difficulty = question.getDifficulty();
            if (difficulty != null) {
                weight *= (1 + difficulty * 0.2); // 难度系数加成
            }

            // 分数加权 - 修改后的实现
            if (answer.getSubmission().getTask().getType()== Task.TaskType.QUIZ) {
                Long quizId = answer.getSubmission().getTask().getId();
                Integer questionScore = quizQuestionRepository.findScoreByQuizIdAndQuestionId(quizId, question.getId());

                if (questionScore != null && questionScore > 0 && answer.getScore() != null) {
                    double scoreRatio = answer.getScore().doubleValue() / questionScore;
                    weight *= (2 - scoreRatio); // 得分率越低，权重越高
                }
            }

            weights.merge(kp, weight, Double::sum);
        }

        return weights;
    }

    /**
     * 根据知识点权重获取加权资源
     */
    private List<WeightedResource> getWeightedResources(Map<KnowledgePoint, Double> knowledgePointWeights) {
        if (knowledgePointWeights.isEmpty()) {
            return Collections.emptyList();
        }

        // 获取知识点关联的所有资源
        List<KnowledgePointResource> allResources = knowledgePointResourceRepository
                .findWithResourcesByKnowledgePointIdIn(
                        knowledgePointWeights.keySet().stream()
                                .map(KnowledgePoint::getId)
                                .collect(Collectors.toList()));

        // 计算每个资源的总权重
        Map<ClassResource, WeightedResource> resourceMap = new HashMap<>();

        for (KnowledgePointResource kpr : allResources) {
            KnowledgePoint kp = kpr.getKnowledgePoint();
            ClassResource resource = kpr.getResource();
            double weight = knowledgePointWeights.getOrDefault(kp, 1.0);

            // 资源类型加权
            if (resource.getType().equals(ClassResource.ResourceType.ppt) ) {

                weight *= 1.3;
            }else if (resource.getType().equals(ClassResource.ResourceType.doc)) {
                weight *= 1.1;
            }else if (resource.getType().equals(ClassResource.ResourceType.pdf) ) {
                weight *= 1.2;
            }
            if (resourceMap.containsKey(resource)) {
                // 如果资源已存在，累加权重
                WeightedResource existing = resourceMap.get(resource);
                existing.setWeight(existing.getWeight() + weight);
                existing.getRelatedKnowledgePointIds().add(kp.getId());
            } else {
                // 新资源，创建记录
                List<Long> relatedKpIds = new ArrayList<>();
                relatedKpIds.add(kp.getId());
                resourceMap.put(resource, new WeightedResource(resource, weight, relatedKpIds));
            }
        }

        // 按权重排序返回
        return resourceMap.values().stream()
                .sorted(Comparator.comparing(WeightedResource::getWeight).reversed())
                .collect(Collectors.toList());
    }
}
