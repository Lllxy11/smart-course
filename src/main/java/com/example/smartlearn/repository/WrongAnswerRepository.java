package com.example.smartlearn.repository;

import com.example.smartlearn.model.KnowledgePoint;
import com.example.smartlearn.model.StudentAnswer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface WrongAnswerRepository extends JpaRepository<StudentAnswer, Long> {
    // 查询学生所有错题 - 修正字段引用
    @Query("SELECT sa FROM StudentAnswer sa WHERE sa.submission.student.studentId = :studentId AND sa.isCorrect = false")
    List<StudentAnswer> findWrongAnswersByStudent(@Param("studentId") Long studentId);

    // 按知识点统计错题 - 修正字段引用和关联关系
    @Query("SELECT q.knowledgePoint, COUNT(sa) FROM StudentAnswer sa " +
            "JOIN sa.question q " +  // 假设Question类有knowledgePoint字段
            "JOIN q.knowledgePoint kp " +  // 修正关联路径
            "WHERE sa.submission.student.studentId = :studentId AND sa.isCorrect = false " +
            "GROUP BY q.knowledgePoint")  // 按知识点分组
    List<Object[]> countWrongAnswersByKnowledgePoint(@Param("studentId") Long studentId);

    // 查询同类错题（相同知识点） - 完全重写
    @Query("SELECT sa FROM StudentAnswer sa " +
            "JOIN sa.question q " +
            "WHERE sa.question.id != :currentQuestionId " +
            "AND q.knowledgePoint IN :knowledgePoints " +  // 直接使用知识点列表
            "AND sa.submission.student.studentId = :studentId " +  // 确保是同一学生的错题
            "AND sa.isCorrect = false " +
            "ORDER BY sa.submission.submittedAt DESC")  // 使用正确的提交时间字段
    List<StudentAnswer> findSimilarWrongAnswers(
            @Param("knowledgePoints") List<KnowledgePoint> knowledgePoints,
            @Param("currentQuestionId") Long currentQuestionId,
            @Param("studentId") Long studentId,
            Pageable pageable);
}
