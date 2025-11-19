package com.example.smartlearn.repository;

import com.example.smartlearn.model.ClassResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface ResourceRecommendationRepository extends JpaRepository<ClassResource, Long> {
    @Query("SELECT cr FROM ClassResource cr " +
            "JOIN KnowledgePointResource kpr ON kpr.resource.resourceId = cr.resourceId " +
            "WHERE kpr.knowledgePoint.id = :knowledgePointId " +
            "AND cr.type IN ('ppt', 'pdf', 'doc')")
    List<ClassResource> findByKnowledgePoint(@Param("knowledgePointId") Long knowledgePointId);

    // 获取学生最近错题相关的资源
    @Query("SELECT DISTINCT cr FROM ClassResource cr " +
            "JOIN KnowledgePointResource kpr ON kpr.resource.resourceId = cr.resourceId " +
            "JOIN Question q ON q.knowledgePoint.id = kpr.knowledgePoint.id " +
            "JOIN StudentAnswer sa ON sa.question.id = q.id " +
            "WHERE sa.submission.student.studentId = :studentId " +
            "AND sa.isCorrect = false " +
            "AND cr.type IN ('ppt', 'pdf', 'doc') " +
            "ORDER BY sa.submission.submittedAt DESC")
    List<ClassResource> findResourcesForWrongAnswers(@Param("studentId") Long studentId, Pageable pageable);
}
