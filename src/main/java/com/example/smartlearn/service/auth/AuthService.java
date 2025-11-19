package com.example.smartlearn.service.auth;


import com.example.smartlearn.dto.request.LoginRequest;
import com.example.smartlearn.dto.request.UserRegistrationRequest;
import com.example.smartlearn.dto.response.LoginResponse;
import com.example.smartlearn.exception.AccountAlreadyExistsException;
import com.example.smartlearn.exception.InvalidRoleException;
import com.example.smartlearn.model.Student;
import com.example.smartlearn.model.Teacher;
import com.example.smartlearn.model.User;
import com.example.smartlearn.repository.StudentRepository;
import com.example.smartlearn.repository.TeacherRepository;
import com.example.smartlearn.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    @Autowired
    public AuthService(UserRepository userRepository,
                       StudentRepository studentRepository,
                       TeacherRepository teacherRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
    }


    public LoginResponse authenticate(LoginRequest request) {
        // 添加日志

        System.out.println("接收登录请求: " + request.getAccount() + ", 角色: " + request.getRole());

        // 添加角色条件查询
        User user = userRepository.findByAccountAndRole(request.getAccount(), request.getRole())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "账号不存在"));

        System.out.println("数据库查询结果: " + user.getAccount());

        // 密码验证
        if (!request.getPassword().equals(user.getPasswordHash())) {
            System.out.println("密码错误");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "密码错误");
        }

        // 根据角色获取教师ID或学生ID
        Long teacherId = null;
        Long studentId = null;

        if ("teacher".equals(user.getRole())) {
            // 查询教师记录
            Teacher teacher = teacherRepository.findByUser(user)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "教师信息未找到"));
            teacherId = teacher.getTeacherId();
        } else if ("student".equals(user.getRole())) {
            // 查询学生记录
            Student student = studentRepository.findByUser(user)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "学生信息未找到"));
            studentId = student.getStudentId();
        }

        // 返回登录信息，包含教师ID或学生ID
        return new LoginResponse(user, teacherId, studentId);
    }

    @Transactional
    public void register(UserRegistrationRequest request) {
        log.info("开始注册处理: 账号={}", request.getAccount());

        // 1. 验证账号唯一性
        if (userRepository.existsByAccount(request.getAccount())) {
            log.warn("账号已存在: {}", request.getAccount());
            throw new AccountAlreadyExistsException("该账号已存在");
        }

        // 2. 创建并保存用户记录
        User user = new User();
        user.setAccount(request.getAccount());
        user.setPasswordHash(request.getPassword());
        user.setRole(request.getRole().toLowerCase()); // 小写存储

        User savedUser = userRepository.save(user);
        log.info("用户记录保存成功, ID={}", savedUser.getUserId());

        // 3. 根据角色创建学生或教师记录
        String role = request.getRole().toLowerCase();

        if ("student".equals(role)) {
            Student student = new Student();
            student.setUser(savedUser);
            student.setName(request.getName());
            student.setClassName(request.getClassName());
            student.setStudentId(request.getStudentId());

            studentRepository.save(student);
            log.info("学生记录保存成功: name={}, class={}",
                    student.getName(), student.getClassName());
        }
        else if ("teacher".equals(role)) {
            Teacher teacher = new Teacher();
            teacher.setUser(savedUser);
            teacher.setName(request.getName());
            teacher.setDepartment(request.getDepartment());
            teacher.setAvatarUrl("/default-avatar.png");

            teacherRepository.save(teacher);
            log.info("教师记录保存成功: name={}, department={}",
                    teacher.getName(), teacher.getDepartment());
        }
        else {
            log.error("无效角色类型: {}", role);
            throw new InvalidRoleException("无效的角色类型: " + role);
        }
    }


}