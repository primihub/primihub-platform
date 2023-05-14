use fusion;
-- ----------------------------
-- Table structure for fusion_resource
-- ----------------------------
DROP TABLE IF EXISTS `fusion_resource`;
CREATE TABLE `fusion_resource`  (
                                    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
                                    `resource_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '资源ID',
                                    `resource_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '资源名称',
                                    `resource_desc` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '资源描述',
                                    `resource_type` tinyint(4) NULL DEFAULT NULL COMMENT '资源类型 上传...',
                                    `resource_auth_type` tinyint(4) NULL DEFAULT NULL COMMENT '授权类型（公开，私有，可见性）',
                                    `resource_rows_count` int(11) NULL DEFAULT NULL COMMENT '资源行数',
                                    `resource_column_count` int(11) NULL DEFAULT NULL COMMENT '资源列数',
                                    `resource_column_name_list` text NULL COMMENT '字段列表',
                                    `resource_contains_y` tinyint(4) NULL DEFAULT NULL COMMENT '资源字段中是否包含y字段 0否 1是',
                                    `resource_y_rows_count` int(11) NULL DEFAULT NULL COMMENT '文件字段y值内容不为空和0的行数',
                                    `resource_y_ratio` decimal(10, 2) NULL DEFAULT NULL COMMENT '文件字段y值内容不为空的行数在总行的占比',
                                    `resource_tag` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '资源标签 格式tag,tag',
                                    `organ_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '机构ID',
                                    `resource_hash_code` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '资源hash值',
                                    `resource_state` tinyint NOT NULL DEFAULT '0' COMMENT '资源状态 0上线 1下线',
                                    `user_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户名称',
                                    `is_del` tinyint(4) NOT NULL COMMENT '是否删除',
                                    `c_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                    `u_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
                                    PRIMARY KEY (`id`) USING BTREE,
                                    UNIQUE INDEX `resource_id_ix`(`resource_id`) USING BTREE,
                                    INDEX `organ_id_ix`(`organ_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for fusion_resource_tag
-- ----------------------------
DROP TABLE IF EXISTS `fusion_resource_tag`;
CREATE TABLE `fusion_resource_tag`  (
                                        `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
                                        `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '标签名称',
                                        `is_del` tinyint(4) NOT NULL COMMENT '是否删除',
                                        `c_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                        `u_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
                                        PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for fusion_resource_visibility_auth
-- ----------------------------
DROP TABLE IF EXISTS `fusion_resource_visibility_auth`;
CREATE TABLE `fusion_resource_visibility_auth`  (
                                                    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
                                                    `resource_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '资源id',
                                                    `organ_global_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '机构id',
                                                    `is_del` tinyint(4) NOT NULL COMMENT '是否删除',
                                                    `c_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                                    `u_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
                                                    PRIMARY KEY (`id`) USING BTREE,
                                                    INDEX `resource_id_ix`(`resource_id`) USING BTREE,
                                                    INDEX `organ_global_id_ix`(`organ_global_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for fusion_resource_field
-- ----------------------------
DROP TABLE IF EXISTS `fusion_resource_field`;
CREATE TABLE `fusion_resource_field` (
                                         `field_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '字段id',
                                         `resource_id` bigint(20) DEFAULT NULL COMMENT '资源id',
                                         `field_name` varchar(255) DEFAULT NULL COMMENT '字段名称',
                                         `field_as` varchar(255) DEFAULT NULL COMMENT '字段别名',
                                         `field_type` int(11) DEFAULT '0' COMMENT '字段类型 默认0 string',
                                         `field_desc` varchar(255) DEFAULT NULL COMMENT '字段描述',
                                         `is_del` tinyint(4) DEFAULT '0' COMMENT '是否删除',
                                         `c_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                         `u_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
                                         PRIMARY KEY (`field_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for fusion_organ_resource_auth
-- ----------------------------

DROP TABLE IF EXISTS `fusion_organ_resource_auth`;
CREATE TABLE `fusion_organ_resource_auth`  (
                                               `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
                                               `resource_id` bigint(20) NOT NULL COMMENT '资源id',
                                               `organ_id` bigint(20) NOT NULL COMMENT '机构id',
                                               `project_id` varchar(255) DEFAULT NULL COMMENT '项目ID',
                                               `audit_status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '审核状态',
                                               `is_del` tinyint(4) NOT NULL COMMENT '是否删除',
                                               `c_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                               `u_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
                                               PRIMARY KEY (`id`) USING BTREE,
                                               INDEX `resource_id_ix`(`resource_id`) USING BTREE,
                                               INDEX `organ_id_ix`(`organ_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;


DROP TABLE IF EXISTS `data_set`;
CREATE TABLE `data_set` (
                                        `id` varchar(255) NOT NULL COMMENT '主键',
                                        `access_info` varchar(255) COMMENT '访问信息',
                                        `driver` varchar(255) NOT NULL COMMENT '资源类型',
                                        `address` varchar(255) NOT NULL COMMENT '资源地址',
                                        `visibility` varchar(255) NOT NULL COMMENT '可见性',
                                        `available` varchar(255) NOT NULL COMMENT '可获得',
                                        `holder` tinyint(4) DEFAULT '0' COMMENT '是否持有 0持有 1不持有',
                                        `fields` text CHARACTER SET utf8 COLLATE utf8_general_ci COMMENT '字段列表',
                                        `is_del` tinyint(4) DEFAULT '0' COMMENT '是否删除',
                                        `c_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                        `u_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
                                        PRIMARY KEY (`id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `fusion_organ`;
CREATE TABLE `fusion_organ`  (
                                 `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增id',
                                 `global_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '唯一id',
                                 `global_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '机构名称',
                                 `register_time` datetime(3) NOT NULL COMMENT '注册时间',
                                 `is_del` tinyint(4) NOT NULL COMMENT '是否删除',
                                 `c_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                 `u_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
                                 PRIMARY KEY (`id`) USING BTREE,
                                 INDEX `global_id_ix`(`global_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;