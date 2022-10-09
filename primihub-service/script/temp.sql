ALTER TABLE `privacy`.`sys_user`
    ADD COLUMN `register_type` tinyint(4) NOT NULL COMMENT '注册类型1：管理员创建 2：邮箱 3：手机' AFTER `u_time`;
-- 2022-10-15
ALTER TABLE `privacy`.`data_resource`
    ADD COLUMN `resource_hash_code` varchar(255) NULL COMMENT '资源hash值';
ALTER TABLE `privacy`.`data_resource`
    ADD COLUMN `resource_state` tinyint(4) NOT NULL DEFAULT '0' COMMENT '资源状态 0上线 1下线';
