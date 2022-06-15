package com.yyds.biz.entity.base;

import lombok.Getter;

@Getter
public enum BaseResultEnum implements ResultEnumType<Integer,String> {
    SUCCESS(0,"请求成功"),
    FAILURE(-1,"请求异常"),
    LACK_OF_PARAM(100,"缺少参数"),
    PARAM_INVALIDATION(101,"无效参数"),
    TOKEN_INVALIDATION(102,"token失效"),
    NO_AUTH(103,"暂无权限"),
    CAN_NOT_ALTER(104,"无法修改"),
    CAN_NOT_DELETE(105,"无法删除"),
    NON_REPEATABLE(106,"不可重复"),
    VALIDATE_KEY_INVALIDATION(107,"验证key已失效"),
    ACCOUNT_NOT_FOUND(108,"无法找到该账号"),
    PASSWORD_NOT_CORRECT(109,"密码不正确"),
    OLD_PASSWORD_NOT_CORRECT(110,"旧密码不正确"),
    DATA_SAVE_FAIL(1001,"添加失败"),
    DATA_EDIT_FAIL(1002,"编辑失败"),
    DATA_QUERY_NULL(1003,"数据为空"),
    DATA_NO_MATCHING(1004,"数据不匹配"),
    DATA_APPROVAL(1005,"审核授权异常"),
    DATA_DEL_FAIL(1006,"删除失败"),
    DATA_RUN_TASK_FAIL(1007,"运行失败"),
    DATA_RUN_SQL_CHECK_FAIL(1008,"SQL校验失败")
    ;
    private Integer returnCode;
    private String message;

    BaseResultEnum(Integer returnCode, String message) {
        this.returnCode = returnCode;
        this.message = message;
    }

}
