package com.primihub.biz.entity.data.po.lpy;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class DataCore {
    private Long id;
    private String idNum;
    private String phoneNum;
    private Double y;
    /** 以下是营销分 */
    private Double score;
    private String scoreModelType;
    @JsonIgnore
    private Integer isDel;
    @JsonIgnore
    private Date createDate;
    @JsonIgnore
    private Date updateDate;
}
