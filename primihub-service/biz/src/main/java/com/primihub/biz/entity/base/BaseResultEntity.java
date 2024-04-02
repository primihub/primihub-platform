package com.primihub.biz.entity.base;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;


@ApiModel("统一返回结构")
@Setter
@Getter
public class BaseResultEntity<T> {
    @ApiModelProperty(value = "结果编码",example = "0")
    private Integer code;
    @ApiModelProperty(value = "结果消息",example = "请求成功")
    private String msg;
    @ApiModelProperty(value = "结果数据")
    private T result;
    @ApiModelProperty(value = "结果补充消息")
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
