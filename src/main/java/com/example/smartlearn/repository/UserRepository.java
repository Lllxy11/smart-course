package com.example.smartlearn.repository;

import com.example.smartlearn.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByAccount(String account);

    @Query("SELECT u FROM User u WHERE u.account = :account AND u.role = :role")
    Optional<User> findByAccountAndRole(@Param("account") String account,
                                        @Param("role") String role);

    boolean existsByAccount(String account);
}