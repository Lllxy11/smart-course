package com.example.smartlearn.service.student;

import com.example.smartlearn.dto.StudentCourseDTO;
import com.example.smartlearn.model.ClassResource;
import com.example.smartlearn.repository.ClassResourceRepository;
import com.example.smartlearn.repository.StudentCourseRepository;
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
    @Value("${file.upload-dir}")
    private String uploadDir;
    @Autowired
    public CourseLearnService(StudentCourseRepository studentCourseRepository, ClassResourceRepository classResourceRepository) {
        this.studentCourseRepository = studentCourseRepository;
        this.classResourceRepository = classResourceRepository;
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






}
