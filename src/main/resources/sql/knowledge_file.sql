-- 知识文件表
DROP TABLE IF EXISTS `knowledge_file`;
CREATE TABLE `knowledge_file` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `file_name` varchar(255) NOT NULL COMMENT '文件名',
  `file_path` varchar(500) NOT NULL COMMENT '文件路径',
  `file_type` varchar(100) DEFAULT NULL COMMENT '文件类型(MIME类型)',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小(字节)',
  `file_hash` varchar(64) DEFAULT NULL COMMENT '文件哈希值(用于去重)',
  `file_suffix` varchar(20) DEFAULT NULL COMMENT '文件后缀',
  `description` text COMMENT '文件描述',
  `status` int DEFAULT '1' COMMENT '上传状态(0-上传中, 1-上传成功, 2-上传失败)',
  `vectorization_status` int DEFAULT '0' COMMENT '向量化状态(0-未向量化, 1-向量化中, 2-向量化成功, 3-向量化失败)',
  `vectorization_time` timestamp NULL COMMENT '向量化时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_file_hash` (`file_hash`) COMMENT '文件哈希唯一索引',
  KEY `idx_file_name` (`file_name`) COMMENT '文件名索引',
  KEY `idx_file_type` (`file_type`) COMMENT '文件类型索引',
  KEY `idx_created_at` (`created_at`) COMMENT '创建时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='知识文件表';
