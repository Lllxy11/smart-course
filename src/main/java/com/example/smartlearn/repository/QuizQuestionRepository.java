package com.example.smartlearn.repository;

import com.example.smartlearn.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuizQuestionRepository extends JpaRepository<Quiz, Long> {
    @Query(value = "SELECT score FROM quiz_questions " +
            "WHERE quiz_id = :quizId AND question_id = :questionId",
            nativeQuery = true)
    Integer findScoreByQuizIdAndQuestionId(
            @Param("quizId") Long quizId,
            @Param("questionId") Long questionId);
}
