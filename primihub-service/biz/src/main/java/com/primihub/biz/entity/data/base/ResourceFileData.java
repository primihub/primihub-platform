package com.primihub.biz.entity.data.base;

import lombok.Data;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ResourceFileData {

    private String field;

    private List<String> fieldList;

    private Integer fieldSize = 0;

    private Integer fileContentSize = 0;

    private Integer resourceFileYRow = 0;

    private boolean fileContainsY = false;

    public Integer yIndex = 0;

    public void setField(String field) {
        this.field = field;
        fieldList = Arrays.stream(field.split(",")).collect(Collectors.toList());
        fieldSize = fieldList.size();
        if (fieldList.contains("y")){
            fileContainsY = true;
            yIndex = fieldList.indexOf("y");
        }
    }

    public void addContentSzie(){
        fileContentSize++;
    }
    public void addFileYRowSzie(){
        resourceFileYRow++;
    }
}
