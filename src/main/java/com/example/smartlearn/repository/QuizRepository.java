package com.example.smartlearn.repository;

import com.example.smartlearn.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 该Repository接口用于试卷数据访问功能。
 * 提供试卷的基本CRUD操作。
 */
@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    /**
     * 根据创建者ID查找试卷列表
     * 该功能用于组卷管理的按教师筛选试卷
     */
    List<Quiz> findByCreatorTeacherId(Long teacherId);

    /**
     * 根据标题模糊查询试卷
     * 该功能用于组卷管理的试卷标题搜索
     */
    List<Quiz> findByTitleContaining(String title);

    /**
     * 根据课程ID查找试卷列表
     * 该功能用于按课程筛选试卷
     */
    List<Quiz> findAllByCourseCourseId(Long courseId);

    /**
     * 更新quiz_questions表中的分数和顺序
     * 该方法用于在添加题目后设置分数和顺序信息
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE quiz_questions SET score = :score, order_index = :orderIndex " +
            "WHERE quiz_id = :quizId AND question_id = :questionId", nativeQuery = true)
    void updateQuestionScoreAndOrder(@Param("quizId") Long quizId,
                                     @Param("questionId") Long questionId,
                                     @Param("score") Integer score,
                                     @Param("orderIndex") Integer orderIndex);
} 