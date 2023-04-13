-- 2022-10-15
ALTER TABLE `fusion`.`fusion_resource`
    ADD COLUMN `resource_hash_code` varchar(255) NULL COMMENT '资源hash值';
ALTER TABLE `fusion`.`fusion_resource`
    ADD COLUMN `resource_state` tinyint(4) NOT NULL DEFAULT '0' COMMENT '资源状态 0上线 1下线';

-- 2023-03-30
ALTER TABLE `fusion`.`fusion_organ` ADD COLUMN `dispatch` tinyint(4) NOT NULL DEFAULT '0';
ALTER TABLE `fusion`.`fusion_organ` ADD COLUMN `public_key` varchar(1000) DEFAULT NULL COMMENT '机构公钥';
ALTER TABLE `fusion`.`fusion_organ` ADD COLUMN `private_key` varchar(1000) DEFAULT NULL COMMENT '机构私钥';