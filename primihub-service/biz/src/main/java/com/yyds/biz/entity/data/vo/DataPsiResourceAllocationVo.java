package com.yyds.biz.entity.data.vo;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class DataPsiResourceAllocationVo {
    /**
     * 资源id
     */
    private Long resourceId;
    /**
     * 资源名称
     */
    private String resourceName;
    /**
     * 机构id
     */
    private Long organId;

    private List<String> keywordList = new ArrayList();
}
