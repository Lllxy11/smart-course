package com.example.smartlearn.controller.teacher;

import com.example.smartlearn.dto.response.Task_ResourceResponse;
import com.example.smartlearn.model.Task_Resource;
import com.example.smartlearn.service.teacher.Task_ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks/{taskId}/resources")
public class Task_ResourceController {

    @Autowired
    private Task_ResourceService taskResourceService;

    // 上传任务资源（支持多个文件）
    @PostMapping("/upload")
    public ResponseEntity<List<Task_ResourceResponse>> uploadResources(
            @PathVariable Long taskId,
            @RequestParam("teacherId") Long teacherId,
            @RequestParam("files") List<MultipartFile> files
    ) {
        System.out.println("接口 /upload 被调用！");
        List<Task_Resource> resources = taskResourceService.uploadTaskResources(taskId, files, teacherId);


        // 转换为Response对象，避免循环引用问题
        List<Task_ResourceResponse> responses = resources.stream()
                .map(resource -> {
                    Task_ResourceResponse response = new Task_ResourceResponse();
                    response.setId(resource.getId());
                    response.setName(resource.getName());
                    response.setFileType(resource.getFileType());
                    response.setFilePath(resource.getFilePath());
                    response.setUploadDate(resource.getUploadDate());
                    response.setUploaderId(resource.getUploader().getTeacherId());
                    return response;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);

        //return ResponseEntity.ok(resources);

    }

    // 获取某任务下的所有资源
    @GetMapping
    public ResponseEntity<List<Task_ResourceResponse>> getResourcesByTask(
            @PathVariable Long taskId
    ) {
        return ResponseEntity.ok(taskResourceService.getResourcesByTaskId(taskId));
    }



}
