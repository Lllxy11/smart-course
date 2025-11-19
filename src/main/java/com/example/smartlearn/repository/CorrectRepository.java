package com.example.smartlearn.repository;

import com.example.smartlearn.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CorrectRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByTaskId(Long taskId);

    @Query("SELECT s FROM Submission s " +
            "LEFT JOIN FETCH s.studentAnswers sa " +
            "LEFT JOIN FETCH sa.question " +
            "LEFT JOIN FETCH s.student " +
            "LEFT JOIN FETCH s.task " +
            "WHERE s.id = :submissionId")
    Optional<Submission> findByIdWithStudentAnswers(@Param("submissionId") Long submissionId);
}