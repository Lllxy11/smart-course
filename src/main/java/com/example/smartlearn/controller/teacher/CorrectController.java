package com.example.smartlearn.controller.teacher;

import com.example.smartlearn.dto.request.CorrectGradeRequest;
import com.example.smartlearn.dto.request.GradeQuizShortAnswerRequest;
import com.example.smartlearn.dto.response.CorrectDetailResponse;
import com.example.smartlearn.dto.response.CorrectResponse;
import com.example.smartlearn.dto.response.SubmissionFileResponse;
import com.example.smartlearn.dto.response.UngradedQuizAnswerResponse;
import com.example.smartlearn.model.Submission;
import com.example.smartlearn.repository.CorrectRepository;
import com.example.smartlearn.service.teacher.CorrectService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api/correct")
public class CorrectController {

    @Autowired
    private CorrectService correctService;
    
    @Autowired
    private CorrectRepository correctRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 查询某任务下所有提交
    @GetMapping("/tasks/{taskId}/list")
    public List<CorrectResponse> getListByTask(@PathVariable Long taskId) {
        System.out.println("收到批改列表请求，taskId=" + taskId);
        return correctService.getSubmissionsByTask(taskId);
    }

    // 查询单个提交详情
    @GetMapping("/detail/{submissionId}")
    public ResponseEntity<CorrectDetailResponse> getDetail(@PathVariable Long submissionId) {
        System.out.println("收到批改详情请求，submissionId=" + submissionId);
        return correctService.getSubmissionDetail(submissionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 批改提交
    @PostMapping("/grade/{submissionId}")
    public ResponseEntity<?> correctSubmission(
            @PathVariable Long submissionId,
            @RequestBody CorrectGradeRequest request) {
        boolean success = correctService.correctSubmission(submissionId, request.getGrade(), request.getFeedback());
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

//    // 下载提交的文件
//    @GetMapping("/download/{submissionId}")
//    public ResponseEntity<Resource> downloadSubmissionFile(@PathVariable Long submissionId) {
//        try {
//            Optional<Submission> submissionOpt = correctRepository.findById(submissionId);
//            if (submissionOpt.isEmpty()) {
//                return ResponseEntity.notFound().build();
//            }
//
//            Submission submission = submissionOpt.get();
//            String filePath = submission.getFilePath();
//
//            if (filePath == null || filePath.isEmpty()) {
//                return ResponseEntity.badRequest().body(null);
//            }
//
//            Path fullPath;
//            String fileName;
//
//            // 检查是否是新的JSON格式
//            if (filePath.trim().startsWith("{")) {
//                try {
//                    // 解析JSON格式的文件路径
//                    ObjectMapper objectMapper = new ObjectMapper();
//                    JsonNode jsonNode = objectMapper.readTree(filePath);
//
//                    // 获取saved路径和original文件名
//                    String savedPath = jsonNode.has("saved") ? jsonNode.get("saved").asText() : null;
//                    String originalFileName = jsonNode.has("original") ? jsonNode.get("original").asText() : null;
//
//                    if (savedPath == null || savedPath.isEmpty()) {
//                        return ResponseEntity.badRequest().body(null);
//                    }
//
//                    // 处理saved路径
//                    if (savedPath.startsWith("http://") || savedPath.startsWith("https://")) {
//                        // 如果是URL，提取文件名
//                        fileName = originalFileName != null ? originalFileName : savedPath.substring(savedPath.lastIndexOf("/") + 1);
//                        // 从URL中提取相对路径（假设URL格式为 http://localhost:8080/uploads/...）
//                        String relativePath = savedPath.replace("http://localhost:8080/uploads/", "");
//                        fullPath = Paths.get(uploadDir, relativePath);
//                    } else if (savedPath.startsWith("/uploads/")) {
//                        // 相对路径
//                        String relativePath = savedPath.substring("/uploads/".length());
//                        fullPath = Paths.get(uploadDir, relativePath);
//                        fileName = originalFileName != null ? originalFileName : fullPath.getFileName().toString();
//                    } else {
//                        // 绝对路径
//                        fullPath = Paths.get(savedPath);
//                        fileName = originalFileName != null ? originalFileName : fullPath.getFileName().toString();
//                    }
//                } catch (Exception e) {
//                    System.err.println("解析JSON格式文件路径失败: " + e.getMessage());
//                    return ResponseEntity.badRequest().body(null);
//                }
//            } else {
//                // 原有的处理逻辑
//                if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
//                    // 如果是URL，提取文件名
//                    fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
//                    // 从URL中提取相对路径（假设URL格式为 http://localhost:8080/uploads/...）
//                    String relativePath = filePath.replace("http://localhost:8080/uploads/", "");
//                    fullPath = Paths.get(uploadDir, relativePath);
//                } else if (filePath.startsWith("/uploads/")) {
//                    // 相对路径
//                    String relativePath = filePath.substring("/uploads/".length());
//                    fullPath = Paths.get(uploadDir, relativePath);
//                    fileName = fullPath.getFileName().toString();
//                } else {
//                    // 绝对路径
//                    fullPath = Paths.get(filePath);
//                    fileName = fullPath.getFileName().toString();
//                }
//            }
//
//            File file = fullPath.toFile();
//            if (!file.exists()) {
//                return ResponseEntity.notFound().build();
//            }
//
//            Resource resource = new FileSystemResource(file);
//
//            // 设置响应头
//            HttpHeaders headers = new HttpHeaders();
//            headers.add(HttpHeaders.CONTENT_DISPOSITION,
//                "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));
//
//            // 根据文件扩展名设置Content-Type
//            String contentType = getContentType(fileName);
//            if (contentType != null) {
//                headers.setContentType(MediaType.parseMediaType(contentType));
//            }
//
//            return ResponseEntity.ok()
//                    .headers(headers)
//                    .body(resource);
//
//        } catch (Exception e) {
//            System.err.println("下载文件失败: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    // 获取文件预览URL
//    @GetMapping("/preview/{submissionId}")
//    public ResponseEntity<String> previewSubmissionFile(@PathVariable Long submissionId) {
//        try {
//            Optional<Submission> submissionOpt = correctRepository.findById(submissionId);
//            if (submissionOpt.isEmpty()) {
//                return ResponseEntity.notFound().build();
//            }
//
//            Submission submission = submissionOpt.get();
//            String filePath = submission.getFilePath();
//
//            if (filePath == null || filePath.isEmpty()) {
//                return ResponseEntity.badRequest().body("无文件可预览");
//            }
//
//            // 检查是否是新的JSON格式
//            if (filePath.trim().startsWith("{")) {
//                try {
//                    // 解析JSON格式的文件路径
//                    ObjectMapper objectMapper = new ObjectMapper();
//                    JsonNode jsonNode = objectMapper.readTree(filePath);
//
//                    // 使用saved字段作为文件路径
//                    String savedPath = jsonNode.has("saved") ? jsonNode.get("saved").asText() : null;
//                    if (savedPath == null || savedPath.isEmpty()) {
//                        return ResponseEntity.badRequest().body("无文件可预览");
//                    }
//
//                    // 如果已经是完整URL，直接返回
//                    if (savedPath.startsWith("http://") || savedPath.startsWith("https://")) {
//                        return ResponseEntity.ok(savedPath);
//                    }
//
//                    // 构建预览URL
//                    String previewUrl = savedPath.startsWith("/uploads/") ? savedPath : "/uploads/" + savedPath;
//                    return ResponseEntity.ok(previewUrl);
//                } catch (Exception e) {
//                    System.err.println("解析JSON格式文件路径失败: " + e.getMessage());
//                    return ResponseEntity.badRequest().body("文件路径格式错误");
//                }
//            } else {
//                // 原有的处理逻辑
//                // 如果已经是完整URL，直接返回
//                if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
//                    return ResponseEntity.ok(filePath);
//                }
//
//                // 构建预览URL
//                String previewUrl = filePath.startsWith("/uploads/") ? filePath : "/uploads/" + filePath;
//                return ResponseEntity.ok(previewUrl);
//            }
//
//        } catch (Exception e) {
//            System.err.println("获取预览URL失败: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    // 辅助方法：根据文件扩展名获取Content-Type
//    private String getContentType(String fileName) {
//        if (fileName == null || !fileName.contains(".")) {
//            return "application/octet-stream";
//        }
//
//        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
//        switch (extension) {
//            case "pdf": return "application/pdf";
//            case "doc": return "application/msword";
//            case "docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
//            case "ppt": return "application/vnd.ms-powerpoint";
//            case "pptx": return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
//            case "xls": return "application/vnd.ms-excel";
//            case "xlsx": return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
//            case "txt": return "text/plain";
//            case "jpg": case "jpeg": return "image/jpeg";
//            case "png": return "image/png";
//            case "gif": return "image/gif";
//            case "mp4": return "video/mp4";
//            case "avi": return "video/x-msvideo";
//            case "mov": return "video/quicktime";
//            case "zip": return "application/zip";
//            default: return "application/octet-stream";
//        }
//    }

    @GetMapping("/quiz/ungraded-short-answers/{taskId}")
    public List<UngradedQuizAnswerResponse> getUngradedQuizShortAnswers(@PathVariable Long taskId) {
        return correctService.getUngradedQuizShortAnswers(taskId);
    }

    @PostMapping("/quiz/grade-short-answer")
public ResponseEntity<?> gradeQuizShortAnswer(@RequestBody GradeQuizShortAnswerRequest req) {
    boolean success = correctService.gradeQuizShortAnswer(
        req.getSubmissionId(), req.getQuestionId(), req.getScore(), req.getFeedback());
    if (success) {
        return ResponseEntity.ok().build();
    } else {
        return ResponseEntity.notFound().build();
    }
}
    /**
     * 获取学生提交的文件列表（用于教师端展示）
     */
    @GetMapping("/files/{submissionId}")
    public ResponseEntity<SubmissionFileResponse> getSubmissionFiles(@PathVariable Long submissionId) {
        try {
            Optional<Submission> submissionOpt = correctRepository.findById(submissionId);
            if (submissionOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Submission submission = submissionOpt.get();
            String filePath = submission.getFilePath();

            if (filePath == null || filePath.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            SubmissionFileResponse response = new SubmissionFileResponse();
            response.setSubmissionId(submissionId);
            response.setStudentId(submission.getStudent().getStudentId());
            response.setStudentName(submission.getStudent().getStudentName());
            response.setSubmittedAt(submission.getSubmittedAt().toString());

            // 解析文件路径JSON
            List<SubmissionFileResponse.FileInfo> files = parseSubmissionFiles(filePath);
            response.setFiles(files);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("获取提交文件列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 下载学生提交的指定文件
     */
    @GetMapping("/download/{submissionId}/file/{fileIndex}")
    public ResponseEntity<Resource> downloadSubmissionFileByIndex(
            @PathVariable Long submissionId,
            @PathVariable Integer fileIndex) {
        try {
            Optional<Submission> submissionOpt = correctRepository.findById(submissionId);
            if (submissionOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Submission submission = submissionOpt.get();
            String filePath = submission.getFilePath();

            if (filePath == null || filePath.isEmpty()) {
                return ResponseEntity.badRequest().body(null);
            }

            // 解析文件路径JSON，获取指定索引的文件
            List<SubmissionFileResponse.FileInfo> files = parseSubmissionFiles(filePath);
            if (fileIndex < 0 || fileIndex >= files.size()) {
                return ResponseEntity.badRequest().body(null);
            }

            SubmissionFileResponse.FileInfo fileInfo = files.get(fileIndex);

            // 构建完整文件路径
            Path fullPath;
            String savedPath = fileInfo.getSavedPath();

            if (savedPath.startsWith("http://") || savedPath.startsWith("https://")) {
                // 如果是URL，提取相对路径
                String relativePath = savedPath.replace("http://localhost:8080/uploads/", "");
                fullPath = Paths.get(uploadDir, relativePath);
            } else if (savedPath.startsWith("/uploads/")) {
                // 相对路径
                String relativePath = savedPath.substring("/uploads/".length());
                fullPath = Paths.get(uploadDir, relativePath);
            } else {
                // 绝对路径
                fullPath = Paths.get(savedPath);
            }

            File file = fullPath.toFile();
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(file);

            // 设置响应头，使用原始文件名
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=" + URLEncoder.encode(fileInfo.getOriginalName(), "UTF-8"));

            // 根据文件扩展名设置Content-Type
            String contentType = getFileType(fileInfo.getOriginalName());
            if (contentType != null) {
                headers.setContentType(MediaType.parseMediaType(contentType));
            }

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (Exception e) {
            System.err.println("下载文件失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 解析提交文件的JSON格式
     */
    private List<SubmissionFileResponse.FileInfo> parseSubmissionFiles(String filePath) throws Exception {
        List<SubmissionFileResponse.FileInfo> files = new ArrayList<>();

        // 检查是否是JSON数组格式
        if (filePath.trim().startsWith("[")) {
            // 解析JSON数组格式
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonArray = objectMapper.readTree(filePath);

            for (JsonNode fileNode : jsonArray) {
                Integer index = fileNode.has("index") ? fileNode.get("index").asInt() : 0;
                String original = fileNode.has("original") ? fileNode.get("original").asText() : "";
                String saved = fileNode.has("saved") ? fileNode.get("saved").asText() : "";

                // 获取文件大小
                Long fileSize = 0L;
                if (saved.startsWith("/uploads/")) {
                    String relativePath = saved.substring("/uploads/".length());
                    Path fullPath = Paths.get(uploadDir, relativePath);
                    File file = fullPath.toFile();
                    if (file.exists()) {
                        fileSize = file.length();
                    }
                }

                files.add(new SubmissionFileResponse.FileInfo(index, original, saved, getFileType(original), fileSize));
            }
        } else if (filePath.trim().startsWith("{")) {
            // 解析单个JSON对象格式（兼容旧格式）
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(filePath);

            String original = jsonNode.has("original") ? jsonNode.get("original").asText() : "";
            String saved = jsonNode.has("saved") ? jsonNode.get("saved").asText() : "";

            // 获取文件大小
            Long fileSize = 0L;
            if (saved.startsWith("/uploads/")) {
                String relativePath = saved.substring("/uploads/".length());
                Path fullPath = Paths.get(uploadDir, relativePath);
                File file = fullPath.toFile();
                if (file.exists()) {
                    fileSize = file.length();
                }
            }

            files.add(new SubmissionFileResponse.FileInfo(0, original, saved, getFileType(original), fileSize));
        }

        // 按索引排序
        files.sort((a, b) -> Integer.compare(a.getIndex(), b.getIndex()));

        return files;
    }

    /**
     * 预览学生提交的指定文件
     */
    @GetMapping("/preview/{submissionId}/file/{fileIndex}")
    public ResponseEntity<String> previewSubmissionFileByIndex(
            @PathVariable Long submissionId,
            @PathVariable Integer fileIndex) {
        try {
            Optional<Submission> submissionOpt = correctRepository.findById(submissionId);
            if (submissionOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Submission submission = submissionOpt.get();
            String filePath = submission.getFilePath();

            if (filePath == null || filePath.isEmpty()) {
                return ResponseEntity.badRequest().body("无文件可预览");
            }

            // 解析文件路径JSON，获取指定索引的文件
            List<SubmissionFileResponse.FileInfo> files = parseSubmissionFiles(filePath);
            if (fileIndex < 0 || fileIndex >= files.size()) {
                return ResponseEntity.badRequest().body("文件索引无效");
            }

            SubmissionFileResponse.FileInfo fileInfo = files.get(fileIndex);
            String savedPath = fileInfo.getSavedPath();
            String originalName = fileInfo.getOriginalName();

            // 构建预览URL
            String previewUrl;
            if (savedPath.startsWith("http://") || savedPath.startsWith("https://")) {
                // 如果已经是完整URL，直接返回
                previewUrl = savedPath;
            } else if (savedPath.startsWith("/uploads/")) {
                // 相对路径，直接返回
                previewUrl = savedPath;
            } else {
                // 其他情况，添加/uploads/前缀
                previewUrl = "/uploads/" + savedPath;
            }

            return ResponseEntity.ok(previewUrl);

        } catch (Exception e) {
            System.err.println("获取文件预览URL失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取文件预览信息（包含文件类型判断）
     */
    @GetMapping("/preview-info/{submissionId}/file/{fileIndex}")
    public ResponseEntity<Map<String, Object>> getFilePreviewInfo(
            @PathVariable Long submissionId,
            @PathVariable Integer fileIndex) {
        try {
            Optional<Submission> submissionOpt = correctRepository.findById(submissionId);
            if (submissionOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Submission submission = submissionOpt.get();
            String filePath = submission.getFilePath();

            if (filePath == null || filePath.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // 解析文件路径JSON，获取指定索引的文件
            List<SubmissionFileResponse.FileInfo> files = parseSubmissionFiles(filePath);
            if (fileIndex < 0 || fileIndex >= files.size()) {
                return ResponseEntity.badRequest().build();
            }

            SubmissionFileResponse.FileInfo fileInfo = files.get(fileIndex);
            String savedPath = fileInfo.getSavedPath();
            String originalName = fileInfo.getOriginalName();
            String fileType = fileInfo.getFileType();

            // 构建预览URL
            String previewUrl;
            if (savedPath.startsWith("http://") || savedPath.startsWith("https://")) {
                previewUrl = savedPath;
            } else if (savedPath.startsWith("/uploads/")) {
                previewUrl = savedPath;
            } else {
                previewUrl = "/uploads/" + savedPath;
            }

            // 判断预览方式
            String previewType = determinePreviewType(originalName);

            Map<String, Object> response = new HashMap<>();
            response.put("previewUrl", previewUrl);
            response.put("originalName", originalName);
            response.put("fileType", fileType);
            response.put("previewType", previewType);
            response.put("fileSize", fileInfo.getFileSize());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("获取文件预览信息失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 判断文件的预览方式
     */
    private String determinePreviewType(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "download"; // 无法预览，只能下载
        }

        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "jpg": case "jpeg": case "png": case "gif":
                return "image"; // 图片可以直接预览
            case "mp4": case "avi": case "mov":
                return "video"; // 视频可以直接播放
            case "doc": case "docx": case "ppt": case "pptx": case "xls": case "xlsx": case "pdf":
                return "download"; // Office文档和PDF都只能下载
            case "txt":
                return "text"; // 文本文件可以直接预览
            default:
                return "download"; // 其他文件只能下载
        }
    }

    /**
     * 根据文件名获取文件类型
     */
    private String getFileType(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "unknown";
        }
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "pdf": return "application/pdf";
            case "doc": return "application/msword";
            case "docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "ppt": return "application/vnd.ms-powerpoint";
            case "pptx": return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "xls": return "application/vnd.ms-excel";
            case "xlsx": return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "txt": return "text/plain";
            case "jpg": case "jpeg": return "image/jpeg";
            case "png": return "image/png";
            case "gif": return "image/gif";
            case "mp4": return "video/mp4";
            case "avi": return "video/x-msvideo";
            case "mov": return "video/quicktime";
            case "zip": return "application/zip";
            default: return "application/octet-stream";
        }
    }
}