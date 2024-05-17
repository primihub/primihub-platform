package com.primihub.biz.entity.data.req;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScoreModelReq {
    private String organId;
    /** url 代码 */
    private String scoreModelCode;
    /** 中文 */
    private String scoreModelName;
    /** 英文 */
    private String scoreModelType;
    /** 键 */
    private String scoreKey;
}
