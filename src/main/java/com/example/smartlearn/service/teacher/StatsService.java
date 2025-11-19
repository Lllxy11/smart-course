package com.example.smartlearn.service.teacher;

import com.example.smartlearn.controller.teacher.CourseStats;
import com.example.smartlearn.repository.KnowledgePointResourcesRepository;
import com.example.smartlearn.repository.KnowledgePointsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsService {

    @Autowired
    private KnowledgePointsRepository knowledgePointsRepo;

    @Autowired
    private KnowledgePointResourcesRepository pointResourcesRepo;

    // 单课程统计 - 简单准确的逻辑
    public CourseStats getCourseStats(Long courseId) {
        // 知识点数量统计
        int pointCount = knowledgePointsRepo.countByCourseId(courseId);

        // 资源数量统计（直接计数知识点的关联资源）
        int resourceCount = pointResourcesRepo.countDistinctResourcesByCourseId(courseId);

        return new CourseStats(pointCount, resourceCount);
    }

    // 批量统计 - 简化逻辑
    public Map<Long, CourseStats> getBatchCourseStats(List<Long> courseIds) {
        // 知识点数量统计
        Map<Long, Integer> pointCounts = knowledgePointsRepo.countByCourseIds(courseIds);

        // 资源数量统计
        Map<Long, Integer> resourceCounts = new HashMap<>();
        for (Long courseId : courseIds) {
            resourceCounts.put(courseId, pointResourcesRepo.countDistinctResourcesByCourseId(courseId));
        }

        // 合并结果
        Map<Long, CourseStats> result = new HashMap<>();
        for (Long courseId : courseIds) {
            result.put(
                    courseId,
                    new CourseStats(
                            pointCounts.getOrDefault(courseId, 0),
                            resourceCounts.getOrDefault(courseId, 0)
                    )
            );
        }
        return result;
    }
}