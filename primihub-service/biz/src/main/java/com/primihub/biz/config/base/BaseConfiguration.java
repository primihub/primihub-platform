package com.primihub.biz.config.base;

import com.alibaba.nacos.api.config.annotation.NacosConfigurationProperties;
import com.primihub.biz.entity.data.vo.ModelComponent;
import com.primihub.biz.entity.sys.config.BaseAuthConfig;
import com.primihub.biz.entity.sys.config.BaseGrpcClientConfig;
import com.primihub.biz.entity.sys.config.LokiConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@Component
@NacosConfigurationProperties(dataId = "base.json",autoRefreshed = true)
public class BaseConfiguration {
    private Set<String> tokenValidateUriBlackList;
    private Set<String> needSignUriList;
    private Set<Long> adminUserIds;
    private String primihubOfficalService;
    private String defaultPassword;
    private String defaultPasswordVector;
    private BaseGrpcClientConfig grpcClient;
    private Integer grpcServerPort;
    private String uploadUrlDirPrefix;
    private String resultUrlDirPrefix;
    private String runModelFileUrlDirPrefix;
    private List<ModelComponent> modelComponents;
    private String usefulToken;
    // resource
    private boolean displayDatabaseSourceType = false;
    // auth
    private Map<String, BaseAuthConfig> authConfigs;
    // mail
    private MailProperties mailProperties;
    // Use in mail text content
    private String systemDomainName;
    // loki
    private LokiConfig lokiConfig;
}
