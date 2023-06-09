package com.primihub.biz.config.base;

import com.alibaba.nacos.api.config.annotation.NacosConfigurationProperties;
import com.primihub.biz.entity.data.vo.ModelComponent;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@NacosConfigurationProperties(dataId = "components.json",autoRefreshed = true)
public class ComponentsConfiguration {
    private List<ModelComponent> modelComponents;
}
