package com.example.smartlearn.repository;

import com.example.smartlearn.model.StudentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Long> {
    
    /**
     * 根据提交记录ID查找所有答案
     */
    List<StudentAnswer> findBySubmissionId(Long submissionId);
    
    /**
     * 根据提交记录ID和题目ID查找答案
     */
    StudentAnswer findBySubmissionIdAndQuestionId(Long submissionId, Long questionId);
    
    /**
     * 根据学生ID和题目ID查找答案
     */
    List<StudentAnswer> findBySubmissionStudentStudentIdAndQuestionId(Long studentId, Long questionId);
    /**
     * 根据题目ID查找所有答题记录
     */
    List<StudentAnswer> findByQuestionId(Long questionId);
    
    /**
     * 根据题目ID统计答题总数
     */
    @Query("SELECT COUNT(sa) FROM StudentAnswer sa WHERE sa.question.id = :questionId")
    Long countByQuestionId(@Param("questionId") Long questionId);
    
    /**
     * 根据题目ID统计正确答题数
     */
    @Query("SELECT COUNT(sa) FROM StudentAnswer sa WHERE sa.question.id = :questionId AND sa.isCorrect = true")
    Long countCorrectByQuestionId(@Param("questionId") Long questionId);
    
    /**
     * 根据题目ID统计错误答题数
     */
    @Query("SELECT COUNT(sa) FROM StudentAnswer sa WHERE sa.question.id = :questionId AND sa.isCorrect = false")
    Long countWrongByQuestionId(@Param("questionId") Long questionId);
    
    /**
     * 根据题目ID和答案内容统计选择次数
     */
    @Query("SELECT COUNT(sa) FROM StudentAnswer sa WHERE sa.question.id = :questionId AND sa.answerContent = :answerContent")
    Long countByQuestionIdAndAnswerContent(@Param("questionId") Long questionId, @Param("answerContent") String answerContent);
    
    /**
     * 根据课程ID查找所有答题记录
     */
    @Query("SELECT sa FROM StudentAnswer sa WHERE sa.question.course.courseId = :courseId")
    List<StudentAnswer> findByCourseId(@Param("courseId") Long courseId);
    
    /**
     * 根据课程ID列表查找所有答题记录
     */
    @Query("SELECT sa FROM StudentAnswer sa WHERE sa.question.course.courseId IN :courseIds")
    List<StudentAnswer> findByCourseIds(@Param("courseIds") List<Long> courseIds);

    @Query("SELECT sa FROM StudentAnswer sa " +
            "JOIN FETCH sa.question q " +
            "WHERE sa.submission.student.studentId = :studentId " +
            "AND sa.isCorrect = false")
    List<StudentAnswer> findWrongAnswersWithQuestions(@Param("studentId") Long studentId);
    // 查找学生在特定课程的所有错题（包含题目信息）
    @Query("SELECT sa FROM StudentAnswer sa " +
            "JOIN FETCH sa.question q " +
            "WHERE sa.submission.student.studentId = :studentId " +
            "AND q.course.courseId = :courseId " +
            "AND sa.isCorrect = false")
    List<StudentAnswer> findWrongAnswersWithQuestionsByCourse(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId);
    // 查找学生的所有错题（包含题目信息和所属测验信息）
    @Query("SELECT sa FROM StudentAnswer sa " +
            "JOIN FETCH sa.question q " +
            "JOIN FETCH sa.submission s " +
            "JOIN FETCH s.task t " +
            "WHERE sa.submission.student.studentId = :studentId " +
            "AND sa.isCorrect = false")
    List<StudentAnswer> findWrongAnswersWithQuestionsAndQuiz(@Param("studentId") Long studentId);

    // 查找学生在特定课程的所有错题（包含题目信息和所属测验信息）
    @Query("SELECT sa FROM StudentAnswer sa " +
            "JOIN FETCH sa.question q " +
            "JOIN FETCH sa.submission s " +
            "JOIN FETCH s.task t " +
            "WHERE sa.submission.student.studentId = :studentId " +
            "AND q.course.courseId = :courseId " +
            "AND sa.isCorrect = false")
    List<StudentAnswer> findWrongAnswersWithQuestionsAndQuizByCourse(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId);

} 