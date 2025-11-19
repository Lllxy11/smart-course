package com.example.smartlearn.repository;

import com.example.smartlearn.model.Teacher;
import com.example.smartlearn.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByName(String name);
    Optional<Teacher> findByUser(User user);
}