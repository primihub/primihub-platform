package com.primihub.biz.entity.data.base;


import lombok.Data;
import java.util.List;

/**
 * PIR查询条件
 */
@Data
public class DataPirKeyQuery {
    /** 查询key数组  ['a','b'] */
    private String[] key;
    /** 查询内容数组  [['a','b']] */
    private List<String[]> query;
}
