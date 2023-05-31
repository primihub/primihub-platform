package com.primihub.biz.entity.data.vo;

import com.primihub.biz.entity.data.po.DataProject;
import com.primihub.biz.entity.data.po.DataProjectOrgan;
import com.primihub.biz.entity.data.po.DataProjectResource;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ShareProjectVo {

    public ShareProjectVo() {
    }

    public ShareProjectVo(String projectId) {
        this.projectId = projectId;
    }

    public ShareProjectVo(DataProject project) {
        this.projectId = project.getProjectId();
        this.project = project;
    }

    private String projectId;
    private DataProject project;
    private List<DataProjectOrgan> projectOrgans = new ArrayList<>();
    private List<DataProjectResource> projectResources = new ArrayList<>();
    private Long timestamp;
    private Integer nonce;

    public void supplement(){
        this.timestamp = System.currentTimeMillis();
        this.nonce = (int)Math.random()*100;
    }
}
