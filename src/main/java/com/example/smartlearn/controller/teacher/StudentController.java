package com.example.smartlearn.controller.teacher;

import ch.qos.logback.classic.Logger;
import com.example.smartlearn.dto.StudentDTO;
import com.example.smartlearn.exception.ResourceNotFoundException;
import com.example.smartlearn.model.Student;
import com.example.smartlearn.repository.StudentRepository;
import com.example.smartlearn.service.teacher.StudentService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/course/{courseId}/student")
public class StudentController {
    private final StudentRepository studentReposity;
    private final StudentService studentService;
    private Logger log;

    @Autowired
    public StudentController(StudentRepository studentRepository, StudentService studentService){
        this.studentReposity= studentRepository;
        this.studentService=studentService;
    }

    //添加一个学生

    @PostMapping("/add")  // 修改为明确的/add路径
    public ResponseEntity<?> addStudent(
            @RequestParam Long courseId,    // 改为RequestParam
            @RequestParam Long studentId,   // 改为Long类型
            @RequestParam String studentName) {

        try {
            System.out.println("接收参数 - courseId:" + courseId + ", studentId:" + studentId + ", studentName:" + studentName);
            Student student = studentService.createOrLinkStudent(studentName, studentId, courseId);
            return ResponseEntity.ok().body(Map.of(
                    "success", true,
                    "student", student,
                    "message", "创建成功"
            ));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "学生未找到"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    @PostMapping("/batch-import")
    public ResponseEntity<?> importStudents(@PathVariable Long courseId,
                                            @RequestParam("file")MultipartFile file){
        try {
            List<Student> importedStudetnt=studentService.BatchImportStudent(file,courseId);
            return ResponseEntity.ok().body(Map.of(
                    "success",true,
                    "importedStudetnt",importedStudetnt.size(),
                    "message","import successfully"
            ));
        }catch (ResourceNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success",false,
                    "message",e.getMessage()
            ));
        }catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(Map.of(
                    "success",false,
                    "message",e.getMessage()
            ));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(Map.of(
                    "success",false,
                    "message",e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{studentId}")
    public ResponseEntity<Void> removeStudentFromCourse(
            @PathVariable Long courseId,
            @PathVariable Long studentId
    ) {
        studentService.deleteStudent(studentId, courseId);
        return ResponseEntity.noContent().build();
    }
    @GetMapping

    public ResponseEntity<Page<StudentDTO>> getStudentsByCourse(
            @PathVariable Long courseId,
            @PageableDefault(
                    size = 10,
                    sort = "studentId",
                    direction = Sort.Direction.ASC ) Pageable pageable) {
        return ResponseEntity.ok(studentService.getStudentsByCourseId(courseId, pageable));
    }


    @GetMapping("/{studentId}")
    public ResponseEntity<?> getStudentById(
            @PathVariable Long courseId,
            @PathVariable Long studentId) {

        try {
            Optional<Student> student = studentService.getStudentById(studentId, courseId);

            return student
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/export")
    public ResponseEntity<ByteArrayResource> exportStudents(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "excel") String format
    )throws IOException{
        List<StudentDTO> students=studentService.exportStudentByCourse(courseId);
        System.out.println('C');
        // 2. 生成Excel文件
        ByteArrayResource resource = new ByteArrayResource(generateExcel(students));
        System.out.println('D');
        byte[] excelBytes = generateExcel(students);
        System.out.println("4. 文件大小：" + excelBytes.length + " bytes");

        // 3. 设置响应头
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"students_" + courseId + ".xlsx\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(excelBytes.length) // 明确内容长度
                .body(resource);

    }
    private byte[] generateExcel(List<StudentDTO> students) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("学生列表");

            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"学号", "姓名"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            // 填充数据
            int rowNum = 1;
            for (StudentDTO student : students) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(student.getStudentId());
                row.createCell(1).setCellValue(student.getStudentName());
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 写入字节数组
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

}
