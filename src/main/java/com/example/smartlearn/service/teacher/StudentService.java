package com.example.smartlearn.service.teacher;

import com.example.smartlearn.dto.StudentDTO;
import com.example.smartlearn.exception.ResourceNotFoundException;
import com.example.smartlearn.model.Course;
import com.example.smartlearn.model.Student;
import com.example.smartlearn.model.StudentCourse;
import com.example.smartlearn.repository.CourseRepository;
import com.example.smartlearn.repository.StudentCourseRepository;
import com.example.smartlearn.repository.StudentRepository;
import com.example.smartlearn.repository.UserRepository;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Service
public class StudentService {
    public static final Logger LOGGER = LoggerFactory.getLogger(StudentService.class);
    private final CourseRepository courseRepository;
    private final StudentRepository studentReposity;
    private final StudentCourseRepository studentCourseRepository;
    private final UserRepository userRepository;
    @Autowired
    public StudentService (CourseRepository courseRepository, StudentRepository studentRepository, StudentCourseRepository studentCourseRepository, UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.studentReposity = studentRepository;
        this.studentCourseRepository = studentCourseRepository;
        this.userRepository = userRepository;
    }
    @Transactional
    public Student createOrLinkStudent(String studentName, Long studentId, Long courseId) {
        // 1. 检查学生是否存在
        Student existingStudent = studentReposity.findById(studentId).orElse(null);
        Student studentToUse;

        if (existingStudent == null) {
            // 学生不存在，创建新学生
            Student newStudent = new Student();
            newStudent.setStudentId(studentId);
            newStudent.setStudentName(studentName);
            studentToUse = studentReposity.save(newStudent);
        } else {
            // 学生已存在，使用现有学生
            studentToUse = existingStudent;
        }

        // 2. 检查学生-课程关联是否存在
        Optional<StudentCourse> existingCourseStudent = studentCourseRepository
                .findByCourseIdAndStudentId(courseId, studentId);

        if (existingCourseStudent.isPresent()) {
            throw new IllegalArgumentException("学生已存在该课程中: " + studentId);
        }

        // 3. 创建新的学生-课程关联
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("课程不存在: " + courseId));

        StudentCourse newStudentCourse = new StudentCourse();
        newStudentCourse.setStudent(studentToUse);
        newStudentCourse.setCourse(course);
        newStudentCourse.setEnrollDate(LocalDateTime.now());
        studentCourseRepository.save(newStudentCourse);

        return studentToUse;
    }
    //批量导入学生信息
    public List<Student> BatchImportStudent(MultipartFile file,Long courseId){

        Course course=courseRepository.findById(courseId).orElseThrow(()
                ->new ResourceNotFoundException("Course not found"+courseId));
        List<Student> students=parseStudentFromExcel(file);
        List<Student> savedStudents=new ArrayList<>();
        for(Student student:students){
            try {
                Optional<Student> existingStudent = studentReposity.findById(student.getStudentId());
                //先检查学生是否已经存在 存在的话就不向学生表中添加 只向学生课程表中添加
                Student studentToLink = existingStudent.orElseGet(() -> studentReposity.save(student));

                if(studentCourseRepository.existsByStudentStudentIdAndCourseCourseId(studentToLink.getStudentId(),courseId)){
                    throw new IllegalArgumentException("学生已存在该课程中"+student.getStudentId());
                }
                StudentCourse studentCourse=new StudentCourse();
                studentCourse.setCourse(course);
                studentCourse.setStudent(studentToLink);
                studentCourse.setEnrollDate(LocalDateTime.now());
                studentCourseRepository.save(studentCourse);
                savedStudents.add(studentToLink);
                //增加选课记录
                if (studentToLink.getStudentCourseRecords() == null) {
                    studentToLink.setStudentCourseRecords(new ArrayList<>());
                }
                studentToLink.getStudentCourseRecords().add(studentCourse);
            }catch (Exception e){
                LOGGER.warn(e.getMessage());
            }
        }
        return savedStudents;
    }
    //解析表格
    private List<Student> parseStudentFromExcel(MultipartFile file){
//        if(file.isEmpty()){
//            throw new UploadException()
//        }

        List<Student> students=new ArrayList<>();

        try (InputStream inputStream=file.getInputStream();
             Workbook workbook=getWorkbook(file.getOriginalFilename(), inputStream)){
            Sheet sheet=workbook.getSheetAt(0);
            for(Row row:sheet){
                if(row.getRowNum()==0){
                    continue;//跳过表头
                }
                Student student=new Student();
                Long studentId= Long.valueOf(getStringValue(row.getCell(0)));
                student.setStudentId(studentId);
                student.setStudentName(getStringValue(row.getCell(1)));
                students.add(student);


            }
        }catch (Exception e){
            throw new ResourceNotFoundException("Excel file import");
        }
        return students;
    }

    private Workbook getWorkbook(String fileName,InputStream inputStream) throws IOException {
        if (fileName.endsWith(".xlsx")) {
            return new XSSFWorkbook(inputStream);
        }else if (fileName.endsWith(".xls")) {
            return new HSSFWorkbook(inputStream);
        }else {
            throw new IllegalArgumentException("Invalid file name");
        }

    }

    private String getStringValue(Cell cell) {
        if(cell==null){
            return null;
        }
        switch (cell.getCellTypeEnum()) {
            case STRING:return cell.getStringCellValue().trim();
            case NUMERIC:return String.valueOf((int) cell.getNumericCellValue());
            default:return null;
        }


    }
    @Transactional
    //删除一条课程记录，同时删除对应学生的选课记录
    public void deleteStudent(Long studentId, Long courseId) {
        if (!studentCourseRepository.existsByStudentStudentIdAndCourseCourseId(studentId, courseId)) {
            throw new ResourceNotFoundException(
                    String.format("Student %s not enrolled in course %s", studentId, courseId)
            );
        }
        studentCourseRepository.deleteByStudentStudentIdAndCourseCourseId(studentId, courseId);
    }

    @Transactional
    public Page<StudentDTO> getStudentsByCourseId(Long courseId, Pageable pageable) {
        // 使用JPQL方法
        Page<Student> students = studentReposity.findByCourseId(courseId, pageable);

        // 转换为DTO
        return students.map(s -> new StudentDTO(
                s.getStudentId(),
                s.getStudentName()
        ));
    }
    public Optional<Student> getStudentById(Long studentId, Long courseId){
        if (studentId == null || courseId == null) {
            throw new IllegalArgumentException("Student ID and Course ID must not be null");
        }

        if(studentCourseRepository.existsByStudentStudentIdAndCourseCourseId(studentId,courseId)){
            return studentReposity.findById(studentId);
        }else {
            return Optional.empty();

        }

    }

    @Transactional
    public List<StudentDTO> exportStudentByCourse(Long courseId){
        System.out.println('A');
        List<Student> students=studentReposity.findByCourse_Id(courseId,Pageable.unpaged()).getContent();
        return students.stream().map(this::convertToExportDTO).collect(Collectors.toList());
    }

    private StudentDTO convertToExportDTO(Student student){
        System.out.println('B');
        StudentDTO studentDTO=new StudentDTO(student.getStudentId(), student.getStudentName());
        return studentDTO;

    }

}
