package com.primihub.biz.entity.data.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.primihub.biz.convert.DataResourceConvert;
import com.primihub.biz.entity.data.po.DataFileField;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class DataResourceCsvVo {
    private Long fileId;
    private List<DataFileFieldVo> fieldList;
    @JsonIgnore
    private List<DataFileField> fieldDataList;
    private List<LinkedHashMap<String,Object>> dataList = new ArrayList<>();

    public List<DataFileFieldVo> getFieldList() {
        return fieldDataList.stream().map(DataResourceConvert::DataFileFieldPoConvertVo).collect(Collectors.toList());
    }
}
