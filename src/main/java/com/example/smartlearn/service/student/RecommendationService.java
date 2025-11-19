package com.example.smartlearn.service.student;

import java.util.List;

public interface RecommendationService {
    /**
     * 根据学生ID获取加权推荐
     * @param studentId 学生ID
     * @return 带权重的推荐资源列表
     */
    List<WeightedResource> getWeightedRecommendations(Long studentId);

    /**
     * 根据学生ID和课程ID获取加权推荐
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 带权重的推荐资源列表
     */
    List<WeightedResource> getWeightedRecommendations(Long studentId, Long courseId);
}

