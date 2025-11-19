package com.example.smartlearn.service.teacher;

import com.example.smartlearn.dto.response.TaskAiScoreResultResponse;
import com.example.smartlearn.model.Student;
import com.example.smartlearn.model.Submission;
import com.example.smartlearn.model.Task;
import com.example.smartlearn.repository.SubmissionRepository;
import com.example.smartlearn.repository.TaskRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


@Service
public class ReportAiScoreService {

    @Value("${dify.api-key}")
    private String apiKey;

    @Value("${dify.base-url}")
    private String baseUrl; // 例：http://localhost/v1

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    /**
     * 获取任务下所有学生的智能批改结果
     */
    public TaskAiScoreResultResponse getTaskAiScoreResults(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));

        List<Submission> submissions = submissionRepository.findByTaskId(taskId);

        TaskAiScoreResultResponse response = new TaskAiScoreResultResponse();
        response.setTaskId(task.getId());
        response.setTaskTitle(task.getTitle());
        response.setTaskDescription(task.getDescription());

        List<TaskAiScoreResultResponse.StudentAiScoreResult> studentResults = new ArrayList<>();

        for (Submission sub : submissions) {
            Student student = sub.getStudent();
            TaskAiScoreResultResponse.StudentAiScoreResult studentResult = new TaskAiScoreResultResponse.StudentAiScoreResult();
            studentResult.setStudentId(student.getStudentId());
            studentResult.setStudentName(student.getStudentName());
            studentResult.setSubmissionId(sub.getId());
            studentResult.setSubmittedAt(sub.getSubmittedAt());
            studentResult.setTotalGrade(sub.getGrade());
            studentResult.setFeedback(sub.getFeedback());

            // 解析 report_ai_score 字段
            String aiScoreJson = sub.getReportAiScore();
            List<TaskAiScoreResultResponse.FileScoreInfo> fileScores = new ArrayList<>();
            boolean hasAiScore = false;
            if (aiScoreJson != null && !aiScoreJson.trim().isEmpty()) {
                try {
                    List<Map<String, Object>> aiScoreList = objectMapper.readValue(aiScoreJson, new TypeReference<List<Map<String, Object>>>(){});
                    // 解析 file_path 字段
                    List<FileMeta> fileInfoList = parseFilePathJson(sub.getFilePath());
                    for (int i = 0; i < aiScoreList.size(); i++) {
                        Map<String, Object> aiScore = aiScoreList.get(i);
                        TaskAiScoreResultResponse.FileScoreInfo fileScore = new TaskAiScoreResultResponse.FileScoreInfo();
                        fileScore.setIndex((Integer) aiScore.getOrDefault("index", i));
                        fileScore.setPaperGrade(toInteger(aiScore.get("paperGrade")));
                        fileScore.setCompleteness(toDouble(aiScore.get("c1")));
                        fileScore.setInnovation(toDouble(aiScore.get("c2")));

                        // 文件名和后缀
                        if (fileInfoList != null && fileScore.getIndex() < fileInfoList.size()) {
                            FileMeta fileInfo = fileInfoList.get(fileScore.getIndex());
                            fileScore.setFileName(fileInfo.original);
                            fileScore.setFileExtension(getFileExtension(fileInfo.original));
                            fileScore.setFilePath(fileInfo.saved);
                        }
                        fileScores.add(fileScore);
                    }
                    hasAiScore = !fileScores.isEmpty();
                } catch (Exception e) {
                    // 解析失败，忽略
                    System.err.println("[getTaskAiScoreResults] 解析提交 " + sub.getId() + " 的智能批改结果失败: " + e.getMessage());
                }
            }
            studentResult.setFileScores(fileScores);
            studentResult.setHasAiScore(hasAiScore);
            studentResults.add(studentResult);
        }
        response.setStudentResults(studentResults);
        return response;
    }

    /**
     * 主入口：对某个submission的所有文件进行AI批改，criteriaList为每个文件的标准和权重
     */
    public void aiScoreSubmission(Long submissionId, List<Map<String, Object>> criteriaList) throws Exception {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("提交记录不存在"));

        String filePathJson = submission.getFilePath();
        if (filePathJson == null || filePathJson.isEmpty()) {
            throw new IllegalArgumentException("无文件可批改");
        }

        List<FileMeta> files = parseFilePathJson(filePathJson);

        // 并发处理每个文件
        List<Future<ReportAiScore>> futures = new ArrayList<>();
        for (FileMeta file : files) {
            // 找到对应的criteria
            Map<String, Object> criteria = criteriaList.stream()
                    .filter(c -> ((Integer) c.get("index")).intValue() == file.index)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("未找到index=" + file.index + "的标准"));
            futures.add(executor.submit(() -> callDifyApi(file, criteria)));
        }

        // 收集结果，按index排序
        List<ReportAiScore> aiScores = new ArrayList<>();
        for (Future<ReportAiScore> future : futures) {
            aiScores.add(future.get());
        }
        aiScores.sort(Comparator.comparingInt(r -> r.index));

        String aiScoreJson = objectMapper.writeValueAsString(aiScores);
        submission.setReportAiScore(aiScoreJson);
        submissionRepository.save(submission);
    }

    /**
     * 上传单个文件到 Dify，返回 upload_file_id
     */
    private String uploadFileToDify(String filePath, String user) throws Exception {
        System.out.println("[uploadFileToDify] 上传文件: " + filePath + ", user=" + user);
        RestTemplate restTemplate = new RestTemplate();
        String uploadUrl = baseUrl + "/files/upload";

        // filePath 是本地服务器的绝对路径
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("[uploadFileToDify] 文件不存在: " + filePath);
            throw new RuntimeException("文件不存在: " + filePath);
        }

        org.springframework.util.MultiValueMap<String, Object> body = new org.springframework.util.LinkedMultiValueMap<>();
        body.add("file", new org.springframework.core.io.FileSystemResource(file));
        body.add("user", user);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(apiKey);

        HttpEntity<org.springframework.util.MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(uploadUrl, requestEntity, Map.class);
            Map result = response.getBody();
            System.out.println("[uploadFileToDify] 上传返回: " + result);
            if (result == null || !result.containsKey("id")) {
                System.out.println("[uploadFileToDify] 上传失败: " + filePath);
                throw new RuntimeException("文件上传失败: " + filePath);
            }
            return result.get("id").toString();
        } catch (Exception e) {
            System.out.println("[uploadFileToDify] 上传异常: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 针对某个任务，对该任务下所有学生的所有文件，使用同一套标准和权重进行智能批改
     * @param taskId 任务ID
     * @param criteria 标准和权重（Map<String, Object>）
     */
    public void aiScoreTask(Long taskId, Map<String, Object> criteria) throws Exception {
        // 校验6个权重之和等于1
        double sum = 0;
        String[] keys = {"c1", "c2", "c3", "c4", "c5", "c6"};
        for (String key : keys) {
            Object val = criteria.get(key);
            if (val == null) throw new IllegalArgumentException("缺少权重参数: " + key);
            sum += Double.parseDouble(val.toString());
        }
        if (Math.abs(sum - 1.0) > 1e-6) {
            throw new IllegalArgumentException("6个标准的权重之和必须等于1，当前为: " + sum);
        }
        System.out.println("[aiScoreTask] 开始批改，taskId=" + taskId + ", criteria=" + criteria);
        String user = "test-user";
        List<Submission> submissions = submissionRepository.findByTaskId(taskId);
        System.out.println("[aiScoreTask] 查询到 submissions 数量: " + submissions.size());
        for (Submission submission : submissions) {
            String filePathJson = submission.getFilePath();
            if (filePathJson == null || filePathJson.isEmpty()) {
                System.out.println("[aiScoreTask] submissionId=" + submission.getId() + " filePath为空，跳过");
                continue;
            }
            List<FileMeta> files = parseFilePathJson(filePathJson);
            System.out.println("[aiScoreTask] submissionId=" + submission.getId() + " 文件数: " + files.size());

            List<ReportAiScore> aiScores = new ArrayList<>();
            for (FileMeta file : files) {
                try {
                    String absPath = resolveAbsolutePath(file.saved);
                    System.out.println("[aiScoreTask] 上传文件: " + absPath);
                    String uploadFileId = uploadFileToDify(absPath, user);
                    System.out.println("[aiScoreTask] 上传成功，upload_file_id=" + uploadFileId);
                    // 统一传 document，符合 Dify 官方文档
                    String type = "document";
                    Map<String, Object> fileInput = new HashMap<>();
                    fileInput.put("transfer_method", "local_file");
                    fileInput.put("upload_file_id", uploadFileId);
                    fileInput.put("type", type);
                    Map<String, Object> inputs = new HashMap<>();
                    inputs.put("url", fileInput);
                    inputs.putAll(criteria);
                    ReportAiScore score = callDifyApiWithInputs(inputs, user, file.index);
                    aiScores.add(score);
                } catch (Exception e) {
                    System.out.println("[aiScoreTask] 文件处理异常: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            aiScores.sort(Comparator.comparingInt(r -> r.index));

            // 保存批改结果
            if (!aiScores.isEmpty()) {
                try {
                    String aiScoreJson = objectMapper.writeValueAsString(aiScores);
                    submission.setReportAiScore(aiScoreJson);
                    submissionRepository.save(submission);
                    System.out.println("[aiScoreTask] submissionId=" + submission.getId() + " 批改结果已保存，成功批改文件数: " + aiScores.size() + "/" + files.size());
                } catch (Exception e) {
                    System.err.println("[aiScoreTask] 保存批改结果失败: " + e.getMessage());
                    e.printStackTrace();
                    throw e; // 重新抛出异常，让 Controller 处理
                }
            } else {
                System.out.println("[aiScoreTask] submissionId=" + submission.getId() + " 没有成功批改的文件，跳过保存");
            }
        }
        System.out.println("[aiScoreTask] 批改全部完成");
    }

    /**
     * 调用 Dify 工作流 API，inputs 已包含 url（local_file结构）和所有参数
     */
    private ReportAiScore callDifyApiWithInputs(Map<String, Object> inputs, String user, int fileIndex) {
        System.out.println("[callDifyApiWithInputs] 调用workflow, fileIndex=" + fileIndex + ", inputs=" + inputs);
        try {
            System.out.println("调用 Dify workflow 的完整 inputs: " + objectMapper.writeValueAsString(inputs));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = baseUrl + "/workflows/run";

            Map<String, Object> body = new HashMap<>();
            body.put("inputs", inputs);
            body.put("user", user);
            body.put("response_mode", "blocking");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            Map result = response.getBody();
            System.out.println("[callDifyApiWithInputs] workflow返回: " + result);

            if (result == null) {
                System.out.println("[callDifyApiWithInputs] 返回结果为空");
                throw new RuntimeException("AI批改返回结果为空");
            }

            // 获取 data 字段
            Map data = (Map) result.get("data");
            if (data == null) {
                System.out.println("[callDifyApiWithInputs] data字段为空");
                throw new RuntimeException("AI批改返回data字段为空");
            }

            // 获取 outputs 字段
            Map outputs = (Map) data.get("outputs");
            if (outputs == null) {
                System.out.println("[callDifyApiWithInputs] outputs字段为空");
                throw new RuntimeException("AI批改返回outputs字段为空");
            }

            // 获取 structured_output 字段
            Map structured = (Map) outputs.get("structured_output");
            if (structured == null) {
                System.out.println("[callDifyApiWithInputs] structured_output字段为空");
                throw new RuntimeException("AI批改返回structured_output字段为空");
            }

            System.out.println("[callDifyApiWithInputs] structured_output: " + structured);

            ReportAiScore score = new ReportAiScore();
            score.index = fileIndex;

            // 安全地获取各个字段
            Object paperOrderObj = structured.get("paperOrder");
            if (paperOrderObj != null) {
                if (paperOrderObj instanceof Number) {
                    score.paperOrder = ((Number) paperOrderObj).intValue();
                } else {
                    score.paperOrder = Integer.parseInt(paperOrderObj.toString());
                }
            }

            Object paperGradeObj = structured.get("paperGrade");
            if (paperGradeObj != null) {
                if (paperGradeObj instanceof Number) {
                    score.paperGrade = ((Number) paperGradeObj).intValue();
                } else {
                    score.paperGrade = Integer.parseInt(paperGradeObj.toString());
                }
            }

            Object c1Obj = structured.get("c1");
            if (c1Obj != null) {
                if (c1Obj instanceof Number) {
                    score.c1 = ((Number) c1Obj).doubleValue();
                } else {
                    score.c1 = Double.parseDouble(c1Obj.toString());
                }
            }

            Object c2Obj = structured.get("c2");
            if (c2Obj != null) {
                if (c2Obj instanceof Number) {
                    score.c2 = ((Number) c2Obj).doubleValue();
                } else {
                    score.c2 = Double.parseDouble(c2Obj.toString());
                }
            }

            System.out.println("[callDifyApiWithInputs] 解析成功，score: " + score);
            return score;
        } catch (Exception e) {
            System.out.println("[callDifyApiWithInputs] 调用异常: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("AI批改失败", e);
        }
    }


    /**
     * 将 /uploads/xxx.pdf 转为本地绝对路径（需根据你的服务器实际路径实现）
     */
    private String resolveAbsolutePath(String savedPath) {
        if (savedPath.startsWith("/uploads/")) {
            // 拼接本地绝对路径，注意去掉/uploads/前缀
            return uploadDir + savedPath.substring("/uploads".length()).replace("/", "\\");
        }
        return savedPath;
    }

    /**
     * 调用 Dify 工作流 API，返回评分结果
     */
    private ReportAiScore callDifyApi(FileMeta file, Map<String, Object> criteria) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = baseUrl + "/workflows/run"; // 注意路径

            Map<String, Object> inputs = new HashMap<>();
            inputs.put("url", file.saved);
            inputs.putAll(criteria);

            Map<String, Object> body = new HashMap<>();
            body.put("inputs", inputs);
            body.put("user", "test-user"); // 必须加上

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            Map result = response.getBody();
            if (result == null || !result.containsKey("structured_output")) {
                throw new RuntimeException("AI批改返回结果异常: " + file.original);
            }
            Map structured = (Map) result.get("structured_output");
            ReportAiScore score = new ReportAiScore();
            score.index = file.index;
            score.paperOrder = (Integer) structured.get("paperOrder");
            score.paperGrade = (Integer) structured.get("paperGrade");
            score.c1 = Double.parseDouble(structured.get("c1").toString());
            score.c2 = Double.parseDouble(structured.get("c2").toString());
            return score;
        } catch (Exception e) {
            throw new RuntimeException("AI批改失败: " + file.original, e);
        }
    }

    /**
     * 解析filePath字段
     */
    private List<FileMeta> parseFilePathJson(String filePathJson) throws Exception {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<FileMeta> files = new ArrayList<>();
            if (filePathJson.trim().startsWith("[")) {
                JsonNode jsonArray = mapper.readTree(filePathJson);
                for (JsonNode fileNode : jsonArray) {
                    int index = fileNode.has("index") ? fileNode.get("index").asInt() : 0;
                    String original = fileNode.has("original") ? fileNode.get("original").asText() : "";
                    String saved = fileNode.has("saved") ? fileNode.get("saved").asText() : "";
                    files.add(new FileMeta(index, original, saved));
                }
            } else if (filePathJson.trim().startsWith("{")) {
                JsonNode jsonNode = mapper.readTree(filePathJson);
                String original = jsonNode.has("original") ? jsonNode.get("original").asText() : "";
                String saved = jsonNode.has("saved") ? jsonNode.get("saved").asText() : "";
                files.add(new FileMeta(0, original, saved));
            }
            return files;
        } catch (Exception e) {
            throw new RuntimeException("解析文件路径JSON失败: " + e.getMessage(), e);
        }
    }

    // 辅助方法：获取文件扩展名
    private String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int idx = fileName.lastIndexOf('.');
        return idx >= 0 ? fileName.substring(idx + 1) : "";
    }

    // 辅助方法：安全转换为Integer
    private Integer toInteger(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Integer) return (Integer) obj;
        if (obj instanceof Number) return ((Number) obj).intValue();
        return Integer.parseInt(obj.toString());
    }

    // 辅助方法：安全转换为Double
    private Double toDouble(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Double) return (Double) obj;
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        return Double.parseDouble(obj.toString());
    }

    // 文件元信息
    public static class FileMeta {
        public int index;
        public String original;
        public String saved;

        public FileMeta(int index, String original, String saved) {
            this.index = index;
            this.original = original;
            this.saved = saved;
        }
    }

    // AI评分结构
    public static class ReportAiScore {
        public int index;
        public int paperOrder;
        public int paperGrade;
        public double c1;
        public double c2;

        @Override
        public String toString() {
            return "ReportAiScore{index=" + index + ", paperOrder=" + paperOrder +
                    ", paperGrade=" + paperGrade + ", c1=" + c1 + ", c2=" + c2 + "}";
        }
    }
}
