package com.example.smartlearn.repository;

import com.example.smartlearn.model.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 该仓库接口用于题库管理的题目数据访问功能。
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    
    /**
     * 根据课程ID查找题目列表
     * 该功能用于题库管理的按课程筛选功能
     */
    List<Question> findByCourseCourseId(Long courseId);
    
    /**
     * 根据关键词搜索题目
     * 该功能用于题库管理的关键词搜索功能
     */
    @Query("SELECT q FROM Question q WHERE q.body LIKE %:keyword%")
    List<Question> findByKeyword(@Param("keyword") String keyword);
    
    /**
     * 根据多个条件筛选题目
     * 该功能用于题库管理的复杂条件筛选功能
     */
    @Query("SELECT q FROM Question q WHERE " +
           "(:courseId IS NULL OR q.course.courseId = :courseId) AND " +
           "(:knowledgePointId IS NULL OR q.knowledgePoint.id = :knowledgePointId) AND " +
           "(:type IS NULL OR q.type = :type) AND " +
           "(:difficulty IS NULL OR q.difficulty = :difficulty) AND " +
           "(:keyword IS NULL OR q.body LIKE %:keyword%)")
    Page<Question> findByConditions(
            @Param("courseId") Long courseId,
            @Param("knowledgePointId") Long knowledgePointId,
            @Param("type") Question.QuestionType type,
            @Param("difficulty") Integer difficulty,
            @Param("keyword") String keyword,
            Pageable pageable
    );
} 