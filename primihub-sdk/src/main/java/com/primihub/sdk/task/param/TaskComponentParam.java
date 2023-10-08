package com.primihub.sdk.task.param;

import com.primihub.sdk.task.annotation.TaskTypeExample;
import com.primihub.sdk.task.dataenum.ModelTypeEnum;
import com.primihub.sdk.task.factory.AbstractComponentGRPCExecute;

import java.util.Map;


@TaskTypeExample(AbstractComponentGRPCExecute.class)
public class TaskComponentParam{
    private ModelTypeEnum modelType;
    /**
     * label_dataset、guest_dataset、arbiter_dataset 必传
     */
    private Map<String, Object> freemarkerMap;
    /**
     * 运行模板内容可以为填充前模板内容、也可以是填充后 untreated属性控制
     */
    private String templatesContent;

    private boolean untreated = true;

    private boolean infer = false;

    private boolean fitTransform = false;

    public ModelTypeEnum getModelType() {
        return modelType;
    }

    public void setModelType(ModelTypeEnum modelType) {
        this.modelType = modelType;
    }

    public Map<String, Object> getFreemarkerMap() {
        return freemarkerMap;
    }

    public void setFreemarkerMap(Map<String, Object> freemarkerMap) {
        this.freemarkerMap = freemarkerMap;
    }

    public String getTemplatesContent() {
        return templatesContent;
    }

    public void setTemplatesContent(String templatesContent) {
        this.templatesContent = templatesContent;
    }

    public boolean isUntreated() {
        return untreated;
    }

    public void setUntreated(boolean untreated) {
        this.untreated = untreated;
    }

    public boolean isInfer() {
        return infer;
    }

    public void setInfer(boolean infer) {
        this.infer = infer;
    }

    public boolean isFitTransform() {
        return fitTransform;
    }

    public void setFitTransform(boolean fitTransform) {
        this.fitTransform = fitTransform;
    }

    @Override
    public String toString() {
        return "ComponentTaskParam{" +
                "modelType=" + modelType +
                ", freemarkerMap=" + freemarkerMap +
                ", templatesContent='" + templatesContent + '\'' +
                ", untreated=" + untreated +
                ", infer=" + infer +
                '}';
    }
}
