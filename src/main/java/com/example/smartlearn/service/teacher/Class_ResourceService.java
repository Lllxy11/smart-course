package com.example.smartlearn.service.teacher;


import com.example.smartlearn.controller.teacher.Class_ResourceController;
import com.example.smartlearn.exception.ResourceNotFoundException;
import com.example.smartlearn.model.ClassResource;
import com.example.smartlearn.repository.ClassResourceRepository;
import com.example.smartlearn.repository.CourseRepository;
import com.example.smartlearn.util.VideoDurationUtil;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class Class_ResourceService {

    private final ClassResourceRepository resourceRepository;
    private static final Logger log = LoggerFactory.getLogger(Class_ResourceService.class);
    @Value("${file.upload-dir}")
    private String uploadDir;
    @Value("${file.max-size:104857600}") // 默认100MB
    private long maxFileSize;
    private static final Logger logger = LoggerFactory.getLogger(Class_ResourceController.class);
    private final CourseRepository courseRepository;
    public Class_ResourceService(ClassResourceRepository resourceRepository, CourseRepository courseRepository) {
        this.resourceRepository = resourceRepository;
        this.courseRepository = courseRepository;
    }

    // 增：上传新资源
    public ClassResource uploadResource(Integer courseId, String name,
                                         ClassResource.ResourceType type,
                                         MultipartFile file,
                                         String description) throws IOException {
        // 生成唯一文件名
        String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        // 根据类型确定子目录
        String subDir = type.name().toLowerCase();

        // 创建目标路径 (使用相对路径)
        Path targetLocation = Paths.get(uploadDir)
                .resolve("courses")
                .resolve(courseId.toString())
                .resolve("resources")
                .resolve(subDir);

        // 确保目录存在
        System.out.println(courseId);
        Files.createDirectories(targetLocation);

        // 创建文件路径
        Path filePath = targetLocation.resolve(filename);

        // 保存文件
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // 创建资源对象
        ClassResource resource = new ClassResource();
        resource.setCourseId(courseId);
        resource.setName(name);
        resource.setType(type);
        // 如果是视频类型，提取视频时长
        if (type == ClassResource.ResourceType.video) {
            try {
                // 使用工具类获取视频时长
                Integer duration = VideoDurationUtil.getVideoDuration(filePath.toFile());
                resource.setDuration(duration);
            } catch (Exception e) {
                // 如果获取时长失败，可以记录日志或设置默认值
                resource.setDuration(0);
                log.error("Failed to get video duration", e);
                System.out.println("Failed to get video duration");
            }
        }


        // 创建相对路径 (作为URL)
        String relativePath = "courses/" + courseId + "/resources/" + subDir + "/" + filename;

        // 设置URL为完整的访问路径
        resource.setUrl("/uploads/" + relativePath);

        resource.setCreatedAt(LocalDateTime.now());
        resource.setDescription(description);

        // 保存到数据库
        return resourceRepository.save(resource);
    }
    /**
     * 将本地路径转换回数据库存储的URL格式
     * @param localPath 本地绝对路径（从Python返回的）
     * @return 数据库中的URL格式（如"/uploads/courses/1/resources/ppt/filename.pptx"）
     */
    public String convertLocalPathToUrl(String localPath) {
        // 标准化路径处理（兼容不同操作系统）
        Path path = Paths.get(localPath).normalize();

        // 找到关键路径部分（从"courses"开始）
        int coursesIndex = path.toString().indexOf("courses");
        if (coursesIndex == -1) {
            throw new IllegalArgumentException("无效的课程资源路径: " + localPath);
        }

        // 提取相对路径部分
        String relativePath = path.toString().substring(coursesIndex);

        // 转换为URL格式（统一使用正斜杠）
        return "/uploads/" + relativePath.replace(File.separator, "/");
    }
    //删除资源
    /**
     * 删除资源（包含物理文件和数据库记录）
     *
     * @param resourceId 资源ID
     * @throws ResourceNotFoundException 如果资源未找到
     * @throws IOException 如果删除文件失败
     */
    public void deleteResource(Integer resourceId) throws ResourceNotFoundException, IOException {
        // 1. 查找资源
        ClassResource resource = getResourceById(resourceId);

        log.info("开始删除资源: ID={}, 名称={}", resourceId, resource.getName());

        // 2. 获取文件路径
        String url = resource.getUrl();
        String relativePath = url.startsWith("/uploads/") ?
                url.substring("/uploads/".length()) :
                url;
        Path filePath = Paths.get(uploadDir, relativePath);

        // 3. 删除物理文件
        if (Files.exists(filePath)) {
            log.info("删除物理文件: 路径={}", filePath);
            Files.delete(filePath);
        } else {
            log.warn("物理文件不存在: 路径={}", filePath);
        }

        // 4. 删除数据库记录
        log.info("删除数据库记录: ID={}", resourceId);
        resourceRepository.delete(resource);
    }



    // 改：更新资源信息
    public ClassResource updateResource(Integer resourceId, String name, String description) {
        ClassResource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found"));

        // 调试信息
        System.out.println("更新前资源信息:");
        System.out.println("ID: " + resource.getResourceId());
        System.out.println("名称: " + resource.getName());
        System.out.println("描述: " + resource.getDescription());

        // 添加空值检查和修剪空白字符
        if (name != null && !name.trim().isEmpty()) {
            resource.setName(name.trim());
        } else {
            // 如果不提供名称，使用原始名称
            name = resource.getName();
        }

        if (description != null) {
            // 允许空描述
            resource.setDescription(description.trim());
        } else {
            // 如果不提供描述，使用原始描述
            description = resource.getDescription();
        }

        // 保存前打印更新后的值
        System.out.println("更新后资源信息:");
        System.out.println("名称: " + name);
        System.out.println("描述: " + description);

        // 确保保存并返回更新后的对象
        ClassResource updated = resourceRepository.save(resource);

        System.out.println("数据库返回的更新资源:");
        System.out.println("名称: " + updated.getName());
        System.out.println("描述: " + updated.getDescription());

        return updated;
    }
    // 更新资源文件
    @Transactional
    public ClassResource updateResourceFile(Integer resourceId, MultipartFile file,Integer courseId) throws IOException {
        // 1. 获取资源但不要修改它（防止实体过早加载）
        ClassResource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("资源不存在, ID: " + resourceId));

        if (resource.getType() == null) {
            // 尝试确定资源类型
            String type = determineResourceType(file.getOriginalFilename());
            if (type == null) {
                throw new IllegalStateException("无法确定资源类型，请手动设置");
            }
            resource.setType(ClassResource.ResourceType.valueOf(type.toUpperCase()));
        }

        // 2. 获取必要信息（避免加载整个资源对象）


        ClassResource.ResourceType type = resource.getType();
        if (!courseId.equals(resource.getCourseId())) {
            resource.setCourseId(courseId);
            resourceRepository.save(resource);
            logger.info("更新资源课程ID: {} -> {}", resource.getCourseId(), courseId);
        }

        // 3. 创建新文件路径
        String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        String subDir = type.name().toLowerCase();
        Path newFilePath = Paths.get(uploadDir)
                .resolve("courses")
                .resolve(courseId.toString())
                .resolve("resources")
                .resolve(subDir)
                .resolve(filename);

        // 4. 确保目录存在
        Files.createDirectories(newFilePath.getParent());

        // 5. 先保存新文件
        try (InputStream is = file.getInputStream()) {
            Files.copy(is, newFilePath, StandardCopyOption.REPLACE_EXISTING);
        }

        // 6. 构建新URL
        String newRelativePath = "courses/" + courseId + "/resources/" + subDir + "/" + filename;
        String newUrl = "/uploads/" + newRelativePath;

        // 7. 删除旧文件（如果存在）
        if (resource.getUrl() != null && !resource.getUrl().isEmpty()) {
            try {
                Path oldFilePath = Paths.get(uploadDir, resource.getUrl().substring("/uploads/".length()));
                Files.deleteIfExists(oldFilePath);
            } catch (IOException e) {
                logger.warn("无法删除旧文件: {}", resource.getUrl());
            }
        }

        // 8. 使用直接SQL更新URL（避免Hibernate状态问题）
        resourceRepository.updateResourceUrl(resourceId, newUrl);

        // 9. 返回更新后的资源
        return resourceRepository.findById(resourceId)
                .orElseThrow(()-> new ResourceNotFoundException("资源不存在"));
    }

    private String determineResourceType(String filename) {
        if (filename == null) return null;

        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();

        switch (extension) {
            case "pdf": return "PDF";
            case "ppt": case "pptx": return "PPT";
            case "doc": case "docx": return "DOC";
            case "mp4": case "mov": case "avi": return "VIDEO";
            default: return null;
        }
    }
    //返回在初始化知识图谱时选中的资源列表
    public List<String> getCourseResourcePathByIds(List<Long> resourceIds) {
        System.out.println("开始利用选中的资源生成知识图谱");
        List<ClassResource> resources = resourceRepository.findByResourceIds(resourceIds);
        System.out.println("<UNK>"+resources.size());
        List<String> localPaths=new ArrayList<>();
        System.out.println(localPaths.size());
        System.out.println(localPaths);
        for (ClassResource resource:resources){
            if(resource.getType() == ClassResource.ResourceType.ppt ||
                    resource.getType() == ClassResource.ResourceType.pdf ||
                    resource.getType() == ClassResource.ResourceType.doc){
                String localPath=convertUrlToLocalPath(resource.getUrl());
                localPaths.add(localPath);

            }
        }
        return localPaths;
    }
    public List<String> getCourseResourceLocalPath(Long courseId){
        System.out.println("开始进行");
        List<ClassResource> resources=resourceRepository.findByCourseId(courseId.intValue());
        System.out.println("<UNK>"+resources.size());
        List<String> localPaths=new ArrayList<>();
        System.out.println(localPaths.size());
        System.out.println(localPaths);
        for (ClassResource resource:resources){
            if(resource.getType() == ClassResource.ResourceType.ppt ||
                    resource.getType() == ClassResource.ResourceType.pdf ||
                    resource.getType() == ClassResource.ResourceType.doc){
                String localPath=convertUrlToLocalPath(resource.getUrl());
                localPaths.add(localPath);

            }
        }
        return localPaths;

    }
    private String convertUrlToLocalPath(String resourceUrl){
        String relativePath = resourceUrl.replaceFirst("/uploads/", "");
        return Paths.get(uploadDir, relativePath).toString().replace("/", File.separator);
    }





    // 查：根据条件查询资源
    public List<ClassResource> getResources(Integer courseId, ClassResource.ResourceType type, String keyword) {
        if (courseId != null && type != null && keyword != null) {
            return resourceRepository.findByCourseIdAndTypeAndNameContaining(courseId, type, keyword);
        } else if (courseId != null && type != null) {
            return resourceRepository.findByCourseIdAndType(courseId, type);
        } else if (courseId != null) {
            return resourceRepository.findByCourseId(courseId);
        } else if (type != null) {
            return resourceRepository.findByType(type);
        } else if (keyword != null) {
            return resourceRepository.findByNameContaining(keyword);
        } else {
            return resourceRepository.findAll();
        }
    }

    // 添加文件分类方法
    public List<ClassResource> getResourcesByCategory(String category, Integer courseId) {
        return resourceRepository.findByTypeAndCourseId(ClassResource.ResourceType.valueOf(category), courseId);
    }

    // 根据ID获取资源
    public ClassResource getResourceById(Integer resourceId) {
        return resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found"));
    }

    public InputStream downloadResource(Integer resourceId) throws IOException {
        ClassResource resource = getResourceById(resourceId);

        // 从URL中提取相对路径
        String url = resource.getUrl();
        String relativePath = url.startsWith("/uploads/") ? url.substring("/uploads/".length()) : url;

        // 构建完整文件路径
        Path fullPath = Paths.get(uploadDir).resolve(relativePath);

        // 返回文件流
        return new FileInputStream(fullPath.toFile());
    }

    // 获取教师的所有资源
    public List<ClassResource> getTeacherResources(Integer teacherId,
                                              Integer courseId,
                                                    ClassResource.ResourceType type,
                                              String keyword) {

        // 1. 获取该教师的所有课程ID
        List<Integer> courseIds = courseRepository.findIdsByTeacherId(teacherId);

        if (courseIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 如果指定了课程ID，确保该课程属于该教师
        if (courseId != null) {
            if (!courseIds.contains(courseId)) {
                try {
                    throw new AccessDeniedException("无权访问此课程资源");
                } catch (AccessDeniedException e) {
                    throw new RuntimeException(e);
                }
            }
            courseIds = Collections.singletonList(courseId);
        }

        // 3. 查询资源
        return resourceRepository.findByCourseIdsAndTypeAndKeyword(
                courseIds, type, keyword
        );
    }

    public List<ClassResource> getAllTeacherResources(
            Integer teacherId,
            ClassResource.ResourceType type,
            String keyword) {

        // 1. 获取教师的所有课程ID
        List<Integer> courseIds = courseRepository.findIdsByTeacherId(teacherId);

        if (courseIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 直接使用课程ID列表查询资源
        return resourceRepository.findByCourseIdsAndTypeAndKeyword(
                courseIds,
                type,
                keyword
        );
    }

    public List<ClassResource> getAllResources(Integer courseId) {
        return resourceRepository.findByCourseId(courseId);
    }



    @org.springframework.transaction.annotation.Transactional
    public List<ClassResource> getCourseResource(Long courseId,ClassResource.ResourceType type){
        return resourceRepository.findByCourseIdAndType(courseId.intValue(),type);

    }


}