-- ----------------------------
-- Table structure for data_model
-- ----------------------------
DROP TABLE IF EXISTS `data_model`;
CREATE TABLE `data_model` (
                              `model_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '模型id',
                              `model_name` varchar(255) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '模型名称',
                              `model_desc` varchar(255) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '模型描述',
                              `model_type` int(2) DEFAULT NULL COMMENT '模型模板',
                              `project_id` bigint(20) DEFAULT NULL COMMENT '项目id',
                              `resource_num` int(8) DEFAULT NULL COMMENT '资源个数',
                              `y_value_column` varchar(255) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT 'y值字段',
                              `component_speed` varchar(255) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '组件执行进度id',
                              `train_type` tinyint(4) DEFAULT '0' COMMENT '训练类型 0纵向 1横向 默认纵向',
                              `is_draft` tinyint(4) DEFAULT '0' COMMENT '是否草稿 0是 1不是 默认是',
                              `user_id` bigint(20) DEFAULT NULL COMMENT '用户id',
                              `organ_id` bigint(20) DEFAULT NULL COMMENT '机构id',
                              `component_json` blob COMMENT '组件json',
                              `latest_task_id` int(8) DEFAULT NULL COMMENT '最近运行一次任务id',
                              `latest_cost_time` datetime DEFAULT NULL COMMENT '最近一次运行时间',
                              `latest_task_status` int(2) DEFAULT NULL COMMENT '最近一次任务状态',
                              `latest_alignment_ratio` decimal(12,6) DEFAULT NULL COMMENT '数据对齐比例',
                              `latest_alignment_cost` int(10) DEFAULT NULL COMMENT '数据对齐耗时',
                              `latest_analyze_ratio` decimal(12,6) DEFAULT NULL COMMENT '统计分析比例',
                              `latest_analyze_cost` int(10) DEFAULT NULL COMMENT '统计分析耗时',
                              `latest_feature_ratio` decimal(12,6) DEFAULT NULL COMMENT '特征筛选比例',
                              `latest_feature_cost` int(10) DEFAULT NULL COMMENT '统计分析耗时',
                              `latest_sample_ratio` decimal(12,6) DEFAULT NULL COMMENT '样本抽样设计比例',
                              `latest_sample_cost` int(10) DEFAULT NULL COMMENT '样本抽样设计耗时',
                              `latest_train_ratio` decimal(12,6) DEFAULT NULL COMMENT '训练测试设计比例',
                              `latest_train_cost` int(10) DEFAULT NULL COMMENT '训练测试设计耗时',
                              `latest_lack_ratio` decimal(12,6) DEFAULT NULL COMMENT '缺失值处理比例',
                              `latest_lack_cost` int(10) DEFAULT NULL COMMENT '缺失值处理耗时',
                              `latest_exception_ratio` decimal(12,6) DEFAULT NULL COMMENT '异常值处理比例',
                              `latest_exception_cost` int(10) DEFAULT NULL COMMENT '异常值处理耗时',
                              `is_del` tinyint(4) DEFAULT '0' COMMENT '是否删除',
                              `create_date` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                              `update_date` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
                              PRIMARY KEY (`model_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='模型表';
-- ----------------------------
-- Table structure for data_model_component
-- ----------------------------
DROP TABLE IF EXISTS `data_model_component`;
CREATE TABLE `data_model_component` (
                                        `mc_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '关系id',
                                        `model_id` bigint(20) DEFAULT NULL COMMENT '模型id',
                                        `input_component_id` bigint(20) DEFAULT NULL COMMENT '输入组件id',
                                        `output_component_id` bigint(20) DEFAULT NULL COMMENT '输出组件id',
                                        `point_type` varchar(255) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '指向类型(直线、曲线图等等)',
                                        `point_json` varchar(255) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '指向json数据',
                                        `is_del` tinyint(4) DEFAULT '0' COMMENT '是否删除',
                                        `create_date` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                        `update_date` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
                                        PRIMARY KEY (`mc_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='组件模型关系表';
-- ----------------------------
-- Table structure for data_component
-- ----------------------------
DROP TABLE IF EXISTS `data_component`;
CREATE TABLE `data_component` (
                                  `component_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '组件id',
                                  `front_component_id` varchar(255) DEFAULT NULL COMMENT '前端组件id',
                                  `model_id` bigint(20) DEFAULT NULL COMMENT '模型id',
                                  `component_code` varchar(255) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '组件code',
                                  `component_name` varchar(255) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '组件名称',
                                  `shape` varchar(255) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '形状',
                                  `width` int(11) DEFAULT '0' COMMENT '宽度',
                                  `height` int(11) DEFAULT '0' COMMENT '高度',
                                  `coordinate_y` int(11) DEFAULT '0' COMMENT '坐标y',
                                  `coordinate_x` int(11) DEFAULT '0' COMMENT '坐标x',
                                  `data_json` varchar(255) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '组件参数json',
                                  `start_time` bigint(20) DEFAULT '0' COMMENT '开始时间戳',
                                  `end_time` bigint(20) DEFAULT '0' COMMENT '结束时间戳',
                                  `component_state` tinyint(4) DEFAULT '0' COMMENT '组件运行状态 0初始 1成功 2运行中 3失败',
                                  `input_file_path` varchar(255) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '输入文件路径',
                                  `output_file_path` varchar(255) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '输出文件路径',
                                  `is_del` tinyint(4) DEFAULT '0' COMMENT '是否删除',
                                  `create_date` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                  `update_date` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
                                  PRIMARY KEY (`component_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='组件表';

-- ----------------------------
-- Table structure for data_model_quota
-- ----------------------------
DROP TABLE IF EXISTS `data_model_quota`;
CREATE TABLE `data_model_quota` (
                                    `quota_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '指标id',
                                    `quota_type` int(2) DEFAULT NULL COMMENT '样本集类型（训练样本集，测试样本集）',
                                    `quota_images` varchar(255) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '样本集图片',
                                    `model_id` bigint(20) DEFAULT NULL COMMENT '模型id',
                                    `component_id` bigint(20) DEFAULT NULL COMMENT '组件id',
                                    `auc` decimal(12,6) DEFAULT NULL COMMENT 'auc',
                                    `ks` decimal(12,6) DEFAULT NULL COMMENT 'ks',
                                    `gini` decimal(12,6) DEFAULT NULL COMMENT 'gini',
                                    `precision` decimal(12,6) DEFAULT NULL COMMENT 'precision',
                                    `recall` decimal(12,6) DEFAULT NULL COMMENT 'recall',
                                    `f1_score` decimal(12,6) DEFAULT NULL COMMENT 'f1_score',
                                    `is_del` tinyint(4) DEFAULT '0' COMMENT '是否删除',
                                    `create_date` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                    `update_date` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
                                    PRIMARY KEY (`quota_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='模板指标入参';


-- ----------------------------
-- Table structure for data_model_task
-- ----------------------------
DROP TABLE IF EXISTS `data_model_task`;
CREATE TABLE `data_model_task` (
                                   `task_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务id',
                                   `task_status` int(2) DEFAULT NULL COMMENT '任务状态',
                                   `model_id` bigint(20) DEFAULT NULL COMMENT '模型id',
                                   `cost_time` datetime DEFAULT NULL COMMENT '运行时间',
                                   `alignment_ratio` decimal(12,6) DEFAULT NULL COMMENT '数据对齐比例',
                                   `alignment_cost` int(10) DEFAULT NULL COMMENT '数据对齐耗时',
                                   `analyze_ratio` decimal(12,6) DEFAULT NULL COMMENT '统计分析比例',
                                   `analyze_cost` int(10) DEFAULT NULL COMMENT '统计分析耗时',
                                   `feature_ratio` decimal(12,6) DEFAULT NULL COMMENT '特征筛选比例',
                                   `feature_cost` int(10) DEFAULT NULL COMMENT '统计分析耗时',
                                   `sample_ratio` decimal(12,6) DEFAULT NULL COMMENT '样本抽样设计比例',
                                   `sample_cost` int(10) DEFAULT NULL COMMENT '样本抽样设计耗时',
                                   `train_ratio` decimal(12,6) DEFAULT NULL COMMENT '训练测试设计比例',
                                   `train_cost` int(10) DEFAULT NULL COMMENT '训练测试设计耗时',
                                   `lack_ratio` decimal(12,6) DEFAULT NULL COMMENT '缺失值处理比例',
                                   `lack_cost` int(10) DEFAULT NULL COMMENT '缺失值处理耗时',
                                   `exception_ratio` decimal(12,6) DEFAULT NULL COMMENT '异常值处理比例',
                                   `exception_cost` int(10) DEFAULT NULL COMMENT '异常值处理耗时',
                                   `is_del` tinyint(4) DEFAULT '0' COMMENT '是否删除',
                                   `create_date` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                   `update_date` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
                                   PRIMARY KEY (`task_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='模型任务表';
-- ----------------------------
-- Table structure for data_mr
-- ----------------------------
DROP TABLE IF EXISTS `data_mr`;
CREATE TABLE `data_mr`  (
                            `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '资源id',
                            `model_id` bigint(20) DEFAULT NULL COMMENT '模型id',
                            `resource_id` bigint(20) DEFAULT NULL COMMENT '资源id',
                            `alignment_num` int(8) DEFAULT NULL COMMENT '对齐后记录数量',
                            `primitive_param_num` int(8) DEFAULT NULL COMMENT '原始变量数量',
                            `modelParam_num` int(8) DEFAULT NULL COMMENT '入模变量数量',
                            `is_del` tinyint(4) DEFAULT '0' COMMENT '是否删除',
                            `create_date` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                            `update_date` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
                            PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '模型资源表' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `data_project`;
CREATE TABLE `data_project` (
                                `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增ID',
                                `project_id` varchar(141) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '项目ID 机构后12位+UUID',
                                `project_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '项目名称',
                                `project_desc` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '项目描述',
                                `created_organ_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '机构id',
                                `created_organ_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '机构名称',
                                `created_username` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '创建者名称',
                                `resource_num` int DEFAULT '0' COMMENT '资源数',
                                `provider_organ_names` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '协作方机构名称 保存三个',
                                `server_address` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '中心节点地址',
                                `status` tinyint DEFAULT '0' COMMENT '项目状态 0审核中 1可用 2关闭',
                                `is_del` tinyint DEFAULT '0' COMMENT '是否删除',
                                `create_date` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                `update_date` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
                                PRIMARY KEY (`id`) USING BTREE,
                                UNIQUE INDEX `project_id_ix`(`project_id`) USING BTREE,
                                INDEX `created_organ_id_ix`(`created_organ_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci  COMMENT='项目表' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `data_project_organ`;
CREATE TABLE `data_project_organ` (
                                      `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
                                      `po_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '项目机构关联ID UUID',
                                      `project_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '项目ID',
                                      `organ_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '机构ID',
                                      `initiate_organ_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '发起方机构ID',
                                      `participation_identity` tinyint DEFAULT NULL COMMENT '机构项目中参与身份 1发起者 2协作者',
                                      `audit_status` tinyint DEFAULT NULL COMMENT '审核状态 0审核中 1同意 2拒绝',
                                      `audit_opinion` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '审核意见',
                                      `secretkey_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '秘钥ID',
                                      `server_address` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '中心节点地址',
                                      `is_del` tinyint DEFAULT '0' COMMENT '是否删除',
                                      `create_date` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                      `update_date` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
                                      PRIMARY KEY (`id`) USING BTREE,
                                      INDEX `project_id_ix`(`project_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT='项目资源授权审核表' ROW_FORMAT = Dynamic;


DROP TABLE IF EXISTS `data_project_resource`;
CREATE TABLE `data_project_resource` (
                                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
                                         `pr_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '项目资源ID  UUID',
                                         `project_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '项目id',
                                         `initiate_organ_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '发起方机构ID',
                                         `organ_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '机构ID',
                                         `participation_identity` tinyint(1) DEFAULT NULL COMMENT '机构项目中参与身份 1发起者 2协作者',
                                         `is_del` tinyint(1) DEFAULT '0' COMMENT '是否删除',
                                         `resource_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '0' COMMENT '资源ID',
                                         `audit_status` tinyint DEFAULT NULL COMMENT '审核状态 0审核中 1同意 2拒绝',
                                         `audit_opinion` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '审核意见',
                                         `secretkey_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '秘钥ID',
                                         `server_address` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '中心节点地址',
                                         `create_date` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                         `update_date` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
                                         PRIMARY KEY (`id`) USING BTREE,
                                         INDEX `project_id_ix`(`project_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT='项目资源关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for data_psi
-- ----------------------------
DROP TABLE IF EXISTS `data_psi`;
CREATE TABLE `data_psi`  (
                             `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'psi 主键',
                             `own_organ_id` varchar(255) DEFAULT NULL COMMENT '本机构id',
                             `own_resource_id` bigint(20) DEFAULT NULL COMMENT '本机构资源id',
                             `own_keyword` varchar(255) DEFAULT NULL COMMENT '本机构资源关键字',
                             `other_organ_id` varchar(255) DEFAULT NULL COMMENT '其他机构id',
                             `other_resource_id` varchar(255) DEFAULT NULL COMMENT '其他机构资源id',
                             `other_keyword` varchar(255) DEFAULT NULL COMMENT '其他机构资源关键字',
                             `output_file_path_type` tinyint(4) DEFAULT '0' COMMENT '文件路径输出类型 0默认 自动生成',
                             `output_no_repeat` tinyint(4) DEFAULT '0' COMMENT '输出内容是否不去重 默认0 不去重 1去重',
                             `column_complete_statistics` tinyint(4) DEFAULT '0' COMMENT '是否对"可统计"的附加列做全表统计 默认0 是 1不是',
                             `result_name` varchar(255) DEFAULT NULL COMMENT '结果名称',
                             `output_content` int(11) DEFAULT '0' COMMENT '输出内容 默认0 0交集 1差集',
                             `output_format` varchar(255) DEFAULT NULL COMMENT '输出格式',
                             `result_organ_ids` varchar(255) DEFAULT NULL COMMENT '结果获取方 多机构","号间隔',
                             `server_address` varchar(255) DEFAULT NULL COMMENT '其他机构中心节点地址',
                             `remarks` varchar(255) DEFAULT NULL COMMENT '备注',
                             `user_id` bigint(20) DEFAULT NULL COMMENT '用户id',
                             `is_del` tinyint(4) DEFAULT '0' COMMENT '是否删除',
                             `create_date` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                             `update_date` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
                             PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;


-- ----------------------------
-- Table structure for data_psi_resource
-- ----------------------------
DROP TABLE IF EXISTS `data_psi_resource`;
CREATE TABLE `data_psi_resource`  (
                                      `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'psi资源id',
                                      `resource_id` bigint(20) DEFAULT NULL COMMENT '资源id',
                                      `psi_resource_desc` varchar(255) DEFAULT NULL COMMENT 'psi资源描述',
                                      `table_structure_template` varchar(255) DEFAULT NULL COMMENT '表结构模板',
                                      `organ_type` int(11) DEFAULT NULL COMMENT '机构类型',
                                      `results_allow_open` int(11) DEFAULT NULL COMMENT '是否允许结果出现在对方节点上',
                                      `keyword_list` varchar(255) DEFAULT NULL COMMENT '关键字 关键字:类型,关键字:类型.....',
                                      `is_del` tinyint(4) DEFAULT '0' COMMENT '是否删除',
                                      `create_date` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                      `update_date` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
                                      PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;


-- ----------------------------
-- Table structure for data_psi_task
-- ----------------------------
DROP TABLE IF EXISTS `data_psi_task`;
CREATE TABLE `data_psi_task`  (
                                  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'psi任务id',
                                  `psi_id` bigint(20) DEFAULT NULL COMMENT 'psi id',
                                  `task_id` varchar(255) DEFAULT NULL COMMENT '对外展示的任务uuid 同时也是文件名称',
                                  `task_state` int(11) DEFAULT '0' COMMENT '运行状态 0未运行 1运行中 2完成 默认0',
                                  `ascription_type` int(11) DEFAULT '0' COMMENT '归属类型 0一方 1双方',
                                  `ascription` varchar(255) DEFAULT NULL COMMENT '结果归属',
                                  `file_rows` int(11) DEFAULT '0' COMMENT '文件行数',
                                  `file_path` varchar(255) DEFAULT NULL COMMENT '文件路径',
                                  `file_content` blob COMMENT '文件内容',
                                  `is_del` tinyint(4) DEFAULT '0' COMMENT '是否删除',
                                  `create_date` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                  `update_date` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
                                  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;
-- ----------------------------
-- Table structure for data_resource
-- ----------------------------
DROP TABLE IF EXISTS `data_resource`;
CREATE TABLE `data_resource`  (
                                  `resource_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '资源id',
                                  `resource_name` varchar(255) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '资源名称',
                                  `resource_desc` varchar(255) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '资源描述',
                                  `resource_sort_type` int(2) DEFAULT NULL COMMENT '资源分类（银行，电商，媒体，运营商，保险）',
                                  `resource_auth_type` int(1) DEFAULT NULL COMMENT '授权类型（公开，私有）',
                                  `resource_source` int(1) DEFAULT NULL COMMENT '资源来源（文件上传，数据库链接）',
                                  `resource_num` int(8) DEFAULT NULL COMMENT '资源数',
                                  `file_id` int(8) DEFAULT NULL COMMENT '文件id',
                                  `file_size` int(32) DEFAULT NULL COMMENT '文件大小',
                                  `file_suffix` varchar(255) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '文件后缀',
                                  `file_rows` int(8) DEFAULT NULL COMMENT '文件行数',
                                  `file_columns` int(8) DEFAULT NULL COMMENT '文件列数',
                                  `file_handle_status` tinyint(4) DEFAULT NULL COMMENT '文件处理状态',
                                  `file_handle_field` blob COMMENT '文件头字段',
                                  `file_contains_y` tinyint(4) DEFAULT '0' COMMENT '文件字段是否包含y字段 0否 1是',
                                  `file_y_rows` int(11) DEFAULT '0' COMMENT '文件字段y值内容不为空的行数',
                                  `file_y_ratio` decimal(8,4) DEFAULT '0.0000' COMMENT '文件字段y值内容不为空的行数在总行的占比',
                                  `public_organ_id` varchar(3072) DEFAULT NULL COMMENT '机构列表',
                                  `resource_fusion_id` varchar(255) DEFAULT NULL COMMENT '中心节点资源ID',
                                  `db_id` int(8) DEFAULT NULL COMMENT '数据库id',
                                  `user_id` bigint(20) DEFAULT NULL COMMENT '用户id',
                                  `organ_id` bigint(20) DEFAULT NULL COMMENT '机构id',
                                  `url` varchar(255) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '资源表示路径',
                                  `is_del` tinyint(4) DEFAULT '0' COMMENT '是否删除',
                                  `create_date` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                  `update_date` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
                                  PRIMARY KEY (`resource_id`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '资源表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for data_resource_tag
-- ----------------------------
DROP TABLE IF EXISTS `data_resource_tag`;
CREATE TABLE `data_resource_tag`  (
                                      `tag_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '标签id',
                                      `tag_name` varchar(255) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '标签名称',
                                      `is_del` tinyint(4) DEFAULT '0' COMMENT '是否删除',
                                      `create_date` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                      `update_date` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
                                      PRIMARY KEY (`tag_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '标签表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for data_rt
-- ----------------------------
DROP TABLE IF EXISTS `data_rt`;
CREATE TABLE `data_rt`  (
                            `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
                            `resource_id` bigint(20) DEFAULT NULL COMMENT '资源id',
                            `tag_id` bigint(20) DEFAULT NULL COMMENT '标签id',
                            `is_del` tinyint(4) DEFAULT '0' COMMENT '是否删除',
                            `create_date` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                            `update_date` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
                            PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '资源标签关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for data_file_field
-- ----------------------------
DROP TABLE IF EXISTS `data_file_field`;
CREATE TABLE `data_file_field` (
                                   `field_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '字段id',
                                   `file_id` bigint(20) DEFAULT NULL COMMENT '文件id',
                                   `resource_id` bigint(20) DEFAULT NULL COMMENT '资源id',
                                   `field_name` varchar(255) DEFAULT NULL COMMENT '字段名称',
                                   `field_as` varchar(255) DEFAULT NULL COMMENT '字段别名',
                                   `field_type` int(11) DEFAULT '0' COMMENT '字段类型 默认0 string',
                                   `field_desc` varchar(255) DEFAULT NULL COMMENT '字段描述',
                                   `relevance` int(11) DEFAULT '0' COMMENT '关键字 0否 1是',
                                   `grouping` int(11) DEFAULT '0' COMMENT '分组 0否 1是',
                                   `protection_status` int(11) DEFAULT '0' COMMENT '保护开关 0关闭 1开启',
                                   `is_del` tinyint(4) DEFAULT '0' COMMENT '是否删除',
                                   `create_date` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                   `update_date` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
                                   PRIMARY KEY (`field_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE = utf8_general_ci COMMENT = '资源字段表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for data_mpc_task
-- ----------------------------
DROP TABLE IF EXISTS `data_mpc_task`;
CREATE TABLE `data_mpc_task` (
                                 `task_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务id',
                                 `task_id_name` varchar(255) DEFAULT NULL COMMENT '任务id对外展示',
                                 `script_id` bigint(20) DEFAULT NULL COMMENT '脚本id',
                                 `user_id` bigint(20) DEFAULT NULL COMMENT '用户id',
                                 `task_status` int(11) DEFAULT '0' COMMENT '任务状态 0未运行 1成功 2运行中 3失败',
                                 `task_desc` varchar(255) DEFAULT NULL COMMENT '任务备注',
                                 `log_data` blob COMMENT '日志信息',
                                 `result_file_path` varchar(255) DEFAULT NULL COMMENT '结果文件地址',
                                 `is_del` tinyint(4) DEFAULT '0' COMMENT '是否删除',
                                 `create_date` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                 `update_date` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
                                 PRIMARY KEY (`task_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8  COMMENT = 'mpc任务表' ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for data_script
-- ----------------------------
DROP TABLE IF EXISTS `data_script`;
CREATE TABLE `data_script`  (
                                `script_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '脚本id',
                                `name` varchar(255) DEFAULT NULL COMMENT '文件名称或文件夹名称',
                                `catalogue` int(11) DEFAULT '0' COMMENT '是否目录 0否 1是',
                                `p_script_id` bigint(20) DEFAULT NULL COMMENT '上级id',
                                `script_type` int(11) DEFAULT NULL COMMENT '脚本类型 0sql 1python',
                                `script_status` int(11) DEFAULT NULL COMMENT '脚本状态 0打开 1关闭 默认打开',
                                `script_content` blob COMMENT '脚本内容',
                                `user_id` bigint(20) DEFAULT NULL COMMENT '用户id',
                                `organ_id` bigint(20) DEFAULT NULL COMMENT '机构id',
                                `is_del` tinyint(4) DEFAULT '0' COMMENT '是否删除',
                                `create_date` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                `update_date` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
                                PRIMARY KEY (`script_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;
-- ----------------------------
-- Table structure for data_task
-- ----------------------------
DROP TABLE IF EXISTS `data_task`;
CREATE TABLE `data_task` (
                             `task_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务id',
                             `task_id_name` varchar(255) DEFAULT NULL COMMENT '任务id展示名',
                             `task_name` varchar(255) DEFAULT NULL COMMENT '任务名称',
                             `task_state` int(11) DEFAULT '0' COMMENT '任务状态(0未开始 1成功 2运行中 3失败 4取消)',
                             `task_type` int(11) DEFAULT NULL COMMENT '任务类型 1、模型 2、PSI 3、PIR',
                             `task_result_path` varchar(255) DEFAULT NULL COMMENT '文件返回路径',
                             `task_result_content` blob COMMENT '文件返回内容',
                             `task_start_time` bigint(20) DEFAULT NULL COMMENT '任务开始时间',
                             `task_end_time` bigint(20) DEFAULT NULL COMMENT '任务结束时间',
                             `task_user_id` bigint(20) DEFAULT NULL COMMENT '任务创建人',
                             `is_del` tinyint(4) DEFAULT '0' COMMENT '是否删除',
                             `create_date` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                             `update_date` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
                             PRIMARY KEY (`task_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='数据任务表';
-- ----------------------------
-- Table structure for sys_auth
-- ----------------------------
DROP TABLE IF EXISTS `sys_auth`;
CREATE TABLE `sys_auth`  (
                             `auth_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '权限id',
                             `auth_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '权限名称',
                             `auth_code` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '权限代码',
                             `auth_type` tinyint(4) NOT NULL COMMENT '权限类型 1 菜单 2 列表 3 按钮',
                             `p_auth_id` bigint(20) NOT NULL COMMENT '父id',
                             `r_auth_id` bigint(20) NOT NULL COMMENT '根id',
                             `full_path` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '完整路径',
                             `auth_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '过滤路径',
                             `data_auth_code` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '数据权限代码',
                             `auth_index` int(16) NOT NULL COMMENT '顺序',
                             `auth_depth` int(16) NOT NULL COMMENT '深度',
                             `is_show` tinyint(4) NOT NULL COMMENT '是否展示',
                             `is_editable` tinyint(4) NOT NULL COMMENT '是否可编辑',
                             `is_del` tinyint(4) NOT NULL COMMENT '是否删除',
                             `c_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                             `u_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
                             PRIMARY KEY (`auth_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1001 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_auth
-- ----------------------------
INSERT INTO `sys_auth` VALUES (1, '项目管理', 'Project', 1, 0, 1, '1', '', 'own', 1, 0, 1, 1, 0, '2022-04-12 13:59:16.000', '2022-04-24 10:33:51.979');
INSERT INTO `sys_auth` VALUES (2, '项目列表', 'ProjectList', 2, 1, 1, '1,2', '', 'own', 1, 1, 1, 1, 0, '2022-04-12 14:00:05.000', '2022-04-12 19:08:46.133');
INSERT INTO `sys_auth` VALUES (3, '项目详情', 'ProjectDetail', 4, 1, 1, '1,3', '', 'own', 2, 1, 1, 1, 0, '2022-04-12 14:01:36.098', '2022-04-15 02:58:42.183');
INSERT INTO `sys_auth` VALUES (4, '新建项目', 'ProjectCreate', 3, 2, 1, '1,2,4', '', 'own', 1, 2, 1, 1, 0, '2022-04-12 14:02:39.000', '2022-04-15 02:53:48.167');
INSERT INTO `sys_auth` VALUES (5, '删除项目', 'ProjectDelete', 3, 2, 1, '1,2,5', '', 'own', 2, 2, 1, 1, 0, '2022-04-12 14:03:27.000', '2022-04-15 02:53:50.484');
INSERT INTO `sys_auth` VALUES (6, '模型管理', 'ModelMenu', 1, 0, 6, '6', '', 'own', 2, 0, 1, 1, 0, '2022-04-12 14:04:31.000', '2022-04-12 19:04:41.268');
INSERT INTO `sys_auth` VALUES (7, '模型列表', 'ModelList', 2, 6, 6, '6,7', '', 'own', 1, 1, 1, 1, 0, '2022-04-12 14:05:57.000', '2022-04-12 19:08:46.133');
INSERT INTO `sys_auth` VALUES (8, '模型详情', 'ModelDetail', 4, 6, 6, '6,8', '', 'own', 2, 1, 1, 1, 0, '2022-04-12 14:07:38.000', '2022-04-15 03:00:45.136');
INSERT INTO `sys_auth` VALUES (9, '模型查看', 'ModelView', 3, 7, 6, '6,7,9', '', 'own', 1, 2, 1, 1, 0, '2022-04-12 14:08:25.000', '2022-04-15 02:56:12.398');
INSERT INTO `sys_auth` VALUES (10, '添加模型', 'ModelCreate', 3, 7, 6, '6,7,10', '', 'own', 2, 2, 1, 1, 0, '2022-04-12 14:09:32.000', '2022-04-15 02:56:14.857');
INSERT INTO `sys_auth` VALUES (11, '模型编辑', 'ModelEdit', 3, 7, 6, '6,7,11', '', 'own', 3, 2, 1, 1, 0, '2022-04-12 14:11:28.000', '2022-04-15 02:56:16.826');
INSERT INTO `sys_auth` VALUES (12, '匿踪查询', 'PrivateSearch', 1, 0, 12, '12', '', 'own', 3, 0, 1, 1, 0, '2022-04-12 14:12:57.000', '2022-04-12 19:04:41.268');
INSERT INTO `sys_auth` VALUES (13, '匿踪查询按钮', 'PrivateSearchButton', 3, 12, 12, '12,13', '', 'own', 1, 1, 1, 1, 0, '2022-04-12 14:14:52.000', '2022-04-12 19:08:46.133');
INSERT INTO `sys_auth` VALUES (14, '资源管理', 'ResourceMenu', 1, 0, 14, '14', '', 'own', 4, 0, 1, 1, 0, '2022-04-12 14:17:03.000', '2022-04-12 19:04:41.268');
INSERT INTO `sys_auth` VALUES (15, '资源概览', 'ResourceList', 2, 14, 14, '14,15', '', 'own', 1, 1, 1, 1, 0, '2022-04-12 14:18:04.000', '2022-04-12 19:08:46.133');
INSERT INTO `sys_auth` VALUES (16, '资源详情', 'ResourceDetail', 4, 14, 14, '14,16', '', 'own', 2, 1, 1, 1, 0, '2022-04-12 14:19:16.000', '2022-04-15 03:02:01.488');
INSERT INTO `sys_auth` VALUES (17, '上传资源', 'ResourceUpload', 3, 15, 14, '14,15,17', '', 'own', 1, 2, 1, 1, 0, '2022-04-12 14:35:56.000', '2022-04-15 03:02:04.775');
INSERT INTO `sys_auth` VALUES (18, '编辑资源', 'ResourceEdit', 3, 15, 14, '14,15,18', '', 'own', 2, 2, 1, 1, 0, '2022-04-12 14:37:32.000', '2022-04-15 03:02:07.847');
INSERT INTO `sys_auth` VALUES (19, '删除资源', 'ResourceDelete', 3, 15, 14, '14,15,19', '', 'own', 3, 2, 1, 1, 0, '2022-04-12 14:38:54.000', '2022-04-15 03:02:10.279');
INSERT INTO `sys_auth` VALUES (20, '授权申请记录', 'ResourceRecord', 2, 14, 14, '14,20', '', 'own', 2, 1, 1, 1, 0, '2022-04-12 14:42:14.000', '2022-04-12 19:08:46.133');
INSERT INTO `sys_auth` VALUES (21, '授权审批', 'ResourceApproval', 3, 20, 14, '14,20,21', '', 'own', 1, 2, 1, 1, 0, '2022-04-12 14:43:45.000', '2022-04-12 19:10:12.574');
INSERT INTO `sys_auth` VALUES (22, '授权审批', 'ResourceApprovalList', 2, 14, 14, '14,22', '', 'own', 3, 1, 1, 1, 0, '2022-04-12 18:10:01.000', '2022-04-12 19:08:46.133');
INSERT INTO `sys_auth` VALUES (23, '授权审批按钮', 'ResourceApprovalButton', 3, 22, 14, '14,22,23', '', 'own', 1, 2, 1, 1, 0, '2022-04-12 18:10:52.000', '2022-04-12 19:10:12.574');
INSERT INTO `sys_auth` VALUES (24, '系统设置', 'Setting', 1, 0, 24, '24', '', 'own', 5, 0, 1, 0, 0, '2022-04-12 18:15:46.000', '2022-04-12 19:04:41.268');
INSERT INTO `sys_auth` VALUES (25, '用户管理', 'UserManage', 2, 24, 24, '24,25', '', 'own', 1, 1, 1, 0, 0, '2022-04-12 18:18:19.000', '2022-04-12 19:08:46.133');
INSERT INTO `sys_auth` VALUES (26, '用户新增', 'UserAdd', 3, 25, 24, '24,25,26', '', 'own', 1, 2, 1, 0, 0, '2022-04-12 18:24:12.000', '2022-04-12 19:10:12.574');
INSERT INTO `sys_auth` VALUES (27, '用户编辑', 'UserEdit', 3, 25, 24, '24,25,27', '', 'own', 2, 2, 1, 0, 0, '2022-04-12 18:25:08.000', '2022-04-12 19:10:12.574');
INSERT INTO `sys_auth` VALUES (28, '用户删除', 'UserDelete', 3, 25, 24, '24,25,28', '', 'own', 3, 2, 1, 0, 0, '2022-04-12 18:25:49.000', '2022-04-12 19:10:12.574');
INSERT INTO `sys_auth` VALUES (29, '密码重置', 'UserPasswordReset', 3, 25, 24, '24,25,29', '', 'own', 4, 2, 1, 0, 0, '2022-04-12 18:26:31.000', '2022-04-12 19:10:12.574');
INSERT INTO `sys_auth` VALUES (30, '角色管理', 'RoleManage', 2, 24, 24, '24,30', '', 'own', 2, 1, 1, 0, 0, '2022-04-12 18:27:45.000', '2022-04-12 19:08:46.133');
INSERT INTO `sys_auth` VALUES (31, '角色新增', 'RoleAdd', 3, 30, 24, '24,30,31', '', 'own', 1, 2, 1, 0, 0, '2022-04-12 18:28:46.000', '2022-04-12 19:10:12.574');
INSERT INTO `sys_auth` VALUES (32, '角色编辑', 'RoleEdit', 3, 30, 24, '24,30,32', '', 'own', 2, 2, 1, 0, 0, '2022-04-12 18:29:29.000', '2022-04-12 19:10:12.574');
INSERT INTO `sys_auth` VALUES (33, '角色删除', 'RoleDelete', 3, 30, 24, '24,30,33', '', 'own', 3, 2, 1, 0, 0, '2022-04-12 18:30:42.000', '2022-04-12 19:10:12.574');
INSERT INTO `sys_auth` VALUES (34, '机构管理', 'OrganManage', 2, 24, 24, '24,34', '', 'own', 3, 1, 1, 0, 0, '2022-04-12 18:32:26.000', '2022-04-12 19:08:46.133');
INSERT INTO `sys_auth` VALUES (35, '机构新增', 'OrganAdd', 3, 34, 24, '24,34,35', '', 'own', 1, 2, 1, 0, 0, '2022-04-12 18:34:11.000', '2022-04-12 19:10:12.574');
INSERT INTO `sys_auth` VALUES (36, '机构编辑', 'OrganEdit', 3, 34, 24, '24,34,36', '', 'own', 2, 2, 1, 0, 0, '2022-04-12 18:35:10.000', '2022-04-12 19:10:12.574');
INSERT INTO `sys_auth` VALUES (37, '机构删除', 'OrganDelete', 3, 34, 24, '24,34,37', '', 'own', 3, 2, 1, 0, 0, '2022-04-12 18:35:56.000', '2022-04-12 19:10:12.574');
INSERT INTO `sys_auth` VALUES (38, '菜单管理', 'MenuManage', 2, 24, 24, '24,38', '', 'own', 4, 1, 1, 0, 0, '2022-04-12 18:38:00.000', '2022-04-12 19:08:46.133');
INSERT INTO `sys_auth` VALUES (39, '菜单新增', 'MenuAdd', 3, 38, 24, '24,38,39', '', 'own', 1, 2, 1, 0, 0, '2022-04-12 18:45:34.000', '2022-04-12 19:10:12.574');
INSERT INTO `sys_auth` VALUES (40, '菜单编辑', 'MenuEdit', 3, 38, 24, '24,38,40', '', 'own', 2, 2, 1, 0, 0, '2022-04-12 18:46:43.000', '2022-04-12 19:10:12.574');
INSERT INTO `sys_auth` VALUES (41, '菜单删除', 'MenuDelete', 3, 38, 24, '24,38,41', '', 'own', 3, 2, 1, 0, 0, '2022-04-12 18:48:22.000', '2022-04-12 19:10:12.574');
INSERT INTO `sys_auth` VALUES (42, '匿踪查询列表', 'PrivateSearchList', 2, 12, 12, '12,42', '', 'own', 1, 1, 1, 1, 0, '2022-04-18 03:05:12.000', '2022-04-18 03:05:12.000');

-- ----------------------------
-- Table structure for sys_ra
-- ----------------------------
DROP TABLE IF EXISTS `sys_ra`;
CREATE TABLE `sys_ra`  (
                           `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增id',
                           `role_id` bigint(20) NOT NULL COMMENT '角色id',
                           `auth_id` bigint(20) NOT NULL COMMENT '权限id',
                           `is_del` tinyint(2) NOT NULL COMMENT '是否删除',
                           `c_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                           `u_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
                           PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1000 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '角色权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_ra
-- ----------------------------
INSERT INTO `sys_ra` VALUES (1, 1, 1, 0, '2022-03-25 17:34:45.902', '2022-03-25 17:35:02.170');
INSERT INTO `sys_ra` VALUES (2, 1, 2, 0, '2022-03-25 17:34:45.902', '2022-03-25 17:35:05.697');
INSERT INTO `sys_ra` VALUES (3, 1, 3, 0, '2022-03-25 17:34:45.902', '2022-03-25 17:35:08.204');
INSERT INTO `sys_ra` VALUES (4, 1, 4, 0, '2022-03-25 17:34:45.902', '2022-03-25 17:35:10.879');
INSERT INTO `sys_ra` VALUES (5, 1, 5, 0, '2022-03-25 17:34:45.902', '2022-03-25 17:35:13.112');
INSERT INTO `sys_ra` VALUES (6, 1, 6, 0, '2022-03-25 17:34:45.902', '2022-03-25 17:35:15.284');
INSERT INTO `sys_ra` VALUES (7, 1, 7, 0, '2022-03-25 17:34:45.902', '2022-03-25 17:35:17.772');
INSERT INTO `sys_ra` VALUES (8, 1, 8, 0, '2022-03-25 17:34:45.902', '2022-03-25 17:35:20.136');
INSERT INTO `sys_ra` VALUES (9, 1, 9, 0, '2022-03-25 17:34:45.902', '2022-03-25 17:35:22.282');
INSERT INTO `sys_ra` VALUES (10, 1, 10, 0, '2022-03-25 17:34:45.902', '2022-03-25 17:35:25.230');
INSERT INTO `sys_ra` VALUES (11, 1, 11, 0, '2022-03-25 17:34:45.902', '2022-03-25 17:35:26.705');
INSERT INTO `sys_ra` VALUES (12, 1, 12, 0, '2022-03-25 17:34:45.902', '2022-03-25 17:35:27.781');
INSERT INTO `sys_ra` VALUES (13, 1, 13, 0, '2022-03-25 17:34:45.902', '2022-03-25 17:35:30.548');
INSERT INTO `sys_ra` VALUES (14, 1, 14, 0, '2022-03-25 17:34:45.902', '2022-03-25 17:35:32.214');
INSERT INTO `sys_ra` VALUES (15, 1, 15, 0, '2022-03-25 17:34:45.902', '2022-03-25 17:35:34.343');
INSERT INTO `sys_ra` VALUES (16, 1, 16, 0, '2022-03-25 17:34:45.902', '2022-03-25 17:35:36.784');
INSERT INTO `sys_ra` VALUES (17, 1, 17, 0, '2022-03-25 17:34:45.902', '2022-03-25 17:35:38.966');
INSERT INTO `sys_ra` VALUES (18, 1, 18, 0, '2022-03-25 17:34:45.902', '2022-03-25 17:35:41.113');
INSERT INTO `sys_ra` VALUES (19, 1, 19, 0, '2022-03-25 17:34:45.902', '2022-03-25 17:35:43.212');
INSERT INTO `sys_ra` VALUES (20, 1, 20, 0, '2022-03-25 17:34:45.902', '2022-03-25 17:35:45.530');
INSERT INTO `sys_ra` VALUES (21, 1, 21, 0, '2022-03-25 17:34:45.902', '2022-03-25 17:35:48.887');
INSERT INTO `sys_ra` VALUES (22, 1, 22, 0, '2022-03-25 17:34:45.902', '2022-03-25 17:35:55.679');
INSERT INTO `sys_ra` VALUES (23, 1, 23, 0, '2022-03-25 17:34:45.902', '2022-03-25 17:35:58.959');
INSERT INTO `sys_ra` VALUES (24, 1, 24, 0, '2022-03-25 17:34:45.902', '2022-03-25 17:36:01.745');
INSERT INTO `sys_ra` VALUES (25, 1, 25, 0, '2022-03-25 17:34:45.902', '2022-03-25 17:36:04.763');
INSERT INTO `sys_ra` VALUES (26, 1, 26, 0, '2022-03-25 17:34:45.902', '2022-03-25 17:36:08.883');
INSERT INTO `sys_ra` VALUES (27, 1, 27, 0, '2022-03-25 17:34:45.902', '2022-03-25 17:36:11.293');
INSERT INTO `sys_ra` VALUES (28, 1, 28, 0, '2022-03-25 17:34:45.902', '2022-03-25 17:36:13.692');
INSERT INTO `sys_ra` VALUES (29, 1, 29, 0, '2022-03-25 17:34:45.902', '2022-03-25 17:36:15.825');
INSERT INTO `sys_ra` VALUES (30, 1, 30, 0, '2022-03-25 17:34:45.902', '2022-03-25 17:36:18.058');
INSERT INTO `sys_ra` VALUES (31, 1, 31, 0, '2022-03-25 17:34:45.902', '2022-03-25 17:36:21.553');
INSERT INTO `sys_ra` VALUES (32, 1, 32, 0, '2022-03-25 17:34:45.902', '2022-03-25 17:36:24.124');
INSERT INTO `sys_ra` VALUES (33, 1, 33, 0, '2022-03-25 17:34:45.902', '2022-03-25 17:36:24.124');
INSERT INTO `sys_ra` VALUES (34, 1, 34, 0, '2022-03-25 17:34:45.902', '2022-03-25 17:36:24.124');
INSERT INTO `sys_ra` VALUES (35, 1, 35, 0, '2022-03-25 17:34:45.902', '2022-03-25 17:36:24.124');
INSERT INTO `sys_ra` VALUES (36, 1, 36, 0, '2022-03-25 17:34:45.902', '2022-03-25 17:36:24.124');
INSERT INTO `sys_ra` VALUES (37, 1, 37, 0, '2022-03-25 17:34:45.902', '2022-03-25 17:36:24.124');
INSERT INTO `sys_ra` VALUES (38, 1, 38, 0, '2022-03-25 17:34:45.902', '2022-03-25 17:36:24.124');
INSERT INTO `sys_ra` VALUES (39, 1, 39, 0, '2022-03-25 17:34:45.902', '2022-03-25 17:36:24.124');
INSERT INTO `sys_ra` VALUES (40, 1, 40, 0, '2022-03-25 17:34:45.902', '2022-03-25 17:36:24.124');
INSERT INTO `sys_ra` VALUES (41, 1, 41, 0, '2022-03-25 17:34:45.902', '2022-03-25 17:36:24.124');
INSERT INTO `sys_ra` VALUES (1048, 1000, 6, 0, '2022-04-27 18:31:33.730', '2022-04-27 18:31:33.730');
INSERT INTO `sys_ra` VALUES (1049, 1000, 7, 0, '2022-04-27 18:31:33.730', '2022-04-27 18:31:33.730');
INSERT INTO `sys_ra` VALUES (1050, 1000, 9, 0, '2022-04-27 18:31:33.730', '2022-04-27 18:31:33.730');
INSERT INTO `sys_ra` VALUES (1051, 1000, 10, 0, '2022-04-27 18:31:33.730', '2022-04-27 18:31:33.730');
INSERT INTO `sys_ra` VALUES (1052, 1000, 11, 0, '2022-04-27 18:31:33.730', '2022-04-27 18:31:33.730');
INSERT INTO `sys_ra` VALUES (1053, 1000, 8, 0, '2022-04-27 18:31:33.730', '2022-04-27 18:31:33.730');
INSERT INTO `sys_ra` VALUES (1054, 1000, 12, 0, '2022-04-27 18:31:33.730', '2022-04-27 18:31:33.730');
INSERT INTO `sys_ra` VALUES (1055, 1000, 13, 0, '2022-04-27 18:31:33.730', '2022-04-27 18:31:33.730');
INSERT INTO `sys_ra` VALUES (1056, 1000, 42, 0, '2022-04-27 18:31:33.730', '2022-04-27 18:31:33.730');
INSERT INTO `sys_ra` VALUES (1057, 1000, 14, 0, '2022-04-27 18:31:33.730', '2022-04-27 18:31:33.730');
INSERT INTO `sys_ra` VALUES (1058, 1000, 15, 0, '2022-04-27 18:31:33.730', '2022-04-27 18:31:33.730');
INSERT INTO `sys_ra` VALUES (1059, 1000, 17, 0, '2022-04-27 18:31:33.730', '2022-04-27 18:31:33.730');
INSERT INTO `sys_ra` VALUES (1060, 1000, 18, 0, '2022-04-27 18:31:33.730', '2022-04-27 18:31:33.730');
INSERT INTO `sys_ra` VALUES (1061, 1000, 19, 0, '2022-04-27 18:31:33.730', '2022-04-27 18:31:33.730');
INSERT INTO `sys_ra` VALUES (1062, 1000, 16, 0, '2022-04-27 18:31:33.730', '2022-04-27 18:31:33.730');
INSERT INTO `sys_ra` VALUES (1063, 1000, 20, 0, '2022-04-27 18:31:33.730', '2022-04-27 18:31:33.730');
INSERT INTO `sys_ra` VALUES (1064, 1000, 21, 0, '2022-04-27 18:31:33.730', '2022-04-27 18:31:33.730');
INSERT INTO `sys_ra` VALUES (1065, 1000, 22, 0, '2022-04-27 18:31:33.730', '2022-04-27 18:31:33.730');
INSERT INTO `sys_ra` VALUES (1066, 1000, 23, 0, '2022-04-27 18:31:33.730', '2022-04-27 18:31:33.730');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
                             `role_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '角色id',
                             `role_name` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '角色名称',
                             `is_editable` tinyint(4) NOT NULL COMMENT '是否可编辑',
                             `is_del` tinyint(4) NOT NULL COMMENT '是否删除',
                             `c_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                             `u_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
                             PRIMARY KEY (`role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1000 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '超级管理员', 0, 0, '2022-03-25 17:08:52.100', '2022-03-25 17:43:29.970');
INSERT INTO `sys_role` VALUES (1000, '业务权限', 1, 0, '2022-04-27 17:50:02.139', '2022-04-27 17:50:02.139');

-- ----------------------------
-- Table structure for sys_ur
-- ----------------------------
DROP TABLE IF EXISTS `sys_ur`;
CREATE TABLE `sys_ur`  (
                           `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增id',
                           `user_id` bigint(20) NOT NULL COMMENT '用户id',
                           `role_id` bigint(20) NOT NULL COMMENT '角色id',
                           `is_del` bigint(20) NOT NULL COMMENT '是否删除',
                           `c_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                           `u_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
                           PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1000 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '用户角色关系表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_ur
-- ----------------------------
INSERT INTO `sys_ur` VALUES (1, 1, 1, 0, '2022-03-25 17:55:53.090', '2022-03-25 18:03:28.371');
INSERT INTO `sys_ur` VALUES (1000, 1000, 1000, 0, '2022-04-27 17:51:02.224', '2022-04-27 17:51:02.224');
INSERT INTO `sys_ur` VALUES (1001, 1001, 1000, 0, '2022-04-27 17:51:23.007', '2022-04-27 17:51:23.007');
INSERT INTO `sys_ur` VALUES (1002, 1002, 1000, 0, '2022-04-27 17:51:53.921', '2022-04-27 17:51:53.921');
INSERT INTO `sys_ur` VALUES (1003, 1003, 1000, 0, '2022-04-27 17:52:23.438', '2022-04-27 17:52:23.438');
INSERT INTO `sys_ur` VALUES (1004, 1004, 1000, 0, '2022-04-27 17:52:55.591', '2022-04-27 17:52:55.591');
INSERT INTO `sys_ur` VALUES (1005, 1005, 1, 0, '2022-04-27 17:53:26.346', '2022-04-27 17:53:26.346');
INSERT INTO `sys_ur` VALUES (1006, 1006, 1, 0, '2022-04-27 17:53:47.143', '2022-04-27 17:53:47.143');
INSERT INTO `sys_ur` VALUES (1007, 1007, 1, 0, '2022-04-27 17:54:14.284', '2022-04-27 17:54:14.284');
INSERT INTO `sys_ur` VALUES (1008, 1008, 1000, 0, '2022-04-27 17:54:37.559', '2022-04-27 17:54:37.559');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
                             `user_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户id',
                             `user_account` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '账户名称',
                             `user_password` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '账户密码',
                             `user_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户昵称',
                             `role_id_list` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '角色id集合',
                             `is_forbid` tinyint(4) NOT NULL COMMENT '是否禁用',
                             `is_editable` tinyint(4) NOT NULL COMMENT '是否可编辑',
                             `is_del` tinyint(4) NOT NULL COMMENT '是否删除',
                             `c_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                             `u_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更改时间',
                             PRIMARY KEY (`user_id`) USING BTREE,
                             UNIQUE INDEX `ix_unique_user_account`(`user_account`) USING BTREE COMMENT '账户名称唯一索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1000 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', 'a0f34ffac5a82245e4fca2e21f358a42', 'admin', '1', 0, 1, 0, '2022-03-25 17:55:53.048', '2022-03-25 18:03:07.552');

-- ----------------------------
-- Table structure for sys_file
-- ----------------------------
DROP TABLE IF EXISTS `sys_file`;
CREATE TABLE `sys_file`  (
                             `file_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '文件id',
                             `file_source` int(12) NOT NULL COMMENT '文件来源',
                             `file_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '文件地址',
                             `file_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '文件名称',
                             `file_suffix` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '文件后缀',
                             `file_size` bigint(20) NOT NULL COMMENT '文件实际大小',
                             `file_current_size` bigint(20) NOT NULL COMMENT '文件当前大小',
                             `file_area` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '文件区域',
                             `is_del` tinyint(4) NOT NULL COMMENT '是否删除',
                             `c_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                             `u_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
                             PRIMARY KEY (`file_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1000 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '文件表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for data_fusion_copy_task
-- ----------------------------
DROP TABLE IF EXISTS `data_fusion_copy_task`;
CREATE TABLE `data_fusion_copy_task` (
                                         `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
                                         `task_type` tinyint(4) NOT NULL COMMENT '任务类型 1 批量 2 单条',
                                         `current_offset` bigint(20) NOT NULL COMMENT '当前偏移量',
                                         `target_offset` bigint(20) NOT NULL COMMENT '目标便宜量',
                                         `task_table` varchar(64) NOT NULL COMMENT '复制任务表名',
                                         `fusion_server_address` varchar(64) NOT NULL COMMENT '连接中心地址',
                                         `latest_error_msg` varchar(1024) NOT NULL COMMENT '最近一次复制失败原因',
                                         `is_del` tinyint(4) NOT NULL COMMENT '是否删除',
                                         `c_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                         `u_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
                                         PRIMARY KEY (`id`) USING BTREE,
                                         KEY `current_offset_ix` (`current_offset`) USING BTREE,
                                         KEY `target_offset_ix` (`target_offset`) USING BTREE,
                                         KEY `c_time_ix` (`c_time`) USING BTREE,
                                         KEY `u_time_ix` (`u_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for data_resource_visibility_auth
-- ----------------------------
DROP TABLE IF EXISTS `data_resource_visibility_auth`;
CREATE TABLE `data_resource_visibility_auth`  (
                                                  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
                                                  `resource_id` bigint(20) NOT NULL COMMENT '资源id',
                                                  `organ_global_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '机构唯一id',
                                                  `organ_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '资源名称',
                                                  `organ_server_address` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '服务地址',
                                                  `is_del` tinyint(4) NOT NULL COMMENT '是否删除',
                                                  `c_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                                  `u_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
                                                  PRIMARY KEY (`id`) USING BTREE,
                                                  INDEX `resource_id_ix`(`resource_id`) USING BTREE,
                                                  INDEX `organ_global_id_ix`(`organ_global_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;