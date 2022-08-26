package com.primihub.biz.entity.data.vo;

import com.primihub.biz.entity.data.po.*;
import lombok.Data;

import java.util.List;

@Data
public class ShareModelVo {


    public ShareModelVo(String projectId, String serverAddress) {
        this.projectId = projectId;
        this.serverAddress = serverAddress;
    }
    public ShareModelVo(DataProject project) {
        this.projectId = project.getProjectId();
        this.serverAddress = project.getServerAddress();
    }
    public ShareModelVo(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public ShareModelVo() {
    }

    private String projectId;
    private String serverAddress;
    private DataModel dataModel;
    private DataTask dataTask;
    private DataModelTask dataModelTask;
    private List<DataModelResource> dmrList;
    private Long timestamp;
    private Integer nonce;
    private List<String> shareOrganId;

    public void init(DataProject project){
        this.projectId = project.getProjectId();
        this.serverAddress = project.getServerAddress();
    }

    public void supplement(){
        this.timestamp = System.currentTimeMillis();
        this.nonce = (int)Math.random()*100;
    }
}
