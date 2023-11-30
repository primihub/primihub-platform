package com.primihub.application.controller.share;


import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.dto.DataFusionCopyDto;
import com.primihub.biz.entity.data.vo.ShareModelVo;
import com.primihub.biz.entity.data.vo.ShareProjectVo;
import com.primihub.biz.entity.sys.po.DataSet;
import com.primihub.biz.entity.sys.po.SysLocalOrganInfo;
import com.primihub.biz.service.data.DataModelService;
import com.primihub.biz.service.data.DataProjectService;
import com.primihub.biz.service.data.DataResourceService;
import com.primihub.biz.service.share.ShareService;
import com.primihub.biz.service.sys.SysOrganService;
import com.primihub.biz.service.test.TestService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Api(value = "数据同步接口 - 多节点可用,单节点不可用",tags = "数据同步接口")
@RequestMapping("shareData")
@RestController
@Slf4j
public class ShareDataController {

    @Autowired
    private DataProjectService dataProjectService;
    @Autowired
    private DataModelService dataModelService;
    @Autowired
    private SysOrganService sysOrganService;
    @Autowired
    private DataResourceService dataResourceService;
    @Autowired
    private TestService testService;
    @Autowired
    private ShareService shareService;

    @PostMapping("/healthConnection")
    public BaseResultEntity healthConnection(@RequestBody Object time){
        log.info("healthConnection - {}",time);
        return BaseResultEntity.success(shareService.getServiceState());
    }

    /**
     * 创建编辑项目接口
     * @return
     */
    @PostMapping("syncProject")
    public BaseResultEntity syncProject(@RequestBody ShareProjectVo vo){
        return dataProjectService.syncProject(vo);
    }

    /**
     * 创建编辑项目接口
     * @return
     */
    @PostMapping("syncModel")
    public BaseResultEntity syncModel(@RequestBody ShareModelVo vo){
        return dataModelService.syncModel(vo);
    }

    @PostMapping("apply")
    public BaseResultEntity applyForJoinNode(@RequestBody Map<String,Object> info){
        if (info==null){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"info");
        }
        if (!info.containsKey("applyId")){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"applyId");
        }
        if (!info.containsKey("organId")){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"organId");
        }
        if (!info.containsKey("organName")){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"organName");
        }
        if (!info.containsKey("gateway")){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"gateway");
        }
        if (!info.containsKey("publicKey")){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"publicKey");
        }
        return sysOrganService.applyForJoinNode(info);
    }

    @PostMapping("saveFusionResource")
    public BaseResultEntity saveFusionResource(@RequestBody DataFusionCopyDto dto){
        if (dto==null){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"dto null");
        }
        if (StringUtils.isEmpty(dto.getOrganId())){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"organId");
        }
        if (StringUtils.isEmpty(dto.getCopyPart())){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"CopyPart");
        }
        return dataResourceService.saveFusionResource(dto);
    }

    @PostMapping("batchSaveTestDataSet")
    public BaseResultEntity batchSaveTestDataSet(@RequestBody List<DataSet> dataSets){
        if (dataSets == null || dataSets.size()==0) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"shareData - dataSets");
        }
        return testService.batchSaveTestDataSet(dataSets);
    }



}
