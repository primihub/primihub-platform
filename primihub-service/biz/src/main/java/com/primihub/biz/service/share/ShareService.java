package com.primihub.biz.service.share;

import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.service.feign.FusionOrganService;
import com.primihub.sdk.task.TaskHelper;
import com.primihub.sdk.task.param.TaskPSIParam;
import com.primihub.sdk.task.param.TaskParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class ShareService {

    @Autowired
    private TaskHelper taskHelper;
    @Autowired
    private FusionOrganService fusionOrganService;


    public Set<String> getServiceState(){
        Set<String> service = new HashSet<>();
        service.add("platform");
        try {
            TaskParam taskParam = new TaskParam(new TaskPSIParam());
            taskParam.setTaskId("0");
            taskParam.setJobId("0");
            taskParam.setRequestId("0");
            taskParam.setPartyCount(0);
            taskHelper.continuouslyObtainTaskStatus(taskParam);
            if (taskParam.getSuccess()){
                service.add("node");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            if(fusionOrganService.healthConnection().getCode().equals(BaseResultEnum.SUCCESS.getReturnCode())){
                service.add("fusion");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return service;
    }
}
