package com.example.smartlearn.util;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class VideoDurationUtil {

    public static int getVideoDuration(File videoFile) throws Exception {
        // 1. 构建 FFmpeg 命令
        String ffmpegPath = "D:\\ffmpeg-7.0.2-essentials_build\\bin\\ffmpeg.exe";
        String[] command = {
                ffmpegPath,
                "-i",
                videoFile.getAbsolutePath()
        };

        // 2. 执行命令并捕获错误流（FFmpeg 输出到 stderr）
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getErrorStream())
        );

        // 3. 解析输出，提取时长
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains("Duration:")) {
                // 示例输出行: "Duration: 00:01:23.45, start: 0.000000, bitrate: 128 kb/s"
                String durationStr = line.split("Duration:")[1].split(",")[0].trim();
                return parseDurationToSeconds(durationStr);
            }
        }

        throw new Exception("无法解析视频时长");
    }

    // 将 HH:MM:SS.mmm 格式转换为秒
    private static int parseDurationToSeconds(String duration) {
        String[] parts = duration.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        double seconds = Double.parseDouble(parts[2]);
        return hours * 3600 + minutes * 60 + (int) seconds;
    }

    public static void main(String[] args) throws Exception {
        File video = new File("test.mp4");
        int duration = getVideoDuration(video);
        System.out.println("视频时长（秒）: " + duration);
    }
}