package com.primihub.sdk.task.param;

import com.primihub.sdk.task.annotation.TaskTypeExample;
import com.primihub.sdk.task.dataenum.FieldTypeEnum;
import com.primihub.sdk.task.factory.AbstractDataSetGRPCExecute;
import java_data_service.DataTypeInfo;

import java.util.List;

@TaskTypeExample(AbstractDataSetGRPCExecute.class)
public class TaskDataSetParam {
    private String id;
    private String accessInfo;
    private String driver;
    private String visibility;
    private List<FieldType> fieldTypes;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccessInfo() {
        return accessInfo;
    }

    public void setAccessInfo(String accessInfo) {
        this.accessInfo = accessInfo;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public List<FieldType> getFieldTypes() {
        return fieldTypes;
    }

    public void setFieldTypes(List<FieldType> fieldTypes) {
        this.fieldTypes = fieldTypes;
    }

    @Override
    public String toString() {
        return "DataSetTaskParam{" +
                "id='" + id + '\'' +
                ", accessInfo='" + accessInfo + '\'' +
                ", driver='" + driver + '\'' +
                ", visibility='" + visibility + '\'' +
                ", fieldTypes=" + fieldTypes +
                '}';
    }
    public static class FieldType{
        private String name;
        private FieldTypeEnum fieldTypeEnum;
        private DataTypeInfo.PlainDataType plainDataType;

        public FieldType(String name, FieldTypeEnum fieldTypeEnum) {
            this.name = name;
            this.fieldTypeEnum = fieldTypeEnum;
        }
        public FieldType(String name, DataTypeInfo.PlainDataType plainDataType) {
            this.name = name;
            this.plainDataType = plainDataType;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public FieldTypeEnum getFieldTypeEnum() {
            return fieldTypeEnum;
        }

        public void setFieldTypeEnum(FieldTypeEnum fieldTypeEnum) {
            this.fieldTypeEnum = fieldTypeEnum;
        }

        public DataTypeInfo.PlainDataType getPlainDataType() {
            return plainDataType;
        }

        public void setPlainDataType(DataTypeInfo.PlainDataType plainDataType) {
            this.plainDataType = plainDataType;
        }
    }
}


