package com.primihub.biz.entity.data.vo;

import com.primihub.biz.entity.data.po.DataProject;
import com.primihub.biz.entity.data.po.DataProjectOrgan;
import com.primihub.biz.entity.data.po.DataProjectResource;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ShareProjectVo {
    private String projectId;
    private String serverAddress;
    private DataProject project;
    private List<DataProjectOrgan> projectOrgans = new ArrayList<>();
    private List<DataProjectResource> projectResources = new ArrayList<>();
    private Long timestamp;
    private Integer nonce;
}
