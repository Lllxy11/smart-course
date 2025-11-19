package com.example.smartlearn.dto.response;

import java.util.List;

/**
 * 学生提交文件信息的响应DTO
 * 用于教师端展示学生提交的文件列表
 */
public class SubmissionFileResponse {
    
    /**
     * 提交记录ID
     */
    private Long submissionId;
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 学生姓名
     */
    private String studentName;
    
    /**
     * 提交时间
     */
    private String submittedAt;
    
    /**
     * 文件列表
     */
    private List<FileInfo> files;
    
    /**
     * 文件信息内部类
     */
    public static class FileInfo {
        /**
         * 文件索引（提交顺序）
         */
        private Integer index;
        
        /**
         * 原始文件名
         */
        private String originalName;
        
        /**
         * 文件保存路径
         */
        private String savedPath;
        
        /**
         * 文件类型
         */
        private String fileType;
        
        /**
         * 文件大小（字节）
         */
        private Long fileSize;
        
        public FileInfo() {}
        
        public FileInfo(Integer index, String originalName, String savedPath, String fileType, Long fileSize) {
            this.index = index;
            this.originalName = originalName;
            this.savedPath = savedPath;
            this.fileType = fileType;
            this.fileSize = fileSize;
        }
        
        // Getters and Setters
        public Integer getIndex() { return index; }
        public void setIndex(Integer index) { this.index = index; }
        
        public String getOriginalName() { return originalName; }
        public void setOriginalName(String originalName) { this.originalName = originalName; }
        
        public String getSavedPath() { return savedPath; }
        public void setSavedPath(String savedPath) { this.savedPath = savedPath; }
        
        public String getFileType() { return fileType; }
        public void setFileType(String fileType) { this.fileType = fileType; }
        
        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    }
    
    // Getters and Setters
    public Long getSubmissionId() { return submissionId; }
    public void setSubmissionId(Long submissionId) { this.submissionId = submissionId; }
    
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    
    public String getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(String submittedAt) { this.submittedAt = submittedAt; }
    
    public List<FileInfo> getFiles() { return files; }
    public void setFiles(List<FileInfo> files) { this.files = files; }
} 