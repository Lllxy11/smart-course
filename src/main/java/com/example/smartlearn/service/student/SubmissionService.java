package com.example.smartlearn.service.student;

import com.example.smartlearn.dto.request.SubmissionRequest;
import com.example.smartlearn.exception.ResourceNotFoundException;
import com.example.smartlearn.model.Student;
import com.example.smartlearn.model.Submission;
import com.example.smartlearn.model.Task;
import com.example.smartlearn.repository.StudentRepository;
import com.example.smartlearn.repository.SubmissionRepository;
import com.example.smartlearn.repository.TaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class SubmissionService {

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 提交任务
     */
    @Transactional
    public void submitTask(SubmissionRequest request) throws IOException {
        System.out.println("开始提交任务，taskId: " + request.getTaskId() + ", studentId: " + request.getStudentId());

        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("任务不存在"));

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("学生不存在"));

        // 检查任务是否已过期
        if (task.getDueDate() != null && task.getDueDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("任务已过期，无法提交");
        }

        // 检查是否已提交过
        if (submissionRepository.existsByTaskIdAndStudentStudentId(task.getId(), student.getStudentId())) {
            throw new IllegalArgumentException("已提交过该任务");

        }
        // 添加任务类型验证
        if (task.getType() == Task.TaskType.HOMEWORK && request.getFiles() != null && !request.getFiles().isEmpty()) {
            throw new IllegalArgumentException("作业类型任务只能提交文本内容");
        }

        if (task.getType() == Task.TaskType.REPORT &&
                (request.getContent() != null && !request.getContent().isEmpty())) {
            throw new IllegalArgumentException("报告类型任务只能提交文件");
        }

        Submission submission = new Submission();
        submission.setTask(task);
        submission.setStudent(student);

        // 根据任务类型处理不同提交内容
        switch (task.getType()) {
            case HOMEWORK:
                handleHomeworkSubmission(request, submission);
                break;
            case REPORT:
                handleReportSubmission(request, submission);
                break;
            // case QUIZ:
            //     throw new IllegalArgumentException("测验类型任务暂不支持提交");
            default:
                throw new IllegalArgumentException("未知的任务类型");
        }

        submission.setGrade(BigDecimal.ZERO); // 初始分数为0
        submissionRepository.save(submission);
    }

    private void handleHomeworkSubmission(SubmissionRequest request, Submission submission) {
        if (request.getContent() == null || request.getContent().isEmpty()) {
            throw new IllegalArgumentException("作业内容不能为空");
        }
        submission.setContent(request.getContent());
    }

    // 文件元信息类
    public static class FileMeta {
        private int index;
        private String original;
        private String saved;

        public FileMeta() {}

        public FileMeta(int index, String original, String saved) {
            this.index = index;
            this.original = original;
            this.saved = saved;
        }

        public int getIndex() { return index; }
        public void setIndex(int index) { this.index = index; }
        public String getOriginal() { return original; }
        public void setOriginal(String original) { this.original = original; }
        public String getSaved() { return saved; }
        public void setSaved(String saved) { this.saved = saved; }
    }

    private void handleReportSubmission(SubmissionRequest request, Submission submission) throws IOException {
        System.out.println("处理报告提交，文件数量: " + (request.getFiles() != null ? request.getFiles().size() : 0));

        java.util.List<MultipartFile> files = request.getFiles();
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("请上传报告文件");
        }
        java.util.List<FileMeta> fileMetas = new java.util.ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            String originalFilename = file.getOriginalFilename();
            System.out.println("处理文件 " + i + ": " + originalFilename);
            if (originalFilename == null ||
                    !(originalFilename.toLowerCase().endsWith(".docx") ||
                            originalFilename.toLowerCase().endsWith(".pdf"))) {
                throw new IllegalArgumentException("只支持word(.docx)或pdf(.pdf)格式文件");
            }
            System.out.println("上传目录: " + uploadDir);
            File uploadDirectory = new File(uploadDir);
            if (!uploadDirectory.exists()) {
                boolean created = uploadDirectory.mkdirs();
                System.out.println("创建上传目录结果: " + created);
                if (!created) {
                    throw new RuntimeException("无法创建上传目录: " + uploadDir);
                }
            }
            String ext = originalFilename.substring(originalFilename.lastIndexOf('.'));
            String fileName = "report_" + submission.getStudent().getStudentId() +
                    "_" + submission.getTask().getId() +
                    "_" + System.currentTimeMillis() + ext;
            String filePath = uploadDir + File.separator + fileName;
            System.out.println("保存文件路径: " + filePath);
            File dest = new File(filePath);
            file.transferTo(dest);
            System.out.println("文件保存成功: " + filePath);
            fileMetas.add(new FileMeta(i, originalFilename, "/uploads/" + fileName));
        }
        // 存为json字符串
        submission.setFilePath(objectMapper.writeValueAsString(fileMetas));
    }
}



//     // 删除单个或多个文件（已提交后不能更改）
//     @Transactional
//     public void deleteSubmissionFiles(Long submissionId, java.util.List<String> fileNamesToDelete) throws IOException {
//         Submission submission = submissionRepository.findById(submissionId)
//             .orElseThrow(() -> new RuntimeException("提交记录不存在"));
//         // 禁止删除，已提交后不能更改
//         throw new IllegalArgumentException("已提交，不能更改");
//         // 如需允许删除，放开下面注释
//         /*
//         java.util.List<String> filePaths = objectMapper.readValue(submission.getFilePath(), new TypeReference<java.util.List<String>>() {});
//         for (String filePath : filePaths) {
//             String fileName = new java.io.File(filePath).getName();
//             if (fileNamesToDelete.contains(fileName)) {
//                 File localFile = new File(uploadDir + File.separator + fileName);
//                 if (localFile.exists()) localFile.delete();
//             }
//         }
//         filePaths.removeIf(path -> fileNamesToDelete.contains(new java.io.File(path).getName()));
//         submission.setFilePath(objectMapper.writeValueAsString(filePaths));
//         submissionRepository.save(submission);
//         */
//     }
// }