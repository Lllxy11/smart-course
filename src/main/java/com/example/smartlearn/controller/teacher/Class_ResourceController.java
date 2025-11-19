package com.example.smartlearn.controller.teacher;

import com.example.smartlearn.dto.request.Class_ResourceUpdateRequest;
import com.example.smartlearn.exception.ResourceNotFoundException;
import com.example.smartlearn.model.ClassResource;
import com.example.smartlearn.service.teacher.Class_ResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/resources")

public class Class_ResourceController {

    private final Class_ResourceService resourceService;
    private static final Logger log = LoggerFactory.getLogger(Class_ResourceService.class);
    public Class_ResourceController(Class_ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @Value("${file.upload-dir}")
    private String uploadDir;
    @Value("${file.max-size:104857600}") // 默认100MB
    private long maxFileSize;
    private static final Logger logger = LoggerFactory.getLogger(Class_ResourceController.class);

    // 上传资源
    @PostMapping("/upload")
    public ResponseEntity<ClassResource> uploadResource(
            @RequestParam("courseId") Integer courseId,
            @RequestParam("name") String name,
            @RequestParam("type") String type,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description) {

        try {
            System.out.println(courseId);
            ClassResource.ResourceType resourceType = ClassResource.ResourceType.valueOf(type);
            ClassResource resource = resourceService.uploadResource(
                    courseId, name, resourceType, file, description
            );
            return new ResponseEntity<>(resource, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    // 删除资源
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResource(@PathVariable Integer id) {
        try {
            resourceService.deleteResource(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 更新资源元数据
    @PutMapping("/{id}")
    public ResponseEntity<ClassResource> updateResource(
            @PathVariable Integer id,
            @RequestBody Class_ResourceUpdateRequest updateRequest) { // 修改为 @RequestBody

        // 验证资源ID
        if (id == null || id <= 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            ClassResource updatedResource = resourceService.updateResource(
                    id,
                    updateRequest.getName(),
                    updateRequest.getDescription()
            );
            return new ResponseEntity<>(updatedResource, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    // 更新资源文件
    @PutMapping("/{id}/file")
    public ResponseEntity<?> updateResourceFile(
            @PathVariable Integer id,
            @RequestParam("file") MultipartFile file,
            @RequestParam("courseId") Integer courseId) {

        try {
            ClassResource updatedResource = resourceService.updateResourceFile(id, file, courseId);
            return ResponseEntity.ok(updatedResource);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("文件处理失败: " + e.getMessage());
        } catch (IllegalStateException e) {
            // 处理资源缺少必要信息的错误
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(e.getMessage());
        } catch (Exception e) {
            logger.error("文件更新失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("服务器内部错误");
        }
    }

    // 获取资源列表（支持分类和搜索）
    @GetMapping
    public ResponseEntity<List<ClassResource>> getResources(
            @RequestParam(value = "courseId", required = false) Integer courseId,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) String category) {

        // 如果指定了category，优先使用
        if (category != null && courseId != null) {
            return ResponseEntity.ok(resourceService.getResourcesByCategory(category, courseId));
        }

        try {
            ClassResource.ResourceType resourceType = null;
            if (type != null) {
                resourceType = ClassResource.ResourceType.valueOf(type);
            }
            List<ClassResource> resources = resourceService.getResources(courseId, resourceType, keyword);
            return new ResponseEntity<>(resources, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 新增：专用下载端点
    @GetMapping("/download/{id}")
    public ResponseEntity<org.springframework.core.io.Resource> downloadResource(@PathVariable Integer id) {
        try {
            ClassResource resource = resourceService.getResourceById(id);
            if (resource == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            // 从URL中提取相对路径
            String url = resource.getUrl();
            // 移除URL前缀 ("/uploads/")
            String relativePath = url.startsWith("/uploads/") ? url.substring("/uploads/".length()) : url;

            // 构建完整文件路径
            Path fullPath = Paths.get(uploadDir).resolve(relativePath);

            // 验证文件存在
            if (!Files.exists(fullPath)) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            // 创建文件资源
            org.springframework.core.io.Resource fileResource = new FileSystemResource(fullPath.toFile());

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" +
                    URLEncoder.encode(resource.getName() + getExtension(resource.getType()), "UTF-8"));
            headers.add(HttpHeaders.CONTENT_TYPE, Files.probeContentType(fullPath));
            headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(fullPath.toFile().length()));

            return new ResponseEntity<>(fileResource, headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 辅助方法：根据类型获取文件扩展名
    private String getExtension(ClassResource.ResourceType type) {
        if (type == null) return "";

        switch (type) {
            case ppt: return ".pptx";
            case pdf: return ".pdf";
            case video: return ".mp4";
            case doc: return ".docx";
            default: return "";
        }
    }

    // 获取教师资源列表
    @GetMapping("/teacher")
    public ResponseEntity<List<ClassResource>> getTeacherResources(
            @RequestParam(value = "teacherId", required = true) Integer teacherId,
            @RequestParam(value = "courseId", required = false) Integer courseId,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "keyword", required = false) String keyword) {

        logger.info("处理教师资源请求 - 教师ID: {}, 课程ID: {}, 类型: {}, 关键词: {}",
                teacherId, courseId, type, keyword);

        try {
            // 转换字符串参数为ResourceType枚举
            ClassResource.ResourceType resourceType = null;
            if (type != null) {
                try {
                    resourceType = ClassResource.ResourceType.valueOf(type.toUpperCase());
                } catch (IllegalArgumentException e) {
                    logger.warn("无效的资源类型: {}", type);
                    return ResponseEntity.ok(Collections.emptyList());
                }
            }

            List<ClassResource> resources = resourceService.getTeacherResources(
                    teacherId, courseId, resourceType, keyword
            );

            logger.info("找到 {} 个资源", resources.size());
            return ResponseEntity.ok(resources);
        } catch (Exception e) {
            logger.error("获取教师资源失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/teacher/all")
    public ResponseEntity<List<ClassResource>> getAllTeacherResources(
            @RequestParam(value = "teacherId", required = true) Integer teacherId,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "keyword", required = false) String keyword) {

        logger.info("获取教师所有资源 - 教师ID: {}", teacherId);

        try {
            // 转换字符串参数为ResourceType枚举
            ClassResource.ResourceType resourceType = null;
            if (type != null) {
                try {
                    resourceType = ClassResource.ResourceType.valueOf(type.toUpperCase());
                } catch (IllegalArgumentException e) {
                    logger.warn("无效的资源类型: {}", type);
                    return ResponseEntity.ok(Collections.emptyList());
                }
            }

            // 获取教师所有资源（不限制课程）
            List<ClassResource> resources = resourceService.getAllTeacherResources(
                    teacherId, resourceType, keyword
            );

            logger.info("找到 {} 个资源", resources.size());
            return ResponseEntity.ok(resources);
        } catch (Exception e) {
            logger.error("获取教师资源失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<ClassResource>> getCourseResources(@PathVariable Long courseId,
                                                                  @RequestParam ClassResource.ResourceType type) {
        List<ClassResource> resources=resourceService.getCourseResource(courseId, type);
        System.out.println("<UNK>"+resources.size());
        return ResponseEntity.ok(resourceService.getCourseResource(courseId, type));

    }




}