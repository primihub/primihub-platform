package com.primihub.biz.config.base;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.primihub.biz.constant.SysConstant;
import com.primihub.biz.entity.data.vo.ModelComponent;
import com.primihub.biz.entity.sys.po.SysLocalOrganInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executor;


/**
 * 机构配置文件监听
 */
@Slf4j
@Component
public class OrganConfiguration {
    @Resource
    private Environment environment;
    @Resource
    private ApplicationContext applicationContext;

    private SysLocalOrganInfo sysLocalOrganInfo;

    public SysLocalOrganInfo getSysLocalOrganInfo() {
        return sysLocalOrganInfo;
    }
    public String getSysLocalOrganName(){
        return sysLocalOrganInfo==null?"":sysLocalOrganInfo.getOrganName();
    }

    public String getSysLocalOrganId(){
        return sysLocalOrganInfo==null?"":sysLocalOrganInfo.getOrganId();
    }

    /**
     * 获取organId的后12短代码
     * @return
     */
    public String getLocalOrganShortCode(){
        if (sysLocalOrganInfo==null||sysLocalOrganInfo.getOrganId()==null) {
            return null;
        }
        return sysLocalOrganInfo.getOrganId().substring(24,36);
    }

    public String generateUniqueCode(){
        if (sysLocalOrganInfo==null||sysLocalOrganInfo.getOrganId()==null) {
            return null;
        }
        return getLocalOrganShortCode()+"-"+ UUID.randomUUID().toString();
    }

    @PostConstruct
    public void init(){
        readNacosConfigOrganInfo();
        readResourceCofigComponentsInfo();
    }


    public void readNacosConfigOrganInfo(){
        try {
            String group=environment.getProperty("nacos.config.group");
            String serverAddr=environment.getProperty("nacos.config.server-addr");
            String namespace=environment.getProperty("nacos.config.namespace");
            Properties properties = new Properties();
            properties.put("serverAddr",serverAddr);
            properties.put("namespace",namespace);
            ConfigService configService= NacosFactory.createConfigService(properties);
            String organInfoContent=configService.getConfig(SysConstant.SYS_ORGAN_INFO_NAME,group,3000);
            log.info(" nacos organ_info data:{}",organInfoContent);
            if (StringUtils.isNotBlank(organInfoContent)){
                sysLocalOrganInfo = JSON.parseObject(organInfoContent,SysLocalOrganInfo.class);
            }
            configService.addListener(SysConstant.SYS_ORGAN_INFO_NAME, group, new Listener() {
                @Override
                public Executor getExecutor() {
                    return null;
                }

                @Override
                public void receiveConfigInfo(String config) {
                    log.info(" nacos receiveConfigInfo organ_info data:{}",config);
                    if (StringUtils.isNotBlank(config)){
                        sysLocalOrganInfo = JSON.parseObject(config,SysLocalOrganInfo.class);
                    }else {
                        sysLocalOrganInfo = null;
                    }
                }
            });
        }catch (Exception e){
            log.info("nacos organ_info Exception:{}",e.getMessage());
        }
    }

    public void readResourceCofigComponentsInfo(){
        try {
            ComponentsConfiguration components = applicationContext.getBean(ComponentsConfiguration.class);
            if (components.getModelComponents()!=null && components.getModelComponents().size()>0){
                return;
            }
            org.springframework.core.io.Resource  resource = new ClassPathResource(SysConstant.SYS_COMPONENTS_INFO_NAME);
            InputStream is = resource.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String data = null;
            while((data = br.readLine()) != null) {
                sb.append(data);
            }
            if (StringUtils.isNotBlank(sb.toString())){
                components.setModelComponents(JSON.parseObject(sb.toString()).getJSONArray("model_components").toJavaList(ModelComponent.class));
            }
            br.close();
            isr.close();
            is.close();
        }catch (Exception e){
            log.info("读取文件失败:{}",e.getMessage());
        }
    }

}
