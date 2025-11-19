package com.example.smartlearn.service.auth;

import com.example.smartlearn.model.Teacher;
import com.example.smartlearn.repository.TeacherRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class TeacherAuthService {

    private final TeacherRepository teacherRepository;

    public TeacherAuthService(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    public Long getCurrentTeacherId(HttpServletRequest request) {
        // 从请求中获取认证信息（具体实现取决于您的认证方式）
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Teacher teacher = teacherRepository.findByName(username)
                .orElseThrow(() -> new RuntimeException("教师信息未找到"));
        return teacher.getTeacherId();
    }
}