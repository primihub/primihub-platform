package com.primihub.biz.service.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.config.base.OrganConfiguration;
import com.primihub.biz.config.mq.SingleTaskChannel;
import com.primihub.biz.entity.base.BaseFunctionHandleEntity;
import com.primihub.biz.entity.base.BaseFunctionHandleEnum;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.po.DataResource;
import com.primihub.biz.entity.sys.po.DataSet;
import com.primihub.biz.entity.sys.po.SysOrgan;
import com.primihub.biz.repository.primarydb.data.DataResourcePrRepository;
import com.primihub.biz.repository.secondarydb.data.DataResourceRepository;
import com.primihub.biz.repository.secondarydb.sys.SysOrganSecondarydbRepository;
import com.primihub.biz.service.data.DataResourceService;
import com.primihub.biz.service.data.OtherBusinessesService;
import com.primihub.biz.service.feign.FusionResourceService;
import com.primihub.biz.util.FileUtil;
import com.primihub.sdk.task.TaskHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
@Slf4j
public class TestService {
    @Autowired
    private DataResourceService dataResourceService;
    @Autowired
    private DataResourceRepository dataResourceRepository;
    @Autowired
    private DataResourcePrRepository dataResourcePrRepository;
    @Autowired
    private FusionResourceService fusionResourceService;
    @Autowired
    private SingleTaskChannel singleTaskChannel;
    @Autowired
    private SysOrganSecondarydbRepository sysOrganSecondarydbRepository;
    @Autowired
    private OtherBusinessesService otherBusinessesService;
    @Autowired
    private OrganConfiguration organConfiguration;
    @Autowired
    private TaskHelper taskHelper;


    public void formatResources(String tag) {
        tag = StringUtils.isBlank(tag)?"grpc":tag;
        List<DataResource> copyResourceList = dataResourceRepository.findCopyResourceList(0L, 5000L);
        for (DataResource dataResource : copyResourceList) {
            if (tag.contains("grpc")){
                dataResourceService.resourceSynGRPCDataSet(dataResource.getFileSuffix(),dataResource.getResourceFusionId(), dataResource.getUrl(),dataResourceRepository.queryDataFileFieldByFileId(dataResource.getResourceId()));
                fusionResourceService.saveResource(organConfiguration.getSysLocalOrganId(),dataResourceService.findCopyResourceList(dataResource.getResourceId(),dataResource.getResourceId()));
            }else if (tag.contains("copy")){
                if (StringUtils.isBlank(dataResource.getResourceHashCode())){
                    try {
                        File file = new File(dataResource.getUrl());
                        if (file.exists()){
                            dataResource.setResourceHashCode(FileUtil.md5HashCode(file));
                            dataResourcePrRepository.editResource(dataResource);
                        }
                    }catch (Exception e){
                        log.info("id:{}  ====  url:{}   错误：",dataResource.getResourceId(),dataResource.getUrl());
                        e.printStackTrace();
                    }
                }
            }
        }
        if (tag.contains("copy")){
            List<SysOrgan> sysOrgans = sysOrganSecondarydbRepository.selectSysOrganByExamine();
            for (SysOrgan sysOrgan : sysOrgans) {
                singleTaskChannel.input().send(MessageBuilder.withPayload(JSON.toJSONString(new BaseFunctionHandleEntity(BaseFunctionHandleEnum.BATCH_DATA_FUSION_RESOURCE_TASK.getHandleType(),sysOrgan.getOrganGateway()))).build());
            }
        }
    }

    public BaseResultEntity testDataSet(String id) {
        BaseResultEntity testDataSet = fusionResourceService.getTestDataSet(id);
        log.info(JSONObject.toJSONString(testDataSet));
        if (testDataSet.getCode().equals(BaseResultEnum.SUCCESS.getReturnCode())){
            List<SysOrgan> sysOrgans = sysOrganSecondarydbRepository.selectSysOrganByExamine();
            for (SysOrgan sysOrgan : sysOrgans) {
                otherBusinessesService.syncGatewayApiData(testDataSet.getResult(),sysOrgan.getOrganGateway()+"/share/shareData/batchSaveTestDataSet",null);
            }
        }
        return BaseResultEntity.success();
    }

    public BaseResultEntity batchSaveTestDataSet(List<DataSet> dataSets) {
        log.info(JSONObject.toJSONString(dataSets));
        return fusionResourceService.batchSaveTestDataSet(dataSets);
    }

    public BaseResultEntity killTask(String taskId) {
        return BaseResultEntity.success(taskHelper.killTask(taskId));
    }
}
