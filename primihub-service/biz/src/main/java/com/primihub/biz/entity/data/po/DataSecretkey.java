package com.primihub.biz.entity.data.po;

import lombok.Data;

import java.util.Date;

/**
 * <p>
 * 协作秘钥表
 * </p>
 *
 * @author 
 * @since 2022-06-22
 */
@Data
public class DataSecretkey {

    /**
     * 自增ID
     */
    private Long id;

    /**
     * 秘钥ID UUID
     */
    private String secretkeyId;

    /**
     * 公开秘钥
     */
    private String publicKey;

    /**
     * 私有秘钥
     */
    private String privateKey;

    /**
     * 是否删除
     */
    private Integer isDel;

    /**
     * 创建时间
     */
    private Date createDate;

    /**
     * 修改时间
     */
    private Date updateDate;


}
