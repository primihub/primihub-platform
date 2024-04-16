package com.primihub.biz.entity.data.req;

import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.entity.data.dto.ModelDerivationDto;
import com.primihub.biz.entity.data.po.*;
import com.primihub.biz.entity.data.vo.ModelProjectResourceVo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.util.*;

@Data
@Slf4j
public class ComponentTaskReq {
    private DataModel dataModel;
    private DataModelTask dataModelTask;
    // start
    private DataTask dataTask;
    /**
     * DataModelResource 指模型数据，其中包括 任务的原始数据和模型任务产生的衍生数据
     */
    private List<DataModelResource> dmrList = new ArrayList<>();
    private List<ModelProjectResourceVo> resourceList;
    /**
     * freemarkerMap 存放 数据集标签和资源id
     */
    private Map<String, Object> freemarkerMap = new HashMap<>();
    private DataModelAndComponentReq modelComponentReq = null;
    private List<DataModelComponent> dataModelComponents = new ArrayList<>();
    // 每个组件都会放进来
    private List<DataComponent> dataComponents = new ArrayList<>();
    // 每个组件中的k-v参数都会放进来
    private Map<String, String> valueMap = new HashMap<>();
    private List<LinkedHashMap<String, Object>> fusionResourceList;
    private List<ModelDerivationDto> derivationList = new ArrayList<>();
    private List<ModelDerivationDto> newest;
    private int job = 0;

    // 源数据id Map
    private Map<String, String> originResourceIdMap = new HashMap<>();

    public ComponentTaskReq(DataModel dataModel) {
        this.dataModel = dataModel;
        this.dataTask = new DataTask();
        this.modelComponentReq = this.parseModelComponentReq();
    }

    private DataModelAndComponentReq parseModelComponentReq() {
        if (StringUtils.isNotBlank(dataModel.getComponentJson())) {
            return JSONObject.parseObject(dataModel.getComponentJson(), DataModelAndComponentReq.class);
        } else {
            return null;
        }
    }

    public int getJob() {
        job++;
        return job;
    }
}
