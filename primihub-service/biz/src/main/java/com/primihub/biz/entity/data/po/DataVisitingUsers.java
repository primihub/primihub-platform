package com.primihub.biz.entity.data.po;

import lombok.Data;

import java.util.Date;

@Data
public class DataVisitingUsers {

    /**
     * 来访ID
     */
    private Long id;

    /**
     * 从业者
     */
    private Integer familiarityPractitioner;

    /**
     * 已在应用
     */
    private Integer familiarityAlreadyinuse;

    /**
     * 非常熟悉
     */
    private Integer familiarityVeryfamiliar;

    /**
     * 一般熟悉
     */
    private Integer familiarityGeneralfamiliar;

    /**
     * 完全不懂
     */
    private Integer familiarityNotknow;

    /**
     * 男
     */
    private Integer genderMale;

    /**
     * 女
     */
    private Integer genderFemale;

    /**
     * 北京
     */
    private Integer cityBeijing;

    /**
     * 上海
     */
    private Integer cityShanghai;

    /**
     * 深圳
     */
    private Integer cityShenzhen;

    /**
     * 杭州
     */
    private Integer cityHangzhou;

    /**
     * 长沙
     */
    private Integer cityChangsha;

    /**
     * 互联网
     */
    private Integer industryInternet;

    /**
     * 金融
     */
    private Integer industryFinancial;

    /**
     * 政府
     */
    private Integer industryGovernment;

    /**
     * 医疗
     */
    private Integer industryMedical;

    /**
     * 工业
     */
    private Integer industryIndustrial;

    /**
     * 汽车
     */
    private Integer industryCar;

    /**
     * 新能源
     */
    private Integer industryNewenergy;

    /**
     * 其他
     */
    private Integer industryOther;

    /**
     * 商业合作
     */
    private Integer visitpurposesCooperation;

    /**
     * 学习
     */
    private Integer visitpurposesLearning;

    /**
     * 试用
     */
    private Integer visitpurposesTrial;

    /**
     * 随便看看
     */
    private Integer visitpurposesBrowse;

    /**
     * 年龄
     */
    private Integer ageAge;

    /**
     * 管理者
     */
    private Integer jobpositionManager;

    /**
     * 产品
     */
    private Integer jobpositionPm;

    /**
     * 技术
     */
    private Integer jobpositionDeveloper;

    /**
     * 商务
     */
    private Integer jobpositionCommerceaffairs;

    /**
     * 解决方案
     */
    private Integer jobpositionSolution;

    /**
     * 其他
     */
    private Integer jobpositionOther;

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