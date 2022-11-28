package com.primihub.biz.constant;

import java.util.regex.Pattern;

public class DataConstant {
    public final static String FEDLEARNER_JOB_RUN = "http://fedlearner/job_api/run";
    public final static String MATCHES="[a-zA-Z]+";
    public final static String FIELD_NAME_AS="field_";
    // Template address
    public final static String FREEMARKER_PYTHON_EN_PAHT= "disxgb_en.ftl";
    public final static String FREEMARKER_PYTHON_HOMO_LR_PAHT= "homo_lr.ftl";
    public final static String FREEMARKER_PYTHON_HOMO_LR_INFER_PAHT= "homo_lr_infer.ftl";
    public final static String FREEMARKER_PYTHON_EXCEPTION_PAHT= "exception.ftl";
    public final static String FREEMARKER_PYTHON_DATA_ALIGN_PAHT= "data_align.ftl";
    // python dataset host、guest
    public final static String PYTHON_LABEL_DATASET = "label_dataset";
    public final static String PYTHON_GUEST_DATASET = "guest_dataset";
    public final static String PYTHON_ARBITER_DATASET = "arbiter_dataset";
    public final static String PYTHON_CALCULATION_FIELD = "label_field";
    public final static String PYTHON_LABEL_PORT = "label_port";
    public final static String PYTHON_GUEST_PORT = "guest_port";

    // Set the timeout for 5 minutes
    public final static Integer UPDATE_MODEL_TIMEOUT = 300000;

    public final static Integer INSERT_DATA_TABLE_PAGESIZE = 1000;

    public final static Pattern RESOURCE_PATTERN_INTEGER = Pattern.compile("^-?\\d{1,9}$");
    public final static Pattern RESOURCE_PATTERN_LONG = Pattern.compile("^-?\\d{10,}$");
    public final static Pattern RESOURCE_PATTERN_DOUBLE = Pattern.compile("^-?\\d+\\.\\d+$");
    public final static Integer READ_DATA_ROW = 50;
    public final static Integer COPY_PAGE_NUM = 20;

    // ModelComponentService impl bean name suffix
    public final static String COMPONENT_BEAN_NAME_SUFFIX = "ComponentTaskServiceImpl";

    // Port number Range
    public final static Long[] GUEST_PORT_RANGE = new Long[]{20000L,30000L};
    public final static Long[] HOST_PORT_RANGE = new Long[]{40000L,50000L};

    public final static Long GRPC_SERVER_TIMEOUT = 24L * 60L * 60L * 1000L;
    public final static Long GRPC_FILE_TIMEOUT = 5L * 60L * 1000L;

    public final static String TASK_LOG_FILE_NAME = "taskLog.log";
}
