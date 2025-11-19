package com.example.smartlearn.repository;

import com.example.smartlearn.model.KnowledgePoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface KnowledgePointsRepository extends JpaRepository<KnowledgePoint, Long> {

    @Query("SELECT COUNT(kp) FROM KnowledgePoint kp WHERE kp.course.courseId = :courseId")
    int countByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT kp.course.courseId, COUNT(kp) FROM KnowledgePoint kp WHERE kp.course.courseId IN :courseIds GROUP BY kp.course.courseId")
    Map<Long, Integer> countByCourseIds(@Param("courseIds") List<Long> courseIds);
}