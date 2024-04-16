package com.primihub.biz.entity.data.req;

import lombok.Data;

@Data
public class DataComponentValue {
    private String key;
    private String val;


    public enum DataComponetValueKeyEnum {
        DATA_ALIGN("dataAlign", "数据对齐"),
        MULTIPLE_SELECTED("MultipleSelected", "")
        ;
        private final String code;
        private final String name;

        DataComponetValueKeyEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }
    }
}
