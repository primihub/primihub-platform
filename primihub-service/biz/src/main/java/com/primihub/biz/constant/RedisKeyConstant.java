package com.primihub.biz.constant;

public class RedisKeyConstant {
    public static final String SYS_USER_LOGIN_STATUS_HASH_KEY="sys_user:login_status_<user_id>";
    public static final String SYS_USER_LOGIN_TOKEN_STR_KEY="sys_user:login_token_<token>";
    public static final String SYS_AUTH_BFS_LIST_STR_KEY="sys_auth:bfs_list";
    public static final String SCHEDULE_FUSION_COPY_KEY="schedule:fusion_copy_<date>_<piece>";
    public static final String VERIFICATION_CODE_TYPE_KEY="verification:code_<code_type>_<code_key>";

    // Port number key
    public final static String REQUEST_PORT_NUMBER = "model:port_number:<square>";
}
