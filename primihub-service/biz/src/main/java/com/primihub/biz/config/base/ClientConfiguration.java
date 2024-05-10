package com.primihub.biz.config.base;

import com.alibaba.nacos.api.config.annotation.NacosConfigurationProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@NacosConfigurationProperties(dataId = "client.json",autoRefreshed = true)
public class ClientConfiguration {
    public String secretId = "58644534";
    public String secretKey = "cDVMxEGRC9sMPZP1xtKnGWShi2YqwWW9yoEBoqo5PS0=";
}
