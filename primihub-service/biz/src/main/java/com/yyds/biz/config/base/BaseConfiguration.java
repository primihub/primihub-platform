package com.yyds.biz.config.base;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.nacos.api.config.annotation.NacosConfigurationProperties;
import com.yyds.biz.entity.data.vo.ModelComponent;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@Component
@NacosConfigurationProperties(dataId = "base.json",autoRefreshed = true)
public class BaseConfiguration {
    private Set<String> tokenValidateUriBlackList;
    private Set<String> needSignUriList;
    private String defaultPassword;
    private String defaultPasswordVector;
    private String grpcClientAddress;
    private Integer grpcClientPort;
    private Integer grpcServerPort;
    private String uploadUrlDirPrefix;
    private String resultUrlDirPrefix;
    private List<ModelComponent> modelComponents;
}
