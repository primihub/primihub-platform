-- 2022-10-15
ALTER TABLE `fusion`.`fusion_resource`
    ADD COLUMN `resource_hash_code` varchar(255) NULL COMMENT '资源hash值';
ALTER TABLE `fusion`.`fusion_resource`
    ADD COLUMN `resource_state` tinyint(4) NOT NULL DEFAULT '0' COMMENT '资源状态 0上线 1下线';