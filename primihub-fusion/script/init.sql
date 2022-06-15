-- ----------------------------
-- Table structure for fusion_go
-- ----------------------------
DROP TABLE IF EXISTS `fusion_go`;
CREATE TABLE `fusion_go`  (
                              `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
                              `group_id` bigint(20) NOT NULL COMMENT '群组id',
                              `organ_global_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '机构唯一id',
                              `is_del` tinyint(4) NOT NULL COMMENT '是否删除',
                              `c_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                              `u_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
                              PRIMARY KEY (`id`) USING BTREE,
                              UNIQUE INDEX `group_id_and global_id_ix`(`group_id`, `organ_global_id`) USING BTREE,
                              INDEX `group_id_ix`(`group_id`) USING BTREE,
                              INDEX `global_id_ix`(`organ_global_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for fusion_group
-- ----------------------------
DROP TABLE IF EXISTS `fusion_group`;
CREATE TABLE `fusion_group`  (
                                 `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
                                 `group_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '群组名称',
                                 `group_organ_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '群主id',
                                 `is_del` tinyint(4) NOT NULL COMMENT '是否删除',
                                 `c_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                 `u_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
                                 PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for fusion_organ
-- ----------------------------
DROP TABLE IF EXISTS `fusion_organ`;
CREATE TABLE `fusion_organ`  (
                                 `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增id',
                                 `global_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '唯一id',
                                 `global_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '机构名称',
                                 `pin_code_md` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'pin码md5',
                                 `register_time` datetime(3) NOT NULL COMMENT '注册时间',
                                 `is_del` tinyint(4) NOT NULL COMMENT '是否删除',
                                 `c_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                 `u_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
                                 PRIMARY KEY (`id`) USING BTREE,
                                 INDEX `global_id_ix`(`global_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;


-- ----------------------------
-- Table structure for fusion_public_ro
-- ----------------------------
DROP TABLE IF EXISTS `fusion_public_ro`;
CREATE TABLE `fusion_public_ro` (
                                    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
                                    `resource_id` bigint(20) DEFAULT NULL COMMENT '资源ID',
                                    `organ_id` varchar(255) DEFAULT NULL COMMENT '机构ID',
                                    `is_del` tinyint(4) NOT NULL COMMENT '是否删除',
                                    `c_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                    `u_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
                                    PRIMARY KEY (`id`) USING BTREE,
                                    KEY `resource_id_ix` (`resource_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for fusion_resource
-- ----------------------------
DROP TABLE IF EXISTS `fusion_resource`;
CREATE TABLE `fusion_resource` (
                                   `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
                                   `resource_id` varchar(255) DEFAULT NULL COMMENT '资源ID',
                                   `resource_name` varchar(255) DEFAULT NULL COMMENT '资源名称',
                                   `resource_desc` varchar(255) DEFAULT NULL COMMENT '资源描述',
                                   `resource_type` tinyint(4) DEFAULT NULL COMMENT '资源类型 上传...',
                                   `resource_auth_type` tinyint(4) DEFAULT NULL COMMENT '授权类型（公开，私有，可见性）',
                                   `resource_rows_count` int(11) DEFAULT NULL COMMENT '资源行数',
                                   `resource_column_count` int(11) DEFAULT NULL COMMENT '资源列数',
                                   `resource_column_name_list` blob COMMENT '字段列表',
                                   `resource_contains_y` tinyint(4) DEFAULT NULL COMMENT '资源字段中是否包含y字段 0否 1是',
                                   `resource_y_rows_count` int(11) DEFAULT NULL COMMENT '文件字段y值内容不为空和0的行数',
                                   `resource_y_ratio` decimal(10,2) DEFAULT NULL COMMENT '文件字段y值内容不为空的行数在总行的占比',
                                   `resource_tag` varchar(255) DEFAULT NULL COMMENT '资源标签 格式tag,tag',
                                   `organ_id` varchar(255) DEFAULT NULL COMMENT '机构ID',
                                   `is_del` tinyint(4) NOT NULL COMMENT '是否删除',
                                   `c_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                   `u_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
                                   PRIMARY KEY (`id`) USING BTREE,
                                   UNIQUE KEY `resource_id_ix` (`resource_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=551 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for fusion_resource_tag
-- ----------------------------
DROP TABLE IF EXISTS `fusion_resource_tag`;
CREATE TABLE `fusion_resource_tag` (
                                       `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
                                       `name` varchar(255) DEFAULT NULL COMMENT '标签名称',
                                       `is_del` tinyint(4) NOT NULL COMMENT '是否删除',
                                       `c_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                       `u_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
                                       PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=359 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
