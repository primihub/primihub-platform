package com.primihub.entity.fusion.po;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author 
 * @since 2023-02-08
 */
@Data
public class FusionOrganExtends {

    /**
     * 主键
     */
    private Long id;

    /**
     * 机构id
     */
    private Long globalId;

    /**
     * 机构ip地址
     */
    private String ip;

    /**
     * 纬度
     */
    private BigDecimal lat;

    /**
     * 经度
     */
    private BigDecimal lon;

    /**
     * 区域
     */
    private String country;

    /**
     * 是否删除
     */
    private Integer isDel;

    /**
     * 创建时间
     */
    private Date cTime;

    /**
     * 更新时间
     */
    private Date uTime;


}
