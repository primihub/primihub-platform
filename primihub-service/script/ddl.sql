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
                            `resource_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '资源id',
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
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (1, '项目管理', 'Project', 1, 0, 1, '1', '', 'own', 1, 0, 1, 1, 0, '2022-07-11 18:39:03.044', '2022-07-11 18:39:03.088');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (2, '项目列表', 'ProjectList', 2, 1, 1, '1,2', '/project/getProjectList', 'own', 1, 1, 1, 1, 0, '2022-07-11 18:39:03.093', '2022-07-11 18:39:03.096');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (3, '项目详情', 'ProjectDetail', 3, 1, 1, '1,3', '/project/getProjectDetails', 'own', 2, 1, 1, 1, 0, '2022-07-11 18:39:03.098', '2022-07-11 18:39:03.101');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (4, '新建项目', 'ProjectCreate', 3, 2, 1, '1,2,4', '/project/saveOrUpdateProject', 'own', 1, 2, 1, 1, 0, '2022-07-11 18:39:03.103', '2022-07-11 18:39:03.105');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (5, '关闭项目', 'ProjectDelete', 3, 2, 1, '1,2,5', '/project/closeProject', 'own', 2, 2, 1, 1, 0, '2022-07-11 18:39:03.106', '2022-07-11 18:39:03.108');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (6, '模型列表', 'ModelList', 2, 3, 1, '1,3,6', '/model/getmodellist', 'own', 1, 2, 1, 1, 0, '2022-07-11 18:39:03.109', '2022-07-11 18:39:03.110');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (7, '模型详情', 'ModelDetail', 3, 6, 1, '1,3,6,7', '/model/getdatamodel', 'own', 1, 3, 1, 1, 0, '2022-07-11 18:39:03.112', '2022-07-11 18:39:03.114');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (8, '模型查看', 'ModelView', 3, 6, 1, '1,3,6,8', '/model/getdatamodel', 'own', 2, 3, 1, 1, 0, '2022-07-11 18:39:03.115', '2022-07-11 18:39:03.117');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (9, '添加模型', 'ModelCreate', 3, 6, 1, '1,3,6,9', '/model/saveModelAndComponent', 'own', 3, 3, 1, 1, 0, '2022-07-11 18:39:03.118', '2022-07-11 18:39:03.119');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (10, '模型编辑', 'ModelEdit', 3, 6, 1, '1,3,6,10', '/model/saveModelAndComponent', 'own', 4, 3, 1, 1, 0, '2022-07-11 18:39:03.120', '2022-07-11 18:39:03.121');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (11, '匿踪查询', 'PrivateSearch', 1, 0, 11, '11', '/fusionResource/getResourceList', 'own', 2, 0, 1, 1, 0, '2022-07-11 18:39:03.122', '2022-07-11 18:39:03.124');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (12, '匿踪查询按钮', 'PrivateSearchButton', 3, 11, 11, '11,12', '/pir/pirSubmitTask', 'own', 1, 1, 1, 1, 0, '2022-07-11 18:39:03.125', '2022-07-11 18:39:03.128');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (13, '匿踪查询列表', 'PrivateSearchList', 2, 11, 11, '11,13', '/pir/downloadPirTask', 'own', 2, 1, 1, 1, 0, '2022-07-11 18:39:03.128', '2022-07-11 18:39:03.130');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (14, '隐私求交', 'PSI', 1, 0, 14, '14', '', 'own', 3, 0, 1, 1, 0, '2022-07-11 18:39:03.131', '2022-07-11 18:39:03.133');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (15, '求交任务', 'PSITask', 2, 14, 14, '14,15', '/psi/getPsiResourceAllocationList', 'own', 1, 1, 1, 1, 0, '2022-07-11 18:39:03.134', '2022-07-11 18:39:03.135');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (16, '求交结果', 'PSIResult', 2, 14, 14, '14,16', '/psi/getPsiTaskList', 'own', 2, 1, 1, 1, 0, '2022-07-11 18:39:03.139', '2022-07-11 18:39:03.142');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (17, '资源管理', 'ResourceMenu', 1, 0, 17, '17', '', 'own', 4, 0, 1, 1, 0, '2022-07-11 18:39:03.143', '2022-07-11 18:39:03.145');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (18, '资源概览', 'ResourceList', 2, 17, 17, '17,18', '/resource/getdataresourcelist', 'own', 1, 1, 1, 1, 0, '2022-07-11 18:39:03.147', '2022-07-11 18:39:03.148');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (19, '资源详情', 'ResourceDetail', 3, 17, 17, '17,19', '/resource/getdataresource', 'own', 2, 1, 1, 1, 0, '2022-07-11 18:39:03.149', '2022-07-11 18:39:03.151');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (20, '上传资源', 'ResourceUpload', 3, 17, 17, '17,20', '/resource/saveorupdateresource', 'own', 3, 1, 1, 1, 0, '2022-07-11 18:39:03.152', '2022-07-11 18:39:03.155');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (21, '编辑资源', 'ResourceEdit', 3, 17, 17, '17,21', '/resource/saveorupdateresource', 'own', 4, 1, 1, 1, 0, '2022-07-11 18:39:03.156', '2022-07-11 18:39:03.158');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (22, '联邦资源', 'UnionList', 2, 17, 17, '17,22', '/fusionResource/getResourceList', 'own', 5, 1, 1, 1, 0, '2022-07-11 18:39:03.159', '2022-07-11 18:39:03.161');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (23, '联邦资源详情', 'UnionResourceDetail', 3, 22, 17, '17,22,23', '/fusionResource/getDataResource', 'own', 1, 2, 1, 1, 0, '2022-07-11 18:39:03.162', '2022-07-11 18:39:03.164');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (24, '系统设置', 'Setting', 1, 0, 24, '24', '', 'own', 5, 0, 1, 0, 0, '2022-07-11 18:39:03.166', '2022-07-11 18:39:03.168');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (25, '用户管理', 'UserManage', 2, 24, 24, '24,25', '/user/findUserPage', 'own', 1, 1, 1, 0, 0, '2022-07-11 18:39:03.169', '2022-07-11 18:39:03.171');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (26, '用户新增', 'UserAdd', 3, 25, 24, '24,25,26', '/user/saveOrUpdateUser', 'own', 1, 2, 1, 0, 0, '2022-07-11 18:39:03.173', '2022-07-11 18:39:03.174');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (27, '用户编辑', 'UserEdit', 3, 25, 24, '24,25,27', '/user/saveOrUpdateUser', 'own', 2, 2, 1, 0, 0, '2022-07-11 18:39:03.176', '2022-07-11 18:39:03.177');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (28, '用户删除', 'UserDelete', 3, 25, 24, '24,25,28', '/user/deleteSysUser', 'own', 3, 2, 1, 0, 0, '2022-07-11 18:39:03.179', '2022-07-11 18:39:03.180');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (29, '密码重置', 'UserPasswordReset', 3, 25, 24, '24,25,29', '/user/initPassword', 'own', 4, 2, 1, 0, 0, '2022-07-11 18:39:03.182', '2022-07-11 18:39:03.183');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (30, '角色管理', 'RoleManage', 2, 24, 24, '24,30', '/role/findRolePage', 'own', 2, 1, 1, 0, 0, '2022-07-11 18:39:03.184', '2022-07-11 18:39:03.190');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (31, '角色新增', 'RoleAdd', 3, 30, 24, '24,30,31', '/role/saveOrUpdateRole', 'own', 1, 2, 1, 0, 0, '2022-07-11 18:39:03.192', '2022-07-11 18:39:03.194');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (32, '角色编辑', 'RoleEdit', 3, 30, 24, '24,30,32', '/role/saveOrUpdateRole', 'own', 2, 2, 1, 0, 0, '2022-07-11 18:39:03.196', '2022-07-11 18:39:03.197');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (33, '角色删除', 'RoleDelete', 3, 30, 24, '24,30,33', '/role/deleteSysRole', 'own', 3, 2, 1, 0, 0, '2022-07-11 18:39:03.198', '2022-07-11 18:39:03.199');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (34, '菜单管理', 'MenuManage', 2, 24, 24, '24,34', '/auth/getAuthTree', 'own', 3, 1, 1, 0, 0, '2022-07-11 18:39:03.200', '2022-07-11 18:39:03.202');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (35, '菜单新增', 'MenuAdd', 3, 34, 24, '24,34,35', '/auth/createAuthNode', 'own', 1, 2, 1, 0, 0, '2022-07-11 18:39:03.202', '2022-07-11 18:39:03.204');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (36, '菜单编辑', 'MenuEdit', 3, 34, 24, '24,34,36', '/auth/alterAuthNodeStatus', 'own', 2, 2, 1, 0, 0, '2022-07-11 18:39:03.204', '2022-07-11 18:39:03.206');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (37, '菜单编辑', 'MenuDelete', 3, 34, 24, '24,34,37', '/auth/deleteAuthNode', 'own', 3, 2, 1, 0, 0, '2022-07-11 18:39:03.207', '2022-07-11 18:39:03.208');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (38, '中心管理', 'CenterManage', 2, 24, 24, '24,38', '', 'own', 4, 1, 1, 0, 0, '2022-07-11 18:39:03.209', '2022-07-11 18:39:03.209');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (39, '编辑机构信息', 'OrganChange', 3, 38, 24, '24,38,39', '/organ/changeLocalOrganInfo', 'own', 1, 2, 1, 0, 0, '2022-07-11 18:39:03.210', '2022-07-11 18:39:03.211');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (40, '添加中心节点', 'FusionAdd', 3, 38, 24, '24,38,40', '/fusion/registerConnection', 'own', 2, 2, 1, 0, 0, '2022-07-11 18:39:03.212', '2022-07-11 18:39:03.214');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (41, '删除中心节点', 'FusionDelete', 3, 38, 24, '24,38,41', '/fusion/deleteConnection', 'own', 3, 2, 1, 0, 0, '2022-07-11 18:39:03.215', '2022-07-11 18:39:03.216');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (42, '创建群组', 'GroupCreate', 3, 38, 24, '24,38,42', '/fusion/createGroup', 'own', 4, 2, 1, 0, 0, '2022-07-11 18:39:03.217', '2022-07-11 18:39:03.218');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (43, '加入群组', 'GroupJoin', 3, 38, 24, '24,38,43', '/fusion/joinGroup', 'own', 5, 2, 1, 0, 0, '2022-07-11 18:39:03.220', '2022-07-11 18:39:03.222');
INSERT INTO `sys_auth` (`auth_id`, `auth_name`, `auth_code`, `auth_type`, `p_auth_id`, `r_auth_id`, `full_path`, `auth_url`, `data_auth_code`, `auth_index`, `auth_depth`, `is_show`, `is_editable`, `is_del`, `c_time`, `u_time`) VALUES (44, '退出群组', 'GroupExit', 3, 38, 24, '24,38,44', '/fusion/exitGroup', 'own', 6, 2, 1, 0, 0, '2022-07-11 18:39:03.223', '2022-07-11 18:39:03.224');


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
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (1, 1, 1, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (2, 1, 2, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (3, 1, 3, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (4, 1, 4, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (5, 1, 5, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (6, 1, 6, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (7, 1, 7, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (8, 1, 8, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (9, 1, 9, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (10, 1, 10, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (11, 1, 11, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (12, 1, 12, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (13, 1, 13, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (14, 1, 14, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (15, 1, 15, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (16, 1, 16, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (17, 1, 17, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (18, 1, 18, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (19, 1, 19, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (20, 1, 20, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (21, 1, 21, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (22, 1, 22, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (23, 1, 23, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (24, 1, 24, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (25, 1, 25, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (26, 1, 26, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (27, 1, 27, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (28, 1, 28, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (29, 1, 29, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (30, 1, 30, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (31, 1, 31, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (32, 1, 32, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (33, 1, 33, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (34, 1, 34, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (35, 1, 35, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (36, 1, 36, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (37, 1, 37, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (38, 1, 38, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (39, 1, 39, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (40, 1, 40, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (41, 1, 41, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (42, 1, 42, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (43, 1, 43, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (44, 1, 44, 0, '2022-07-11 16:48:13.064', '2022-07-11 16:48:13.064');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (45, 1000, 1, 0, '2022-07-11 16:48:13.070', '2022-07-11 16:48:13.070');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (46, 1000, 2, 0, '2022-07-11 16:48:13.070', '2022-07-11 16:48:13.070');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (47, 1000, 3, 0, '2022-07-11 16:48:13.070', '2022-07-11 16:48:13.070');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (48, 1000, 4, 0, '2022-07-11 16:48:13.070', '2022-07-11 16:48:13.070');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (49, 1000, 5, 0, '2022-07-11 16:48:13.070', '2022-07-11 16:48:13.070');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (50, 1000, 6, 0, '2022-07-11 16:48:13.070', '2022-07-11 16:48:13.070');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (51, 1000, 7, 0, '2022-07-11 16:48:13.070', '2022-07-11 16:48:13.070');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (52, 1000, 8, 0, '2022-07-11 16:48:13.070', '2022-07-11 16:48:13.070');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (53, 1000, 9, 0, '2022-07-11 16:48:13.070', '2022-07-11 16:48:13.070');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (54, 1000, 10, 0, '2022-07-11 16:48:13.070', '2022-07-11 16:48:13.070');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (55, 1000, 11, 0, '2022-07-11 16:48:13.070', '2022-07-11 16:48:13.070');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (56, 1000, 12, 0, '2022-07-11 16:48:13.070', '2022-07-11 16:48:13.070');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (57, 1000, 13, 0, '2022-07-11 16:48:13.070', '2022-07-11 16:48:13.070');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (58, 1000, 14, 0, '2022-07-11 16:48:13.070', '2022-07-11 16:48:13.070');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (59, 1000, 15, 0, '2022-07-11 16:48:13.070', '2022-07-11 16:48:13.070');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (60, 1000, 16, 0, '2022-07-11 16:48:13.070', '2022-07-11 16:48:13.070');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (61, 1000, 17, 0, '2022-07-11 16:48:13.070', '2022-07-11 16:48:13.070');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (62, 1000, 18, 0, '2022-07-11 16:48:13.070', '2022-07-11 16:48:13.070');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (63, 1000, 19, 0, '2022-07-11 16:48:13.070', '2022-07-11 16:48:13.070');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (64, 1000, 20, 0, '2022-07-11 16:48:13.070', '2022-07-11 16:48:13.070');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (65, 1000, 21, 0, '2022-07-11 16:48:13.070', '2022-07-11 16:48:13.070');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (66, 1000, 22, 0, '2022-07-11 16:48:13.070', '2022-07-11 16:48:13.070');
INSERT INTO `sys_ra` (`id`, `role_id`, `auth_id`, `is_del`, `c_time`, `u_time`) VALUES (67, 1000, 23, 0, '2022-07-11 16:48:13.070', '2022-07-11 16:48:13.070');


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
                             `register_type` tinyint(4) NOT NULL COMMENT '注册类型1：管理员创建 2：邮箱 3：手机',
                             PRIMARY KEY (`user_id`) USING BTREE,
                             UNIQUE INDEX `ix_unique_user_account`(`user_account`) USING BTREE COMMENT '账户名称唯一索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1000 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', 'a0f34ffac5a82245e4fca2e21f358a42', 'admin', '1', 0, 1, 0, '2022-03-25 17:55:53.048', '2022-07-18 17:13:02.377', 1);

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