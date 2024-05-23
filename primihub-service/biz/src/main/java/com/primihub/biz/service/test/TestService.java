package com.primihub.biz.service.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.config.base.OrganConfiguration;
import com.primihub.biz.config.mq.SingleTaskChannel;
import com.primihub.biz.constant.SysConstant;
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
import com.primihub.sdk.config.GrpcProxyConfig;
import com.primihub.sdk.config.ProxyConfig;
import com.primihub.sdk.task.TaskHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.LinkedHashMap;
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
    @Autowired
    private BaseConfiguration baseConfiguration;


    public void formatResources(String tag) {
        tag = StringUtils.isBlank(tag)?"grpc":tag;
        List<DataResource> copyResourceList = dataResourceRepository.findCopyResourceList(0L, 5000L);
        for (DataResource dataResource : copyResourceList) {
            if (tag.contains("grpc")){
                dataResourceService.resourceSynGRPCDataSet(dataResource.getFileSuffix(),dataResource.getResourceFusionId(), dataResource.getUrl(),dataResourceRepository.queryDataFileFieldByResourceId(dataResource.getResourceId()));
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

    // 出代理
    public BaseResultEntity testDataSet(String id) {
        BaseResultEntity testDataSet = fusionResourceService.getTestDataSet(id);
        log.info(JSONObject.toJSONString(testDataSet));
        if (testDataSet.getCode().equals(BaseResultEnum.SUCCESS.getReturnCode())){
            List<LinkedHashMap> result = (List<LinkedHashMap>) testDataSet.getResult();
            if (nodeHasGRPCOutProxy()) {
                for (LinkedHashMap map : result) {
                    String oldAddress = (String) map.getOrDefault("address", StringUtils.EMPTY);
                    log.info("[address][before] --- [{}]", oldAddress);
                    String[] split = oldAddress.split(SysConstant.COLON_DELIMITER);
                    if (split.length < 3) {
                        continue;
                    }
                    ProxyConfig outProxy = baseConfiguration.getGrpcProxy().getOutProxy();
                    String oldHost = split[1];
                    String oldPort = split[2];
                    String replace1 = oldAddress.replace(oldHost, outProxy.getAddress());
                    String replace2 = replace1.replace(oldPort, String.valueOf(outProxy.getPort()));
                    log.info("[address][after] --- [{}]", replace2);
                    map.put("address", replace2);
                }
            }
            List<SysOrgan> sysOrgans = sysOrganSecondarydbRepository.selectSysOrganByExamine();
            for (SysOrgan sysOrgan : sysOrgans) {
                otherBusinessesService.syncGatewayApiData(result,sysOrgan.getOrganGateway()+"/share/shareData/batchSaveTestDataSet",null);
            }
        }
        return BaseResultEntity.success();
    }

    private boolean nodeHasGRPCOutProxy() {
        GrpcProxyConfig grpcProxy = baseConfiguration.getGrpcProxy();
        if (grpcProxy == null) {
            return false;
        }
        ProxyConfig outProxy = grpcProxy.getOutProxy();
        if (outProxy == null) {
            return false;
        }
        if (StringUtils.isBlank(outProxy.getAddress()) || outProxy.getPort() == null) {
            return false;
        }
        return true;
    }

    public BaseResultEntity batchSaveTestDataSet(List<DataSet> dataSets) {
        log.info(JSONObject.toJSONString(dataSets));
        if (nodeHasGRPCInProxy()) {
            for (DataSet dataSet : dataSets) {
                String oldAddress = dataSet.getAddress();
                String[] split = oldAddress.split(SysConstant.COLON_DELIMITER);
                if (split.length < 3) {
                    continue;
                }
                ProxyConfig outProxy = baseConfiguration.getGrpcProxy().getInProxy();
                String oldHost = split[1];
                String oldPort = split[2];
                String replace1 = oldAddress.replace(oldHost, outProxy.getAddress());
                String replace2 = replace1.replace(oldPort, String.valueOf(outProxy.getPort()));
                dataSet.setAddress(replace2);
            }
        }
        return fusionResourceService.batchSaveTestDataSet(dataSets);
    }

    private boolean nodeHasGRPCInProxy() {
        GrpcProxyConfig grpcProxy = baseConfiguration.getGrpcProxy();
        if (grpcProxy == null) {
            return false;
        }
        ProxyConfig outProxy = grpcProxy.getInProxy();
        if (outProxy == null) {
            return false;
        }
        if (StringUtils.isBlank(outProxy.getAddress()) || outProxy.getPort() == null) {
            return false;
        }
        return true;
    }

    public BaseResultEntity killTask(String taskId) {
        return BaseResultEntity.success(taskHelper.killTask(taskId));
    }
}
