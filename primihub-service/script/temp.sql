ALTER TABLE `privacy`.`sys_user`
    ADD COLUMN `register_type` tinyint(4) NOT NULL COMMENT '注册类型1：管理员创建 2：邮箱 3：手机' AFTER `u_time`;