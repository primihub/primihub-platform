package com.primihub.biz.entity.data.req;

import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.entity.data.dto.ModelDerivationDto;
import com.primihub.biz.entity.data.po.*;
import com.primihub.biz.entity.data.vo.ModelProjectResourceVo;
import com.primihub.biz.service.data.component.impl.ExceptionComponentTaskServiceImpl;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.util.*;

@Data
@Slf4j
public class  ComponentTaskReq {
    private DataModel dataModel;
    private DataModelTask dataModelTask;
    private DataTask dataTask;
    private List<DataModelResource> dmrList = new ArrayList<>();
    private List<ModelProjectResourceVo> resourceList;
    private Map<String,Object> freemarkerMap = new HashMap<>();
    private DataModelAndComponentReq modelComponentReq = null;
    private List<DataModelComponent> dataModelComponents = new ArrayList<>();
    private List<DataComponent> dataComponents = new ArrayList<>();
    private Map<String, String> valueMap = new HashMap<>();
    private List<LinkedHashMap<String,Object>> fusionResourceList;
    private List<ModelDerivationDto> derivationList = new ArrayList<>();
    private List<ModelDerivationDto> newest;
    private int job = 0;

    public ComponentTaskReq(DataModel dataModel) {
        this.dataModel = dataModel;
        this.dataTask = new DataTask();
    }

    public DataModelAndComponentReq getModelComponentReq(){
        if (modelComponentReq!=null){
            return modelComponentReq;
        }
        try {
            if (StringUtils.isNotBlank(dataModel.getComponentJson())) {
                modelComponentReq  = JSONObject.parseObject(dataModel.getComponentJson(), DataModelAndComponentReq.class);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return modelComponentReq;
    }

    public int getJob() {
        job++;
        return job;
    }
}
