package com.primihub.biz.constant;

public class RedisKeyConstant {
    public static final String SYS_USER_LOGIN_STATUS_HASH_KEY="sys_user:login_status_<user_id>";
    public static final String SYS_USER_LOGIN_TOKEN_STR_KEY="sys_user:login_token_<token>";
    public static final String SYS_USER_CHANGE_ACCOUNT_KEY="sys_user:change_account_<userId>";
    public static final String SYS_USER_LOGIN_PASS_ERRER_KEY="sys_user:login_pe_<user_id>";
    public static final String SYS_AUTH_BFS_LIST_STR_KEY="sys_auth:bfs_list";
    public static final String SCHEDULE_FUSION_COPY_KEY="schedule:fusion_copy_<date>_<piece>";
    public static final String VERIFICATION_CODE_TYPE_KEY="verification:code_<code_type>_<code_key>";

    // task key
    public final static String TASK_STATUS_KEY = "ts:<taskId>:<jobId>";
    public final static String TASK_STATUS_LIST_KEY = "ts:list";

    public final static String SEATUNNEL_DATA_LIST_KEY = "se:dl:<traceId>";

    public final static String PIR_TASK_DATA_SET = "pir:t:d:s:<taskId>";
    public final static String PIR_TASK_DATA_LIST = "pir:t:d:l:<taskId>";
}
