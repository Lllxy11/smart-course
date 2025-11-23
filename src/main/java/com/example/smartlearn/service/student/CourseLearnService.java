package com.example.smartlearn.service.student;

import com.example.smartlearn.dto.StudentCourseDTO;
import com.example.smartlearn.dto.StudentCourseStatusDTO;
import com.example.smartlearn.model.ClassResource;
import com.example.smartlearn.model.Course;
import com.example.smartlearn.model.Student;
import com.example.smartlearn.model.StudentCourse;
import com.example.smartlearn.repository.ClassResourceRepository;
import com.example.smartlearn.repository.CourseRepository;
import com.example.smartlearn.repository.StudentCourseRepository;
import com.example.smartlearn.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CourseLearnService {
    private final StudentCourseRepository studentCourseRepository;
    private final ClassResourceRepository classResourceRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    @Value("${file.upload-dir}")
    private String uploadDir;
    @Autowired
    public CourseLearnService(StudentCourseRepository studentCourseRepository,
                             ClassResourceRepository classResourceRepository,
                             CourseRepository courseRepository,
                             StudentRepository studentRepository) {
        this.studentCourseRepository = studentCourseRepository;
        this.classResourceRepository = classResourceRepository;
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
    }

    /**
     * 按课程ID和资源类型获取资源（带权限校验）
     */
    @Transactional(readOnly = true)
    public List<ClassResource> getResourcesByCourseAndType(
            Long studentId,
            Long courseId,
            ClassResource.ResourceType resourceType
    ) throws AccessDeniedException {
        // 1. 验证学生是否属于该课程
        if (!studentCourseRepository.existsByStudentStudentIdAndCourseCourseId(studentId, courseId)) {
            throw new AccessDeniedException("无权访问此课程资源");
        }
        System.out.println('A');
        System.out.println(courseId);
        System.out.println(resourceType);
        List<ClassResource> classResource = new ArrayList<>();
        classResource=classResourceRepository.findByCourseIdAndType(courseId.intValue(),resourceType);
        System.out.println(classResource.size());
        System.out.println(classResource);

        // 2. 查询课程下指定类型的资源
        return classResourceRepository.findByCourseIdAndType(courseId.intValue(), resourceType);


    }
    @Transactional
    public List<StudentCourseDTO> getEnrolledCourse(
            Long studentId
    ){
        List<StudentCourseDTO>  studentCourseDTOList = studentCourseRepository.findCourseDTOsByStudentId(studentId);
        for(StudentCourseDTO studentCourseDTO:studentCourseDTOList){
            System.out.println(studentCourseDTO);
        }
        return studentCourseRepository.findCourseDTOsByStudentId(studentId);


    }

    /**
     * 获取所有课程并标记是否已选
     */
    @Transactional(readOnly = true)
    public List<StudentCourseStatusDTO> getCoursesWithStatus(Long studentId) {
        var enrolled = studentCourseRepository.findByStudentStudentId(studentId);
        var enrolledIds = enrolled.stream()
                .map(sc -> sc.getCourse().getCourseId())
                .collect(java.util.stream.Collectors.toSet());

        List<Course> allCourses = courseRepository.findAll();

        List<StudentCourseStatusDTO> result = new java.util.ArrayList<>();
        for (Course course : allCourses) {
            result.add(StudentCourseStatusDTO.builder()
                    .courseId(course.getCourseId())
                    .code(course.getCode())
                    .courseName(course.getName())
                    .selected(enrolledIds.contains(course.getCourseId()))
                    .build());
        }
        return result;
    }

    /**
     * 学生选课
     */
    @Transactional
    public void enrollCourse(Long studentId, Long courseId) {
        // 已存在则直接返回
        if (studentCourseRepository.existsByStudentStudentIdAndCourseCourseId(studentId, courseId)) {
            return;
        }
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("学生不存在"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("课程不存在"));

        StudentCourse sc = new StudentCourse();
        sc.setStudent(student);
        sc.setCourse(course);
        sc.setEnrollDate(java.time.LocalDateTime.now());
        studentCourseRepository.save(sc);
    }






}
