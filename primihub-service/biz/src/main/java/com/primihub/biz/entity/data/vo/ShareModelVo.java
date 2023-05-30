package com.primihub.biz.entity.data.vo;

import com.primihub.biz.entity.data.dto.ModelDerivationDto;
import com.primihub.biz.entity.data.po.*;
import com.primihub.biz.service.data.component.impl.ExceptionComponentTaskServiceImpl;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ShareModelVo {


    public ShareModelVo(DataProject project) {
        this.projectId = project.getProjectId();
    }

    public ShareModelVo() {
    }

    private String projectId;
    private DataModel dataModel;
    private DataTask dataTask;
    private DataModelTask dataModelTask;
    private List<DataModelResource> dmrList;
    private Long timestamp;
    private Integer nonce;
    private List<String> shareOrganId;
    private List<ModelDerivationDto> derivationList;

    public void init(DataProject project){
        this.projectId = project.getProjectId();
    }

    public void supplement(){
        this.timestamp = System.currentTimeMillis();
        this.nonce = (int)Math.random()*100;
    }
}
