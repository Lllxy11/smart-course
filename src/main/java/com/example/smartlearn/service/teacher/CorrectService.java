package com.example.smartlearn.service.teacher;

import com.example.smartlearn.dto.response.AnswerDetail;
import com.example.smartlearn.dto.response.CorrectDetailResponse;
import com.example.smartlearn.dto.response.CorrectResponse;
import com.example.smartlearn.dto.response.UngradedQuizAnswerResponse;
import com.example.smartlearn.model.Student;
import com.example.smartlearn.model.StudentAnswer;
import com.example.smartlearn.model.Submission;
import com.example.smartlearn.model.Task;
import com.example.smartlearn.repository.CorrectRepository;
import com.example.smartlearn.repository.StudentRepository;
import com.example.smartlearn.repository.TaskRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CorrectService {
    private static final Logger logger = LoggerFactory.getLogger(CorrectService.class);

    @Autowired
    private CorrectRepository correctRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private TaskRepository taskRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * 查询某任务下所有提交（非QUIZ类）
     */
    public List<CorrectResponse> getSubmissionsByTask(Long taskId) {
        List<Submission> submissions = correctRepository.findByTaskId(taskId);
        return submissions.stream().map(this::convertToCorrectResponse).collect(Collectors.toList());
    }

    /**
     * 查询某任务下有文件提交的作业
     */
    public List<CorrectResponse> getFileSubmissionsByTask(Long taskId) {
        List<Submission> submissions = correctRepository.findByTaskId(taskId);
        return submissions.stream()
                .filter(sub -> sub.getFilePath() != null && !sub.getFilePath().isEmpty())
                .map(this::convertToCorrectResponse)
                .collect(Collectors.toList());
    }

    /**
     * 查询某任务下有文本内容的作业
     */
    public List<CorrectResponse> getTextSubmissionsByTask(Long taskId) {
        List<Submission> submissions = correctRepository.findByTaskId(taskId);
        return submissions.stream()
                .filter(sub -> sub.getContent() != null && !sub.getContent().trim().isEmpty())
                .map(this::convertToCorrectResponse)
                .collect(Collectors.toList());
    }

    /**
     * 查询某任务下已批改的提交
     */
    public List<CorrectResponse> getGradedSubmissionsByTask(Long taskId) {
        List<Submission> submissions = correctRepository.findByTaskId(taskId);
        return submissions.stream()
                .filter(sub -> sub.getGrade() != null)
                .map(this::convertToCorrectResponse)
                .collect(Collectors.toList());
    }

    /**
     * 查询某任务下未批改的提交
     */
    public List<CorrectResponse> getUngradedSubmissionsByTask(Long taskId) {
        List<Submission> submissions = correctRepository.findByTaskId(taskId);
        return submissions.stream()
                .filter(sub -> sub.getGrade() == null)
                .map(this::convertToCorrectResponse)
                .collect(Collectors.toList());
    }

    /**
     * 查询单个提交详情（非QUIZ类）
     */
    public Optional<CorrectDetailResponse> getSubmissionDetail(Long submissionId) {
        //Optional<Submission> opt = correctRepository.findById(submissionId);
        Optional<Submission> opt = correctRepository.findByIdWithStudentAnswers(submissionId);
        if (opt.isEmpty()) return Optional.empty();
        Submission sub = opt.get();
        Student student = sub.getStudent();
        String description = null;
        if (sub.getTask() != null) {
            description = sub.getTask().getDescription();
        }
        // 日志：打印taskId
        Long taskId = sub.getTask() != null ? sub.getTask().getId() : null;
        logger.info("[getSubmissionDetail] submissionId={}, taskId={}", submissionId, taskId);

        // 构建答题内容
        StringBuilder answerContent = new StringBuilder();
        List<AnswerDetail> answers = new ArrayList<>();
        
        if (sub.getStudentAnswers() != null && !sub.getStudentAnswers().isEmpty()) {
            logger.info("[getSubmissionDetail] 找到 {} 个学生答案", sub.getStudentAnswers().size());
            for (StudentAnswer answer : sub.getStudentAnswers()) {
                if (answer.getQuestion() != null) {
                    // 解析题目内容，提取题干和正确答案
                    String questionStem = parseQuestionStem(answer.getQuestion().getBody());
                    String correctAnswer = extractCorrectAnswer(answer.getQuestion());
                    
                    // 调试日志
                    logger.info("[getSubmissionDetail] 题目ID={}, 题干={}, 正确答案={}", 
                        answer.getQuestion().getId(), questionStem, correctAnswer);
                    
                    // 构建简洁的文本内容
                    answerContent.append("第").append(answers.size() + 1).append("题：").append(questionStem).append("\n");
                    answerContent.append("学生答案：").append(answer.getAnswerContent()).append("\n");
                    
                    // 根据题目类型显示不同信息
                    if (answer.getQuestion().getType() == com.example.smartlearn.model.Question.QuestionType.SINGLE_CHOICE ||
                        answer.getQuestion().getType() == com.example.smartlearn.model.Question.QuestionType.MULTI_CHOICE) {
                        // 选择题显示对错和得分
                        answerContent.append("正确答案：").append(correctAnswer).append("\n");
                        answerContent.append("是否正确：").append(answer.getCorrect() != null ? (answer.getCorrect() ? "正确" : "错误") : "未判断").append("\n");
                        answerContent.append("得分：").append(answer.getScore() != null ? answer.getScore() : "未评分").append("\n");
                    } else if (answer.getQuestion().getType() == com.example.smartlearn.model.Question.QuestionType.FILL_IN_BLANK) {
                        // 填空题显示正确答案供参考
                        answerContent.append("正确答案：").append(correctAnswer).append("\n");
                        answerContent.append("得分：").append(answer.getScore() != null ? answer.getScore() : "未评分").append("\n");
                    } else {
                        // 简答题只显示得分
                        answerContent.append("得分：").append(answer.getScore() != null ? answer.getScore() : "未评分").append("\n");
                    }
                    answerContent.append("---\n");
                    
                    // 构建AnswerDetail对象
                    AnswerDetail answerDetail = new AnswerDetail(
                        answer.getId(),
                        answer.getQuestion().getId(),
                        questionStem, // 使用解析后的题干
                        answer.getAnswerContent(),
                        correctAnswer, // 添加正确答案
                        answer.getQuestion().getType().name(),
                        answer.getScore(),
                        new BigDecimal(10), // 默认满分10分，实际应该从试卷配置获取
                        answer.getCorrect()
                    );
                    
                    // 调试日志：检查AnswerDetail对象
                    logger.info("[getSubmissionDetail] 创建AnswerDetail - ID: {}, correctAnswer: '{}'", 
                        answerDetail.getId(), answerDetail.getCorrectAnswer());
                    
                    answers.add(answerDetail);
                }
            }
        } else {
            logger.info("[getSubmissionDetail] 未找到学生答案记录");
        }

        // 优先使用答题内容，如果没有则使用原有的content
        String finalContent = answerContent.length() > 0 ? answerContent.toString() : sub.getContent();

        CorrectDetailResponse response = new CorrectDetailResponse(
                sub.getId(),
                student != null ? student.getStudentId() : null,
                student != null ? student.getStudentName() : null,
                sub.getSubmittedAt(),
                sub.getGrade(),
                sub.getFeedback(),
                finalContent,
                sub.getFilePath(),
                description
        );
        response.setTaskId(taskId); // 确保赋值
        response.setAnswers(answers); // 设置答题详情列表

        // 日志：打印返回对象
        logger.info("[getSubmissionDetail] 返回 CorrectDetailResponse: submissionId={}, taskId={}", response.getSubmissionId(), response.getTaskId());

        return Optional.of(response);
    }

    /**
     * 批改（打分、写评语）
     */
    @Transactional
    public boolean correctSubmission(Long submissionId, BigDecimal grade, String feedback) {
        Optional<Submission> opt = correctRepository.findById(submissionId);
        if (opt.isEmpty()) return false;
        Submission sub = opt.get();
        sub.setGrade(grade);
        sub.setFeedback(feedback);
        correctRepository.save(sub);
        return true;
    }

    /**
     * 获取文件下载URL
     */
    public String getFileDownloadUrl(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }

        // 检查是否是新的JSON格式
        if (filePath.trim().startsWith("{")) {
            try {
                // 解析JSON格式的文件路径
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(filePath);

                // 使用saved字段作为文件路径
                String savedPath = jsonNode.has("saved") ? jsonNode.get("saved").asText() : null;
                if (savedPath != null && !savedPath.isEmpty()) {
                    return getFileDownloadUrlFromPath(savedPath);
                }

                logger.warn("无法从JSON格式的文件路径中提取saved路径: {}", filePath);
                return null;
            } catch (Exception e) {
                logger.error("解析JSON格式文件路径失败: {}", e.getMessage());
                return null;
            }
        }

        // 原有的处理逻辑
        return getFileDownloadUrlFromPath(filePath);
    }

    /**
     * 从路径获取文件下载URL
     */
    private String getFileDownloadUrlFromPath(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }

        // 如果已经是完整的URL，直接返回
        if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
            return filePath;
        }

        // 如果是相对路径，构建下载URL
        if (filePath.startsWith("/uploads/")) {
            return filePath; // 前端可以直接访问
        }

        return null;
    }

    /**
     * 获取文件名
     */
    public String getFileName(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }

        // 检查是否是新的JSON格式
        if (filePath.trim().startsWith("{")) {
            try {
                // 解析JSON格式的文件路径
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(filePath);

                // 优先使用original字段（原始文件名），如果没有则使用saved字段
                String originalFileName = jsonNode.has("original") ? jsonNode.get("original").asText() : null;
                if (originalFileName != null && !originalFileName.isEmpty()) {
                    return originalFileName;
                }

                // 如果original字段为空，从saved字段中提取文件名
                String savedPath = jsonNode.has("saved") ? jsonNode.get("saved").asText() : null;
                if (savedPath != null && !savedPath.isEmpty()) {
                    return extractFileNameFromPath(savedPath);
                }

                logger.warn("无法从JSON格式的文件路径中提取文件名: {}", filePath);
                return "未知文件";
            } catch (Exception e) {
                logger.error("解析JSON格式文件路径失败: {}", e.getMessage());
                return "解析失败";
            }
        }

        // 原有的处理逻辑
        return extractFileNameFromPath(filePath);
    }

    /**
     * 从路径中提取文件名
     */
    private String extractFileNameFromPath(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }

        if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
            return filePath.substring(filePath.lastIndexOf("/") + 1);
        } else if (filePath.startsWith("/uploads/")) {
            return filePath.substring(filePath.lastIndexOf("/") + 1);
        }

        return filePath.substring(filePath.lastIndexOf("/") + 1);
    }

    /**
     * 解析题目内容，提取题干
     */
    private String parseQuestionStem(String questionBody) {
        if (questionBody == null || questionBody.trim().isEmpty()) {
            return "题目内容为空";
        }
        
        try {
            // 尝试解析JSON格式
            if (questionBody.trim().startsWith("{")) {
                // 简单的JSON解析，提取stem字段
                int stemIndex = questionBody.indexOf("\"stem\":");
                if (stemIndex != -1) {
                    int startQuote = questionBody.indexOf("\"", stemIndex + 7);
                    if (startQuote != -1) {
                        int endQuote = questionBody.indexOf("\"", startQuote + 1);
                        if (endQuote != -1) {
                            return questionBody.substring(startQuote + 1, endQuote);
                        }
                    }
                }
            }
            
            // 如果不是JSON格式或解析失败，直接返回原内容
            return questionBody;
        } catch (Exception e) {
            logger.warn("解析题目内容失败: {}", e.getMessage());
            return questionBody;
        }
    }

    /**
     * 从题目中提取正确答案
     */
    private String extractCorrectAnswer(com.example.smartlearn.model.Question question) {
        try {
            if (question.getBody() == null || question.getBody().trim().isEmpty()) {
                logger.warn("题目body为空，题目ID: {}", question.getId());
                return "无答案";
            }
            
            logger.info("提取正确答案 - 题目ID: {}, 题目类型: {}, 题目body: {}", 
                question.getId(), question.getType(), question.getBody());
            
            // 解析JSON格式的题目body
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(question.getBody());
            
            String result = null;
            
            // 根据题目类型提取答案
            if (question.getType() == com.example.smartlearn.model.Question.QuestionType.SINGLE_CHOICE) {
                result = jsonNode.get("answer").asText();
                logger.info("单选题答案: {}", result);
            } else if (question.getType() == com.example.smartlearn.model.Question.QuestionType.MULTI_CHOICE) {
                JsonNode answerArray = jsonNode.get("answer");
                if (answerArray.isArray()) {
                    StringBuilder sb = new StringBuilder();
                    for (JsonNode answer : answerArray) {
                        if (sb.length() > 0) sb.append(",");
                        sb.append(answer.asText());
                    }
                    result = sb.toString();
                } else {
                    result = answerArray.asText();
                }
                logger.info("多选题答案: {}", result);
            } else {
                // 填空题和简答题
                result = jsonNode.get("answer").asText();
                logger.info("填空题/简答题答案: {}", result);
            }
            
            return result;
        } catch (Exception e) {
            logger.error("提取正确答案失败 - 题目ID: {}, 错误: {}", question.getId(), e.getMessage(), e);
            return "解析失败";
        }
    }

    /**
     * 确定提交类型
     */
    private String determineSubmissionType(Submission submission) {
        boolean hasFile = submission.getFilePath() != null && !submission.getFilePath().isEmpty();
        boolean hasContent = submission.getContent() != null && !submission.getContent().trim().isEmpty();
        
        if (hasFile && hasContent) {
            return "BOTH";
        } else if (hasFile) {
            return "FILE";
        } else if (hasContent) {
            return "TEXT";
        } else {
            return "NONE";
        }
    }

    /**
     * 转换为CorrectResponse
     */
    private CorrectResponse convertToCorrectResponse(Submission submission) {
        Student student = submission.getStudent();
        String submissionType = determineSubmissionType(submission);
        String fileName = getFileName(submission.getFilePath());
        
        return new CorrectResponse(
                submission.getId(),
                student != null ? student.getStudentId() : null,
                student != null ? student.getStudentName() : null,
                submission.getSubmittedAt(),
                submission.getGrade(),
                submission.getFeedback(),
                submissionType,
                fileName
        );
    }

    /**
     * 查询某任务下所有未批改的QUIZ简答题
     */
    public List<UngradedQuizAnswerResponse> getUngradedQuizShortAnswers(Long taskId) {
        logger.info("[getUngradedQuizShortAnswers] 入参 taskId={}", taskId);
        List<Submission> submissions = correctRepository.findByTaskId(taskId);
        logger.info("[getUngradedQuizShortAnswers] 查询到 submissions 数量: {}", submissions.size());
        List<UngradedQuizAnswerResponse> result = new ArrayList<>();
        for (Submission sub : submissions) {
            logger.info("[getUngradedQuizShortAnswers] submissionId={}, taskType={}", sub.getId(), sub.getTask() != null ? sub.getTask().getType() : null);
            if (sub.getTask() == null || sub.getTask().getType() != Task.TaskType.QUIZ) continue;
            if (sub.getStudentAnswers() == null) continue;
            for (StudentAnswer ans : sub.getStudentAnswers()) {
                logger.info("[getUngradedQuizShortAnswers]   answerId={}, questionId={}, questionType={}, score={}",
                        ans.getId(),
                        ans.getQuestion() != null ? ans.getQuestion().getId() : null,
                        ans.getQuestion() != null ? ans.getQuestion().getType() : null,
                        ans.getScore());
                if (ans.getQuestion().getType() == com.example.smartlearn.model.Question.QuestionType.SHORT_ANSWER
                        && (ans.getScore() == null || ans.getScore().compareTo(BigDecimal.ZERO) == 0)) {
                    UngradedQuizAnswerResponse dto = new UngradedQuizAnswerResponse();
                    dto.setSubmissionId(sub.getId());
                    dto.setStudentId(sub.getStudent().getStudentId());
                    dto.setStudentName(sub.getStudent().getName());
                    dto.setQuestionId(ans.getQuestion().getId());
                    dto.setQuestionTitle(ans.getQuestion().getBody());
                    dto.setStudentAnswer(ans.getAnswerContent());
                    dto.setSubmittedAt(sub.getSubmittedAt());
                    result.add(dto);
                    logger.info("[getUngradedQuizShortAnswers]   --> 加入未批改简答题: submissionId={}, questionId={}", sub.getId(), ans.getQuestion().getId());
                }
            }
        }
        logger.info("[getUngradedQuizShortAnswers] 最终返回未批改简答题数量: {}", result.size());
        for (UngradedQuizAnswerResponse dto : result) {
            logger.info("[getUngradedQuizShortAnswers] 返回项: submissionId={}, questionId={}, studentId={}, studentName={}, questionTitle={}, studentAnswer={}, submittedAt={}",
                dto.getSubmissionId(),
                dto.getQuestionId(),
                dto.getStudentId(),
                dto.getStudentName(),
                dto.getQuestionTitle(),
                dto.getStudentAnswer(),
                dto.getSubmittedAt()
            );
        }
        return result;
    }

    /**
     * 批改QUIZ简答题
     */
    @Transactional
    public boolean gradeQuizShortAnswer(Long submissionId, Long questionId, BigDecimal score, String feedback) {
        Optional<Submission> opt = correctRepository.findById(submissionId);
        if (opt.isEmpty()) return false;
        Submission sub = opt.get();
        boolean updated = false;
        for (StudentAnswer ans : sub.getStudentAnswers()) {
            if (ans.getQuestion().getId().equals(questionId)) {
                ans.setScore(score);
                //ans.setFeedback(feedback);
                updated = true;
            }
        }
        if (updated) {
            // 更新总分
            BigDecimal total = sub.getStudentAnswers().stream()
                    .map(StudentAnswer::getScore)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            sub.setGrade(total);
            correctRepository.save(sub);
        }
        return updated;
    }
}
