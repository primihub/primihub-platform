package com.primihub.biz.entity.sys.po;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/*
{
    "PIR": {
        "resourceId": "704a92e392fd-89fc0bd7-a4af-419d-b303-55604956628e"
    },
    "PSI": {

    },
    "ADPrediction": {},
    "UserPortrait": {},
    "reasoning": {},
    "CredentialsPIR": {
        "resourceId": "704a92e392fd-89fc0bd7-a4af-419d-b303-55604956628e",
    }
}
 */
@Getter
@Setter
public class SysMarketInfo {
    private Map<String, Object> PIR;
    private Map<String, Object> PSI;
    private Map<String, Object> ADPrediction;
    private Map<String, Object> UserPortrait;
    private Map<String, Object> reasoning;
    private Map<String, Object> CredentialsPIR;
}
