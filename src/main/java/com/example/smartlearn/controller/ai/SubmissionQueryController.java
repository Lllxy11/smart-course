package com.example.smartlearn.controller.ai;

import com.example.smartlearn.model.Submission;
import com.example.smartlearn.repository.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/submission-query")
public class SubmissionQueryController {

    @Autowired
    private SubmissionRepository submissionRepository;

    @GetMapping("/task/{taskId}/submissions")
    public ResponseEntity<List<SubmissionScoreResponse>> getTaskSubmissionsWithScore(@PathVariable Long taskId) {
        List<Submission> submissions = submissionRepository.findByTaskId(taskId);
        List<SubmissionScoreResponse> result = submissions.stream().map(sub -> {
            SubmissionScoreResponse dto = new SubmissionScoreResponse();
            dto.setStudentId(sub.getStudent().getStudentId());
            dto.setStudentName(sub.getStudent().getStudentName());
            dto.setSubmissionId(sub.getId());
            dto.setFilePath(sub.getFilePath());
            dto.setReportAiScore(sub.getReportAiScore());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // DTO 内部类
    public static class SubmissionScoreResponse {
        private Long studentId;
        private String studentName;
        private Long submissionId;
        private String filePath; // JSON字符串
        private String reportAiScore; // JSON字符串

        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }
        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }
        public Long getSubmissionId() { return submissionId; }
        public void setSubmissionId(Long submissionId) { this.submissionId = submissionId; }
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        public String getReportAiScore() { return reportAiScore; }
        public void setReportAiScore(String reportAiScore) { this.reportAiScore = reportAiScore; }
    }
}
