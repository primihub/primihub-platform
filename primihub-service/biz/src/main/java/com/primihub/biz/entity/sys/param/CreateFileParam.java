package com.primihub.biz.entity.sys.param;

import lombok.Data;

@Data
public class CreateFileParam {
    private Integer fileSource;
    private String fileSuffix;
    private Long fileSize;
}
