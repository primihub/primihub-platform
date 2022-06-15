package com.primihub.biz.entity.data.vo;


import com.primihub.biz.entity.data.po.DataFileField;
import lombok.Data;

import java.util.List;

@Data
public class DataRunScript {

    public DataRunScript(Long resourceId, String resourceName) {
        this.resourceId = resourceId;
        this.resourceName = resourceName;
    }

    public DataRunScript(Long resourceId, String resourceName, List<DataFileField> dataFileFields) {
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.dataFileFields = dataFileFields;
    }

    public DataRunScript() {
    }

    public DataRunScript(Long resourceId, String resourceName, Long fileId, String fileName, List<DataFileField> dataFileFields) {
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.fileId = fileId;
        this.fileName = fileName;
        this.dataFileFields = dataFileFields;
    }

    /**
     * 资源id
     */
    private Long resourceId;
    /**
     * 资源名称
     */
    private String resourceName;

    private Long fileId;

    private String fileName;

    private List<DataFileField> dataFileFields;
}
