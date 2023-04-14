package com.primihub.biz.entity.data.vo;


import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Data
public class ComponentResourceVo {
    public ComponentResourceVo() {}
    public ComponentResourceVo(String fileStr) {
        if (StringUtils.isBlank(fileStr)){
            fileStr = "id,name";
        }
        String[] fileSplit = fileStr.split(",");
        yfile = new ArrayList<>();
        if (fileSplit.length==0){
            yfile.add(new ComponentResourceYVo("id","id"));
            yfile.add(new ComponentResourceYVo("name","name"));
        }else {
            for (String file : fileSplit) {
                yfile.add(new ComponentResourceYVo(file,file));
            }
        }
    }

    private String resourceId;
    private String resourceName;
    private List<ComponentResourceYVo> yfile;
}
