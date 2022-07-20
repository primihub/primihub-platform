package com.primihub.biz.config.base;

import com.alibaba.nacos.api.config.annotation.NacosConfigurationProperties;
import com.primihub.biz.entity.data.vo.ModelComponent;
import lombok.Getter;
import lombok.Setter;
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
    private String primihubOfficalService;
    private String defaultPassword;
    private String defaultPasswordVector;
    private String grpcClientAddress;
    private Integer grpcClientPort;
    private String grpcDataSetClientAddress;
    private Integer grpcDataSetClientPort;
    private Integer grpcServerPort;
    private String uploadUrlDirPrefix;
    private String resultUrlDirPrefix;
    private String runModelFileUrlDirPrefix;
    private List<ModelComponent> modelComponents;
    private String usefulToken;
}
