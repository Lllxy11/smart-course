package com.example.smartlearn.service.teacher;

import com.example.smartlearn.dto.response.CourseGradeReportResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 成绩报表导出服务
 * 基于现有表结构，无需创建新表
 */
@Service
public class GradeReportExportService {

    @Autowired
    private GradeAnalysisService gradeAnalysisService;

    /**
     * 导出课程成绩报表Excel
     */
    public byte[] exportCourseGradeReport(Long courseId) throws IOException {
        try {
            CourseGradeReportResponse report = gradeAnalysisService.getCourseGradeReport(courseId);

            try (Workbook workbook = new XSSFWorkbook()) {
                // 创建简单的工作表
                Sheet sheet = workbook.createSheet("成绩报表");

                // 创建标题行
                Row titleRow = sheet.createRow(0);
                titleRow.createCell(0).setCellValue("课程成绩报表");
                titleRow.createCell(1).setCellValue(report.getCourseName());

                // 创建表头
                Row headerRow = sheet.createRow(1);
                headerRow.createCell(0).setCellValue("学号");
                headerRow.createCell(1).setCellValue("姓名");
                headerRow.createCell(2).setCellValue("总成绩");
                headerRow.createCell(3).setCellValue("等级");
                headerRow.createCell(4).setCellValue("排名");
                headerRow.createCell(5).setCellValue("完成任务数");
                headerRow.createCell(6).setCellValue("总任务数");
                headerRow.createCell(7).setCellValue("完成率");

                // 添加学生数据
                int rowNum = 2;
                for (CourseGradeReportResponse.StudentGradeInfo student : report.getStudentGrades()) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(student.getStudentNumber());
                    row.createCell(1).setCellValue(student.getStudentName());
                    row.createCell(2).setCellValue(student.getTotalGrade().doubleValue());
                    row.createCell(3).setCellValue(student.getGradeLevel());
                    row.createCell(4).setCellValue(student.getRank());
                    row.createCell(5).setCellValue(student.getCompletedTasks());
                    row.createCell(6).setCellValue(student.getTotalTasks());
                    row.createCell(7).setCellValue(student.getCompletionRate());
                }

                // 添加课程统计信息
                rowNum++;
                Row statsRow = sheet.createRow(rowNum++);
                statsRow.createCell(0).setCellValue("课程统计");

                CourseGradeReportResponse.CourseStatistics stats = report.getCourseStatistics();
                Row totalStudentsRow = sheet.createRow(rowNum++);
                totalStudentsRow.createCell(0).setCellValue("总学生数");
                totalStudentsRow.createCell(1).setCellValue(stats.getTotalStudents());

                Row avgRow = sheet.createRow(rowNum++);
                avgRow.createCell(0).setCellValue("班级平均分");
                avgRow.createCell(1).setCellValue(stats.getClassAverage().doubleValue());

                Row highestRow = sheet.createRow(rowNum++);
                highestRow.createCell(0).setCellValue("最高分");
                highestRow.createCell(1).setCellValue(stats.getHighestGrade().doubleValue());

                Row lowestRow = sheet.createRow(rowNum++);
                lowestRow.createCell(0).setCellValue("最低分");
                lowestRow.createCell(1).setCellValue(stats.getLowestGrade().doubleValue());

                // 自动调整列宽
                for (int i = 0; i < 8; i++) {
                    sheet.autoSizeColumn(i);
                }

                // 写入字节数组
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                workbook.write(out);
                return out.toByteArray();
            }
        } catch (Exception e) {
            System.err.println("导出成绩报表失败: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("导出失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建概览工作表
     */
    private void createOverviewSheet(Workbook workbook, CourseGradeReportResponse report,
                                     CellStyle headerStyle, CellStyle titleStyle, CellStyle dataStyle) {
        Sheet sheet = workbook.createSheet("课程概览");

        // 课程基本信息
        int rowNum = 0;
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("课程成绩报表");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 5));

        rowNum++;
        createInfoRow(sheet, rowNum++, "课程名称", report.getCourseName(), dataStyle);
        createInfoRow(sheet, rowNum++, "课程代码", report.getCourseCode(), dataStyle);
        createInfoRow(sheet, rowNum++, "授课教师", report.getTeacherName(), dataStyle);
        createInfoRow(sheet, rowNum++, "生成时间",
                report.getGeneratedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), dataStyle);

        rowNum++;

        // 课程统计信息
        Row statsTitleRow = sheet.createRow(rowNum++);
        Cell statsTitleCell = statsTitleRow.createCell(0);
        statsTitleCell.setCellValue("课程统计信息");
        statsTitleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum-1, rowNum-1, 0, 5));

        CourseGradeReportResponse.CourseStatistics stats = report.getCourseStatistics();
        createInfoRow(sheet, rowNum++, "总学生数", String.valueOf(stats.getTotalStudents()), dataStyle);
        createInfoRow(sheet, rowNum++, "班级平均分", stats.getClassAverage().toString(), dataStyle);
        createInfoRow(sheet, rowNum++, "最高分", stats.getHighestGrade().toString(), dataStyle);
        createInfoRow(sheet, rowNum++, "最低分", stats.getLowestGrade().toString(), dataStyle);
        createInfoRow(sheet, rowNum++, "标准差", stats.getStandardDeviation().toString(), dataStyle);

        rowNum++;

        // 成绩分布
        Row distributionTitleRow = sheet.createRow(rowNum++);
        Cell distributionTitleCell = distributionTitleRow.createCell(0);
        distributionTitleCell.setCellValue("成绩分布");
        distributionTitleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum-1, rowNum-1, 0, 5));

        Row distributionHeaderRow = sheet.createRow(rowNum++);
        distributionHeaderRow.createCell(0).setCellValue("等级");
        distributionHeaderRow.createCell(1).setCellValue("人数");
        distributionHeaderRow.createCell(2).setCellValue("占比");

        for (int i = 0; i < 3; i++) {
            distributionHeaderRow.getCell(i).setCellStyle(headerStyle);
        }

        if (stats.getGradeDistribution() != null) {
            for (Map.Entry<String, Integer> entry : stats.getGradeDistribution().entrySet()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(entry.getKey());
                row.createCell(1).setCellValue(entry.getValue());
                row.createCell(2).setCellValue(String.format("%.1f%%",
                        (double) entry.getValue() / stats.getTotalStudents() * 100));

                for (int i = 0; i < 3; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }
        }

        // 自动调整列宽
        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * 创建学生成绩详情工作表
     */
    private void createStudentGradesSheet(Workbook workbook, CourseGradeReportResponse report,
                                          CellStyle headerStyle, CellStyle titleStyle,
                                          CellStyle dataStyle, CellStyle percentageStyle) {
        Sheet sheet = workbook.createSheet("学生成绩详情");

        // 表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {"排名", "学号", "姓名", "总成绩", "满分", "百分比", "等级", "完成任务数", "总任务数", "完成率"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 数据行
        int rowNum = 1;
        for (CourseGradeReportResponse.StudentGradeInfo studentGrade : report.getStudentGrades()) {
            Row row = sheet.createRow(rowNum++);
            if (row == null) {
                row = sheet.createRow(rowNum++);
            }
            row.createCell(0).setCellValue(studentGrade.getRank());
            row.createCell(1).setCellValue(studentGrade.getStudentNumber());
            row.createCell(2).setCellValue(studentGrade.getStudentName());
            row.createCell(3).setCellValue(studentGrade.getTotalGrade().doubleValue());
            row.createCell(4).setCellValue(studentGrade.getMaxGrade().doubleValue());

            Cell percentageCell = row.createCell(5);
            percentageCell.setCellValue(studentGrade.getGradePercentage());
            percentageCell.setCellStyle(percentageStyle);

            row.createCell(6).setCellValue(studentGrade.getGradeLevel());
            row.createCell(7).setCellValue(studentGrade.getCompletedTasks());
            row.createCell(8).setCellValue(studentGrade.getTotalTasks());

            Cell completionCell = row.createCell(9);
            completionCell.setCellValue(studentGrade.getCompletionRate());
            completionCell.setCellStyle(percentageStyle);

            // 设置数据样式
            for (int i = 0; i < 10; i++) {
                if (i != 5 && i != 9) { // 百分比列使用特殊样式
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }
        }

        // 自动调整列宽
        for (int i = 0; i < 10; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * 创建任务统计工作表
     */
    private void createTaskStatisticsSheet(Workbook workbook, CourseGradeReportResponse report,
                                           CellStyle headerStyle, CellStyle titleStyle, CellStyle dataStyle) {
        Sheet sheet = workbook.createSheet("任务统计");

        // 表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {"任务ID", "任务标题", "任务类型", "截止日期", "总学生数", "提交学生数",
                "完成学生数", "平均分", "满分", "完成率", "平均百分比"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 数据行
        int rowNum = 1;
        for (CourseGradeReportResponse.TaskStatistics taskStat : report.getTaskStatistics()) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(taskStat.getTaskId());
            row.createCell(1).setCellValue(taskStat.getTaskTitle());
            row.createCell(2).setCellValue(taskStat.getTaskType());

            // 处理可能为null的dueDate
            if (taskStat.getDueDate() != null) {
                row.createCell(3).setCellValue(taskStat.getDueDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            } else {
                row.createCell(3).setCellValue("无截止日期");
            }

            row.createCell(4).setCellValue(taskStat.getTotalStudents());
            row.createCell(5).setCellValue(taskStat.getSubmittedStudents());
            row.createCell(6).setCellValue(taskStat.getCompletedStudents());

            if (taskStat.getAverageScore() != null) {
                row.createCell(7).setCellValue(taskStat.getAverageScore().doubleValue());
            }

            row.createCell(8).setCellValue(taskStat.getMaxScore().doubleValue());
            row.createCell(9).setCellValue(taskStat.getCompletionRate());

            if (taskStat.getAveragePercentage() != null) {
                row.createCell(10).setCellValue(taskStat.getAveragePercentage());
            }

            // 设置数据样式
            for (int i = 0; i < 11; i++) {
                row.getCell(i).setCellStyle(dataStyle);
            }
        }

        // 自动调整列宽
        for (int i = 0; i < 11; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * 创建信息行
     */
    private void createInfoRow(Sheet sheet, int rowNum, String label, String value, CellStyle style) {
        Row row = sheet.createRow(rowNum);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(style);

        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(style);
    }

    /**
     * 创建表头样式
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    /**
     * 创建标题样式
     */
    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    /**
     * 创建数据样式
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * 创建百分比样式
     */
    private CellStyle createPercentageStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("0.0%"));
        return style;
    }
} 