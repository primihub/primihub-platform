ALTER TABLE `privacy`.`sys_user`
    ADD COLUMN `register_type` tinyint(4) NOT NULL COMMENT '注册类型1：管理员创建 2：邮箱 3：手机' AFTER `u_time`;
-- 2022-10-15
ALTER TABLE `privacy`.`sys_user`
    ADD COLUMN `auth_uuid` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '第三方uuid' AFTER `u_time`;
ALTER TABLE `privacy`.`sys_user`
    ADD INDEX ix_index_auth_uuid (auth_uuid);
-- 2022-10-15
ALTER TABLE `privacy`.`data_resource`
    ADD COLUMN `resource_hash_code` varchar(255) NULL COMMENT '资源hash值';
ALTER TABLE `privacy`.`data_resource`
    ADD COLUMN `resource_state` tinyint(4) NOT NULL DEFAULT '0' COMMENT '资源状态 0上线 1下线';
-- 2022-11-30
ALTER TABLE `privacy`.`data_mr`
    ADD COLUMN `take_part_type` tinyint(4) NOT NULL DEFAULT '0' COMMENT '参与类型 0使用数据 1衍生数据';
