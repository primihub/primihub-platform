package com.primihub.biz.entity.sys.po;
import lombok.Data;

import java.util.Date;

/**
 * 文件列表
 */
@Data
public class SysFile {
    /**
     * 文件id
     */
    private Long fileId;
    /**
     * 文件来源
     */
    private Integer fileSource;
    /**
     * 文件地址
     */
    private String fileUrl;
    /**
     * 文件名称
     */
    private String fileName;
    /**
     * 文件后缀
     */
    private String fileSuffix;
    /**
     * 文件实际大小
     */
    private Long fileSize;
    /**
     * 文件当前大小
     */
    private Long fileCurrentSize;
    /**
     * 文件区域
     */
    private String fileArea;
    /**
     * 是否删除
     */
    private Integer isDel;
    /**
     * 创建时间
     */
    private Date cTime;
    /**
     * 修改时间
     */
    private Date uTime;
}
