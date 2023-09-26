package com.primihub.biz.entity.data.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DataPsiResourceAllocationVo {
    /**
     * 资源id
     */
    private String resourceId;
    /**
     * 资源名称
     */
    private String resourceName;
    /**
     * 机构id
     */
    private String organId;

    private List<DataResourceFieldCopyVo> keywordList = new ArrayList<>();
}
