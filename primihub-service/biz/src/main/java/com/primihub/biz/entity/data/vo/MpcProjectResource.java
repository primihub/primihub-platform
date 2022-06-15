package com.primihub.biz.entity.data.vo;


import com.primihub.biz.entity.data.po.DataFileField;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Data
public class MpcProjectResource {

    public MpcProjectResource(Long id, String label) {
        this.id = id;
        this.label = label;
        this.children = new ArrayList<>();
    }

    public MpcProjectResource(Long id, String label, List<DataFileField> list) {
        this.id = id;
        this.label = label;
        if (list!=null&&list.size()>0){
            this.children = new ArrayList<>();
            for (DataFileField dataFileField : list) {
                if (StringUtils.isNotBlank(dataFileField.getFieldAs())){
                    children.add(new MpcProjectResource(dataFileField.getFieldId(),dataFileField.getFieldAs()));
                }else {
                    children.add(new MpcProjectResource(dataFileField.getFieldId(),dataFileField.getFieldName()));
                }
            }
        }
    }

    private Long id;
    private String label;
    private List<MpcProjectResource> children;

}
