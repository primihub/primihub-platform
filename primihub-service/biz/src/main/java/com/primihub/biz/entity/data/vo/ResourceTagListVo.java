package com.primihub.biz.entity.data.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class ResourceTagListVo {
    /**
     * 标签id
     */
    private Long tagId;
    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 资源id
     */
    @JsonIgnore
    private Long resourceId;
}
