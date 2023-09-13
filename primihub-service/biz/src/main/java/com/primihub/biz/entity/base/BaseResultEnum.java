package com.primihub.biz.entity.base;

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
    NO_ORGAN_DATA(111,"没有机构信息请重新生成"),
    HANDLE_RIGHT_NOW(112,"正在被操作"),
    NOT_IN_THE_WHITE_LIST(113,"不在短信服务白名单中"),
    FIVE_MINUTES_LATER(114,"请在五分钟后重试"),
    ONE_MINUTES_LATER(115,"请在一分钟后重试"),
    OUTNUMBER(116,"超出当天发送条数"),
    SMS_FAILURE(117,"SMS发送失败"),
    VERIFICATION_CODE(118,"验证码失败"),
    RESTRICT_LOGIN(119,"限制登录"),
    AUTH_LOGIN(120,"授权登录失败"),
    FORCE_VALIDATION(121,"强制登录验证滑块"),
    DATA_SAVE_FAIL(1001,"添加失败"),
    DATA_EDIT_FAIL(1002,"编辑失败"),
    DATA_QUERY_NULL(1003,"数据为空"),
    DATA_NO_MATCHING(1004,"数据不匹配"),
    DATA_APPROVAL(1005,"审核授权失败"),
    DATA_DEL_FAIL(1006,"删除失败"),
    DATA_RUN_TASK_FAIL(1007,"失败"),
    DATA_RUN_SQL_CHECK_FAIL(1008,"SQL校验失败"),
    DATA_RUN_FILE_CHECK_FAIL(1009,"文件解析失败"),
    DATA_DOWNLOAD_TASK_ERROR_FAIL(1010,"文件下载失败"),
    DATA_DB_FAIL(1011,"数据库失败"),
    DATA_LOG_FAIL(1012,"查询日志失败"),
    DECRYPTION_FAILED(1013,"解密失败")
    ;
    private Integer returnCode;
    private String message;

    BaseResultEnum(Integer returnCode, String message) {
        this.returnCode = returnCode;
        this.message = message;
    }

}
