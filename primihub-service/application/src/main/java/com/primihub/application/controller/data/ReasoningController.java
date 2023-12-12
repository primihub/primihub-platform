package com.primihub.application.controller.data;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.req.DataReasoningReq;
import com.primihub.biz.entity.data.req.PageReq;
import com.primihub.biz.entity.data.req.ReasoningListReq;
import com.primihub.biz.service.data.DataReasoningService;
import io.swagger.annotations.Api;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@Api(value = "推理接口",tags = "推理接口")
@RequestMapping("reasoning")
@RestController
public class ReasoningController {

    @Autowired
    private DataReasoningService dataReasoningService;

    /**
     * 获取模型列表
     * @return
     */
    @GetMapping("getReasoningList")
    public BaseResultEntity getReasoningList(ReasoningListReq req,
                                             @RequestHeader("userId") Long userId){
        if (userId==null || userId==0L) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"userId");
        }
        req.setUserId(userId);
        return dataReasoningService.getReasoningList(req);
    }
    @PostMapping("saveReasoning")
    public BaseResultEntity saveReasoning(DataReasoningReq req,
                                          @RequestHeader("userId") Long userId){
        if (userId==null || userId==0L) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"userId");
        }
        if (req==null) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"dataReasoning");
        }
        if (req.getTaskId()==null || req.getTaskId()==0L) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"taskId");
        }
        if (StringUtils.isBlank(req.getReasoningName())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"reasoningName");
        }
        if (StringUtils.isBlank(req.getReasoningDesc())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"reasoningDesc");
        }
        if (req.getResourceList() == null || req.getResourceList().isEmpty()) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceList");
        }
        req.setUserId(userId);
        return dataReasoningService.saveReasoning(req);
    }

    @GetMapping("getReasoning")
    public BaseResultEntity getReasoning(Long id){
        if (id==null || id==0L) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"id");
        }
        return dataReasoningService.getReasoning(id);
    }
}
