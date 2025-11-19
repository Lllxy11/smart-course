package com.example.smartlearn.repository;

import com.example.smartlearn.model.KnowledgePointResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KnowledgePointResourcesRepository extends JpaRepository<KnowledgePointResource, Long> {

    // 查询指定课程的资源数量（使用DISTINCT确保去重）
    @Query("SELECT COUNT(DISTINCT kpr.resource.resourceId) " +
            "FROM KnowledgePointResource kpr " +
            "WHERE kpr.knowledgePoint.id IN (SELECT kp.id FROM KnowledgePoint kp WHERE kp.course.courseId = :courseId)")
    int countDistinctResourcesByCourseId(@Param("courseId") Long courseId);
}