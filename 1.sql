/*
 Navicat Premium Dump SQL

 Source Server         : MySQL80
 Source Server Type    : MySQL
 Source Server Version : 80042 (8.0.42)
 Source Host           : localhost:3306
 Source Schema         : 7777

 Target Server Type    : MySQL
 Target Server Version : 80042 (8.0.42)
 File Encoding         : 65001

 Date: 19/11/2025 19:26:11
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for background_jobs
-- ----------------------------
DROP TABLE IF EXISTS `background_jobs`;
CREATE TABLE `background_jobs`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `job_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `payload` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `result` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `status` enum('COMPLETED','FAILED','PENDING','PROCESSING') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `updated_at` datetime(6) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for capabilities
-- ----------------------------
DROP TABLE IF EXISTS `capabilities`;
CREATE TABLE `capabilities`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `course_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKesl8anm6d9lfonkip7rljpyeh`(`course_id` ASC) USING BTREE,
  CONSTRAINT `FKesl8anm6d9lfonkip7rljpyeh` FOREIGN KEY (`course_id`) REFERENCES `courses` (`course_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for class_resource
-- ----------------------------
DROP TABLE IF EXISTS `class_resource`;
CREATE TABLE `class_resource`  (
  `resource_id` bigint NOT NULL AUTO_INCREMENT,
  `course_id` int NULL DEFAULT NULL,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `duration` int NULL DEFAULT NULL,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `task_id` int NULL DEFAULT NULL,
  `type` enum('ppt','pdf','video','doc') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`resource_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 36 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for courses
-- ----------------------------
DROP TABLE IF EXISTS `courses`;
CREATE TABLE `courses`  (
  `course_id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `credit` int NULL DEFAULT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `teacher_id` bigint NULL DEFAULT NULL,
  `term` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`course_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for knowledge_point_resources
-- ----------------------------
DROP TABLE IF EXISTS `knowledge_point_resources`;
CREATE TABLE `knowledge_point_resources`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `linked_at` datetime(6) NULL DEFAULT NULL,
  `knowledge_point_id` bigint NOT NULL,
  `resource_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKbi6cd0patirthle2bdrkn4xke`(`knowledge_point_id` ASC) USING BTREE,
  INDEX `FK4q6xibj84wovc4dievy9j9h2o`(`resource_id` ASC) USING BTREE,
  CONSTRAINT `FK4q6xibj84wovc4dievy9j9h2o` FOREIGN KEY (`resource_id`) REFERENCES `class_resource` (`resource_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKbi6cd0patirthle2bdrkn4xke` FOREIGN KEY (`knowledge_point_id`) REFERENCES `knowledge_points` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 62 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for knowledge_points
-- ----------------------------
DROP TABLE IF EXISTS `knowledge_points`;
CREATE TABLE `knowledge_points`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `parent_id` bigint NULL DEFAULT NULL,
  `position_x` double NULL DEFAULT NULL,
  `position_y` double NULL DEFAULT NULL,
  `course_id` bigint NOT NULL,
  `parent_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK1e5pfbb749fk6iryvhtkphdn7`(`course_id` ASC) USING BTREE,
  CONSTRAINT `FK1e5pfbb749fk6iryvhtkphdn7` FOREIGN KEY (`course_id`) REFERENCES `courses` (`course_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 750 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for questions
-- ----------------------------
DROP TABLE IF EXISTS `questions`;
CREATE TABLE `questions`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `body` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `difficulty` int NULL DEFAULT NULL,
  `type` enum('FILL_IN_BLANK','MULTI_CHOICE','SHORT_ANSWER','SINGLE_CHOICE') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `course_id` bigint NOT NULL,
  `knowledge_point_id` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK6y78pbrt5dxp12ljsa8r1ey82`(`course_id` ASC) USING BTREE,
  INDEX `FK2t662x34k11q1fx4u71nt7pbj`(`knowledge_point_id` ASC) USING BTREE,
  CONSTRAINT `FK2t662x34k11q1fx4u71nt7pbj` FOREIGN KEY (`knowledge_point_id`) REFERENCES `knowledge_points` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `FK6y78pbrt5dxp12ljsa8r1ey82` FOREIGN KEY (`course_id`) REFERENCES `courses` (`course_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for quiz_questions
-- ----------------------------
DROP TABLE IF EXISTS `quiz_questions`;
CREATE TABLE `quiz_questions`  (
  `quiz_id` bigint NOT NULL,
  `question_id` bigint NOT NULL,
  `score` int NULL DEFAULT NULL,
  `order_index` int NULL DEFAULT NULL,
  PRIMARY KEY (`quiz_id`, `question_id`) USING BTREE,
  INDEX `FKev41c723fx659v28pjycox15o`(`question_id` ASC) USING BTREE,
  CONSTRAINT `FKanfmgf6ksbdnv7ojb0pfve54q` FOREIGN KEY (`quiz_id`) REFERENCES `quizzes` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKev41c723fx659v28pjycox15o` FOREIGN KEY (`question_id`) REFERENCES `questions` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for quizzes
-- ----------------------------
DROP TABLE IF EXISTS `quizzes`;
CREATE TABLE `quizzes`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `total_points` int NULL DEFAULT NULL,
  `course_id` bigint NULL DEFAULT NULL,
  `creator_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKpxdnhxeppxx606nhyjtjyharp`(`course_id` ASC) USING BTREE,
  INDEX `FKjmp5ly7ob0lweo6kyvoeuqexg`(`creator_id` ASC) USING BTREE,
  CONSTRAINT `FKjmp5ly7ob0lweo6kyvoeuqexg` FOREIGN KEY (`creator_id`) REFERENCES `teachers` (`teacher_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKpxdnhxeppxx606nhyjtjyharp` FOREIGN KEY (`course_id`) REFERENCES `courses` (`course_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for student_answers
-- ----------------------------
DROP TABLE IF EXISTS `student_answers`;
CREATE TABLE `student_answers`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `answer_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `is_correct` bit(1) NULL DEFAULT NULL,
  `score` decimal(5, 2) NULL DEFAULT NULL,
  `question_id` bigint NOT NULL,
  `submission_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK8nyksamccim8emu803uhf2da`(`question_id` ASC) USING BTREE,
  INDEX `FKf0s9eifp1j4hxy0v2of0obgqf`(`submission_id` ASC) USING BTREE,
  CONSTRAINT `FK8nyksamccim8emu803uhf2da` FOREIGN KEY (`question_id`) REFERENCES `questions` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKf0s9eifp1j4hxy0v2of0obgqf` FOREIGN KEY (`submission_id`) REFERENCES `submissions` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for student_course
-- ----------------------------
DROP TABLE IF EXISTS `student_course`;
CREATE TABLE `student_course`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `enroll_date` datetime(6) NOT NULL,
  `final_grade` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `course_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKpq6ovhxttt2ggd6cwu7sytpth`(`course_id` ASC) USING BTREE,
  INDEX `FKnh5bqghcqt8f5p2yqshbr5g6q`(`student_id` ASC) USING BTREE,
  CONSTRAINT `FKnh5bqghcqt8f5p2yqshbr5g6q` FOREIGN KEY (`student_id`) REFERENCES `students` (`student_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKpq6ovhxttt2ggd6cwu7sytpth` FOREIGN KEY (`course_id`) REFERENCES `courses` (`course_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for students
-- ----------------------------
DROP TABLE IF EXISTS `students`;
CREATE TABLE `students`  (
  `student_id` bigint NOT NULL,
  `class` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `student_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `user_id` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`student_id`) USING BTREE,
  UNIQUE INDEX `UKg4fwvutq09fjdlb4bb0byp7t`(`user_id` ASC) USING BTREE,
  CONSTRAINT `FKdt1cjx5ve5bdabmuuf3ibrwaq` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for submissions
-- ----------------------------
DROP TABLE IF EXISTS `submissions`;
CREATE TABLE `submissions`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `feedback` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `file_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `grade` decimal(5, 2) NULL DEFAULT NULL,
  `submitted_at` datetime(6) NOT NULL,
  `student_id` bigint NOT NULL,
  `task_id` bigint NOT NULL,
  `report_ai_score` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKhwebuw14r6lb2ja85w9mwa8vf`(`student_id` ASC) USING BTREE,
  INDEX `FK4rk9ci86nt91knr0so271buuk`(`task_id` ASC) USING BTREE,
  CONSTRAINT `FK4rk9ci86nt91knr0so271buuk` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKhwebuw14r6lb2ja85w9mwa8vf` FOREIGN KEY (`student_id`) REFERENCES `students` (`student_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for task_resources
-- ----------------------------
DROP TABLE IF EXISTS `task_resources`;
CREATE TABLE `task_resources`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `file_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `file_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `upload_date` datetime(6) NOT NULL,
  `course_id` bigint NOT NULL,
  `task_id` bigint NOT NULL,
  `uploader_id` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_task_resource_course_id`(`course_id` ASC) USING BTREE,
  INDEX `fk_task_resources_task_id`(`task_id` ASC) USING BTREE,
  INDEX `fk_resources_uploader_id`(`uploader_id` ASC) USING BTREE,
  CONSTRAINT `fk_resources_uploader_id` FOREIGN KEY (`uploader_id`) REFERENCES `teachers` (`teacher_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_task_resource_course_id` FOREIGN KEY (`course_id`) REFERENCES `courses` (`course_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_task_resources_task_id` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tasks
-- ----------------------------
DROP TABLE IF EXISTS `tasks`;
CREATE TABLE `tasks`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `due_date` datetime(6) NULL DEFAULT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `type` enum('HOMEWORK','QUIZ','REPORT') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `course_id` bigint NOT NULL,
  `quiz_id` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UKrq4w4qf381eid82bkihr6kfec`(`quiz_id` ASC) USING BTREE,
  INDEX `FKopldg47bgaarlampi2f6wees3`(`course_id` ASC) USING BTREE,
  CONSTRAINT `FK9m4bwrk61gld3uvug608ajhu9` FOREIGN KEY (`quiz_id`) REFERENCES `quizzes` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKopldg47bgaarlampi2f6wees3` FOREIGN KEY (`course_id`) REFERENCES `courses` (`course_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for teachers
-- ----------------------------
DROP TABLE IF EXISTS `teachers`;
CREATE TABLE `teachers`  (
  `teacher_id` bigint NOT NULL AUTO_INCREMENT,
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `department` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `user_id` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`teacher_id`) USING BTREE,
  UNIQUE INDEX `UKcd1k6xwg9jqtiwx9ybnxpmoh9`(`user_id` ASC) USING BTREE,
  CONSTRAINT `FKb8dct7w2j1vl1r2bpstw5isc0` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `user_id` bigint NOT NULL AUTO_INCREMENT,
  `account` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `UKr2bdsy4yqtkpssr475ch00bdr`(`account` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for video_events
-- ----------------------------
DROP TABLE IF EXISTS `video_events`;
CREATE TABLE `video_events`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `duration` int NULL DEFAULT NULL,
  `event_type` enum('ENDED','PAUSE','PLAY','SEEK') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `session_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `video_timestamp` int NULL DEFAULT NULL,
  `resource_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKkg2cwqy6nvblf3c0y2u1k7gwb`(`resource_id` ASC) USING BTREE,
  INDEX `FKrintpw9v4ibu72rl9em9f1upm`(`student_id` ASC) USING BTREE,
  CONSTRAINT `FKkg2cwqy6nvblf3c0y2u1k7gwb` FOREIGN KEY (`resource_id`) REFERENCES `class_resource` (`resource_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKrintpw9v4ibu72rl9em9f1upm` FOREIGN KEY (`student_id`) REFERENCES `students` (`student_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for video_resource_analytics
-- ----------------------------
DROP TABLE IF EXISTS `video_resource_analytics`;
CREATE TABLE `video_resource_analytics`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `analysis_time` datetime(6) NOT NULL,
  `average_watch_time` double NULL DEFAULT NULL,
  `completion_rate` double NULL DEFAULT NULL,
  `hot_spots` json NULL,
  `student_last_watch_time` int NULL DEFAULT NULL,
  `student_progress_rate` double NULL DEFAULT NULL,
  `student_watch_count` int NULL DEFAULT NULL,
  `total_views` int NULL DEFAULT NULL,
  `resource_id` bigint NULL DEFAULT NULL,
  `student_id` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK6s88vutavoqber6t03sssj9o1`(`resource_id` ASC) USING BTREE,
  INDEX `FKgvaagod51rkmdrkfboe2k5a5m`(`student_id` ASC) USING BTREE,
  CONSTRAINT `FK6s88vutavoqber6t03sssj9o1` FOREIGN KEY (`resource_id`) REFERENCES `class_resource` (`resource_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKgvaagod51rkmdrkfboe2k5a5m` FOREIGN KEY (`student_id`) REFERENCES `students` (`student_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
