package com.example.smartlearn.service.teacher;

import com.example.smartlearn.dto.response.Task_ResourceResponse;
import com.example.smartlearn.exception.ResourceNotFoundException;
import com.example.smartlearn.model.Task;
import com.example.smartlearn.model.Task_Resource;
import com.example.smartlearn.model.Teacher;
import com.example.smartlearn.repository.TaskRepository;
import com.example.smartlearn.repository.Task_ResourceRepository;
import com.example.smartlearn.repository.TeacherRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@EnableTransactionManagement
public class Task_ResourceService {

    @Autowired
    private Task_ResourceRepository taskResourceRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    //private final String uploadBasePath = "D:/shixunfile"; // 可改为 application.properties 里的 ${file.upload-dir}

    @Value("${file.upload-dir}")
    private String uploadBasePath;

//    @Transactional
//    public List<Task_Resource> uploadTaskResources(Long taskId, List<MultipartFile> files, Long teacherId) {
//        Task task = taskRepository.findById(taskId)
//                .orElseThrow(() -> new ResourceNotFoundException("任务不存在: " + taskId));
//
//        Teacher teacher = teacherRepository.findById(teacherId)
//                .orElseThrow(() -> new ResourceNotFoundException("教师不存在: " + teacherId));
//
//        List<Task_Resource> savedResources = new ArrayList<>();
//        for (MultipartFile file : files) {
//            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
//            String filePath = uploadBasePath + "/" + fileName;
//            File dest = new File(filePath);
//            try {
//                file.transferTo(dest);
//            } catch (Exception e) {
//                throw new RuntimeException("上传失败: " + file.getOriginalFilename(), e);
//            }
//
//            Task_Resource resource = new Task_Resource();
//            resource.setName(file.getOriginalFilename());
//            resource.setFileType(file.getContentType());
//            resource.setFilePath("/uploads/" + fileName); // 供前端访问
//            resource.setUploadDate(LocalDateTime.now());
//            resource.setTask(task);
//            resource.setCourse(task.getCourse());
//            resource.setUploader(teacher);
//
//            savedResources.add(taskResourceRepository.save(resource));
//        }
//        return savedResources;
//    }

    @Transactional
    public List<Task_Resource> uploadTaskResources(Long taskId, List<MultipartFile> files, Long teacherId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("任务不存在: " + taskId));

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("教师不存在: " + teacherId));

        List<Task_Resource> savedResources = new ArrayList<>();

        File uploadDir = new File(uploadBasePath);
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            System.out.println("上传目录不存在，创建结果: " + created);
            if (!created) {
                throw new RuntimeException("上传目录创建失败");
            }
        }

        for (MultipartFile file : files) {
            System.out.println("开始上传文件: " + file.getOriginalFilename());

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String filePath = uploadBasePath + "/" + fileName;
            File dest = new File(filePath);

            try {
                System.out.println("准备写入文件到: " + filePath);
                file.transferTo(dest);
                System.out.println("写入文件成功: " + filePath);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("上传失败: " + file.getOriginalFilename(), e);
            }

            Task_Resource resource = new Task_Resource();
            resource.setName(file.getOriginalFilename());
            resource.setFileType(file.getContentType());
            resource.setFilePath("http://localhost:8080/uploads/" + fileName);
            resource.setUploadDate(LocalDateTime.now());
            resource.setTask(task);
            resource.setCourse(task.getCourse());
            resource.setUploader(teacher);

            Task_Resource saved = taskResourceRepository.save(resource);
            taskResourceRepository.flush();
            System.out.println("保存资源成功，ID=" + saved.getId());

            savedResources.add(saved);
        }

        return savedResources;
    }




    public List<Task_ResourceResponse> getResourcesByTaskId(Long taskId) {
        List<Task_Resource> list = taskResourceRepository.findByTaskId(taskId);
        List<Task_ResourceResponse> responses = new ArrayList<>();
        for (Task_Resource res : list) {
            Task_ResourceResponse r = new Task_ResourceResponse();
            r.setId(res.getId());
            r.setName(res.getName());
            r.setFileType(res.getFileType());
            r.setFilePath(res.getFilePath());
            r.setUploadDate(res.getUploadDate());
            r.setUploaderId(res.getUploader().getTeacherId());
            responses.add(r);
        }
        return responses;
    }
}
