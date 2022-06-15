package com.yyds.biz.entity.base;

import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class BaseResultEntity<T> {
    private Integer code;
    private String msg;
    private T result;
    private String extra;

    public BaseResultEntity() {
    }

    public BaseResultEntity(Integer code, String msg, T result) {
        this.code = code;
        this.msg = msg;
        this.result = result;
    }

    public BaseResultEntity(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public BaseResultEntity(ResultEnumType<Integer,String> baseResultEnum) {
        this.code = baseResultEnum.getReturnCode();
        this.msg = baseResultEnum.getMessage();
    }

    public BaseResultEntity(ResultEnumType<Integer,String> baseResultEnum, String extraInfo) {
        this.code = baseResultEnum.getReturnCode();
        this.msg = baseResultEnum.getMessage()+":"+extraInfo;
    }

    public BaseResultEntity(ResultEnumType<Integer,String> baseResultEnum, String extraInfo, String configInfo) {
        this.code = baseResultEnum.getReturnCode();
        this.msg = baseResultEnum.getMessage()+":"+extraInfo;
        this.extra =configInfo;
    }

    public BaseResultEntity(T result) {
        this.code = BaseResultEnum.SUCCESS.getReturnCode();
        this.msg = BaseResultEnum.SUCCESS.getMessage();
        this.result =result;
    }

    public BaseResultEntity(T result,String extra) {
        this.code = BaseResultEnum.SUCCESS.getReturnCode();
        this.msg = BaseResultEnum.SUCCESS.getMessage();
        this.result =result;
        this.extra =extra;
    }

    public static BaseResultEntity success(){
        return new BaseResultEntity(BaseResultEnum.SUCCESS);
    }

    public static <T> BaseResultEntity success(T result){
        return new BaseResultEntity(result);
    }

    public static <T> BaseResultEntity success(T result,String extra){
        return new BaseResultEntity(result,extra);
    }

    public static BaseResultEntity failure(BaseResultEnum baseResultEnum){
        return new BaseResultEntity(baseResultEnum);
    }

    public static BaseResultEntity failure(BaseResultEnum baseResultEnum, String extraInfo, String configInfo){
        return new BaseResultEntity(baseResultEnum,extraInfo,configInfo);
    }

    public static BaseResultEntity failure(BaseResultEnum baseResultEnum, String extraInfo){
        return new BaseResultEntity(baseResultEnum,extraInfo);
    }

}
