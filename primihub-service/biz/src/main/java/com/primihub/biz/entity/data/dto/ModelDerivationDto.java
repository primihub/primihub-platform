package com.primihub.biz.entity.data.dto;

import lombok.Data;
import lombok.Getter;

@Data
public class ModelDerivationDto {
    /**
     * 当前资源Id,可能是普通资源，也可能是衍生资源
     */
    private String resourceId;
    private String type;
    private String derivationType;
    private String newResourceId;
    private String path;
    /**
     * 衍生资源的原始资源，注意如果一个资源的多个递归衍生资源是同一个原始资源
     */
    private String originalResourceId;

    public ModelDerivationDto(String resourceId, String type, String derivationType, String newResourceId) {
        this.resourceId = resourceId;
        this.derivationType = derivationType;
        this.type = type;
        this.newResourceId = newResourceId;
    }

    public ModelDerivationDto(String resourceId, String type, String derivationType, String newResourceId, String path, String originalResourceId) {
        this.resourceId = resourceId;
        this.derivationType = derivationType;
        this.type = type;
        this.newResourceId = newResourceId;
        this.path = path;
        this.originalResourceId = originalResourceId;
    }

    public ModelDerivationDto() {
    }


    @Getter
    public enum ModelDerivationTypeEnum {
        EXCEPTION("exception", "异常值处理"),
        DATA_ALIGN("dataAlign", "数据对齐"),
        FIT_TRANS_FORM("fitTransform", "缺失值填充"),
        ;
        private final String code;
        private final String desc;

        ModelDerivationTypeEnum(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }
}
