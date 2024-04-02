package com.primihub.biz.service.data;

import com.alibaba.fastjson.JSON;
import com.primihub.biz.constant.DataConstant;
import com.primihub.biz.entity.sys.po.SysOrgan;
import com.primihub.biz.repository.secondarydb.data.DataCopySecondarydbRepository;
import com.primihub.biz.config.base.OrganConfiguration;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.dataenum.DataFusionCopyEnum;
import com.primihub.biz.entity.data.dto.DataFusionCopyDto;
import com.primihub.biz.entity.data.po.DataFusionCopyTask;
import com.primihub.biz.entity.sys.po.SysLocalOrganInfo;
import com.primihub.biz.repository.primarydb.data.DataCopyPrimarydbRepository;
import com.primihub.biz.repository.secondarydb.sys.SysOrganSecondarydbRepository;
import com.primihub.biz.service.feign.FusionResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DataCopyService implements ApplicationContextAware {

    private static ApplicationContext context;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context=applicationContext;
    }
    @Resource(name="soaRestTemplate")
    private RestTemplate restTemplate;
    @Autowired
    private OrganConfiguration organConfiguration;
    @Autowired
    private DataCopySecondarydbRepository dataCopySecondarydbRepository;
    @Autowired
    private DataCopyPrimarydbRepository dataCopyPrimarydbRepository;
    @Autowired
    private FusionResourceService fusionResourceService;
    @Autowired
    private OtherBusinessesService otherBusinessesService;
    @Autowired
    private SysOrganSecondarydbRepository sysOrganSecondarydbRepository;

    public List<DataFusionCopyTask> selectNotFinishedTask(Date threeDayAgo, Date tenMinuteAgo){
        return dataCopySecondarydbRepository.selectNotFinishedTask(threeDayAgo,tenMinuteAgo);
    }

    public void handleFusionCopyTask(DataFusionCopyTask task){
        DataFusionCopyEnum enu=DataFusionCopyEnum.FUSION_COPY_MAP.get(task.getTaskTable());
        Long startOffset,endOffset;
        if (task.getTaskType() == 1) {
            startOffset=task.getCurrentOffset();
            endOffset=Math.min(startOffset+ DataConstant.COPY_PAGE_NUM-1,task.getTargetOffset());
        } else if (task.getTaskType() == 2) {
            startOffset=task.getCurrentOffset();
            endOffset=task.getTargetOffset();
        } else {
            return;
        }
        if(enu!=null) {
            int errorCount=0;
            SysLocalOrganInfo sysLocalOrganInfo = organConfiguration.getSysLocalOrganInfo();
            if (sysLocalOrganInfo==null||sysLocalOrganInfo.getOrganId()==null|| "".equals(sysLocalOrganInfo.getOrganId().trim())) {
                return;
            }
            while(startOffset<endOffset) {
                log.info(startOffset+"-"+endOffset);
                DataFusionCopyDto copyDto = new DataFusionCopyDto();
                copyDto.setTableName(task.getTaskTable());
                copyDto.setMaxOffset(endOffset);
                copyDto.setOrganId(organConfiguration.getSysLocalOrganId());
                Object object = context.getBean(enu.getBeanName());
                Class<? extends Object> clazz = object.getClass();
                try {
                    Method method = clazz.getMethod(enu.getFunctionName(), Long.class,Long.class);
                    Object result = method.invoke(object,task.getTaskType()==1?startOffset:endOffset,endOffset);
                    copyDto.setCopyPart(JSON.toJSONString(result));
//                    log.info(copyDto.getCopyPart());
                } catch (Exception e) {
                    e.printStackTrace();
                    log.info("handleFusionCopyTask", e);
                    return ;
                }
                boolean isSuccess=true;
                String errorMsg="";
                if(!"[]".equals(copyDto.getCopyPart())) {
                    try {
                        SysOrgan sysOrgan = sysOrganSecondarydbRepository.selectSysOrganByOrganId(task.getOrganId());
                        if (sysOrgan ==null){
                            isSuccess = false;
                            errorMsg = "机构信息查询null";
                        }else {
                            BaseResultEntity resultEntity = otherBusinessesService.syncGatewayApiData(copyDto, task.getServerAddress() + "/share/shareData/saveFusionResource", sysOrgan.getPublicKey());
                            log.info("本次查找备份结果：{}-{}, 其中与其他机构同步结果： {}", startOffset, endOffset, JSON.toJSONString(resultEntity));
                            if (!resultEntity.getCode().equals(BaseResultEnum.SUCCESS.getReturnCode())) {
                                isSuccess = false;
                                if (++errorCount >= 3) {
                                    errorMsg = resultEntity.getMsg().substring(0, Math.min(1000, resultEntity.getMsg().length()));
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        isSuccess = false;
                        if (++errorCount >= 3) {
                            errorMsg = e.getMessage().substring(0, Math.min(1000, e.getMessage().length()));
                        }
                    }
                }
                if(isSuccess) {
                    log.info("传输成功一样：执行修改任务：taskId: {}, endOffset: {}", task.getId(), endOffset);
                    dataCopyPrimarydbRepository.updateCopyInfo(task.getId(),endOffset,"success");
                    if (task.getTaskType() == 1) {
                        startOffset=startOffset+DataConstant.COPY_PAGE_NUM;
                        endOffset=Math.min(endOffset+DataConstant.COPY_PAGE_NUM,task.getTargetOffset());
                    }else{
                        startOffset=endOffset;
                    }
                    errorCount = 0;
                }else{
                    if(errorCount>=3){
                        dataCopyPrimarydbRepository.updateCopyInfo(task.getId(),null,errorMsg);
                        break;
                    }
                }
            }
        }
    }
}
