package com.example.smartlearn.util;

import com.example.smartlearn.dto.response.KnowldegePointGet;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class KnowledgePointExtractor {
    private static final String API_URL = "http://127.0.0.1:5000/extract";
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 从多个文件提取知识点
     */
    public static KnowldegePointGet extractFromFiles(List<String> filePaths) {
        try {
            System.out.println("开始调取python封装的API");
            // 构建请求体
            String requestBody = buildRequestBody(filePaths);

            // 创建HTTP请求
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // 发送请求并获取响应
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            // 检查响应状态
            if (response.statusCode() != 200) {
                return new KnowldegePointGet(null); // 返回空对象
            }

            // 解析响应
            return OBJECT_MAPPER.readValue(response.body(), KnowldegePointGet.class);

        } catch (Exception e) {
            // 记录错误日志
            System.err.println("知识提取失败: " + e.getMessage());
            return new KnowldegePointGet(null); // 返回空对象
        }
    }

    /**
     * 构建JSON请求体
     */
    private static String buildRequestBody(List<String> filePaths) {
        StringBuilder sb = new StringBuilder("{\"file_path\":[");
        for (int i = 0; i < filePaths.size(); i++) {
            String escapedPath = filePaths.get(i)
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"");
            sb.append("\"").append(escapedPath).append("\"");
            if (i < filePaths.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]}");
        return sb.toString();
    }


    /**
     * 自定义异常
     */
    public static class ExtractionException extends Exception {
        public ExtractionException(String message) {
            super(message);
        }

        public ExtractionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
