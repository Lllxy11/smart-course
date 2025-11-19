package com.example.smartlearn.dto.response;

import java.math.BigDecimal;

public class AnswerDetail {
    private Long id;
    private Long questionId;
    private String questionBody;
    private String answerContent;
    private String correctAnswer; // 新增：正确答案
    private String type;
    private BigDecimal score;
    private BigDecimal maxScore;
    private Boolean isCorrect;

    public AnswerDetail() {}

    public AnswerDetail(Long id, Long questionId, String questionBody, String answerContent,
                        String correctAnswer, String type, BigDecimal score, BigDecimal maxScore, Boolean isCorrect) {
        this.id = id;
        this.questionId = questionId;
        this.questionBody = questionBody;
        this.answerContent = answerContent;
        this.correctAnswer = correctAnswer;
        this.type = type;
        this.score = score;
        this.maxScore = maxScore;
        this.isCorrect = isCorrect;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public String getQuestionBody() { return questionBody; }
    public void setQuestionBody(String questionBody) { this.questionBody = questionBody; }

    public String getAnswerContent() { return answerContent; }
    public void setAnswerContent(String answerContent) { this.answerContent = answerContent; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }

    public BigDecimal getMaxScore() { return maxScore; }
    public void setMaxScore(BigDecimal maxScore) { this.maxScore = maxScore; }

    public Boolean getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }
} 