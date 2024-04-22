package com.primihub.biz.entity.data.po;


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
    private Double score1;
    private Double score2;
    private Double score3;
    private Double score4;
    private Double score5;
    private Double score6;
    private Double score7;
    private Double score8;
    private Double score9;
    private Double score10;
    @JsonIgnore
    private Integer isDel;
    @JsonIgnore
    private Date createDate;
    @JsonIgnore
    private Date updateDate;
}
