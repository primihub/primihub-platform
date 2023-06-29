package com.primihub.biz.config.base;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.primihub.biz.constant.DataConstant;
import com.primihub.sdk.util.TemplatesHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Properties;
import java.util.concurrent.Executor;

@Slf4j
@Component
public class TemplatesConfig {
    @Resource
    private Environment environment;
    @Autowired
    private BaseConfiguration baseConfiguration;

    @PostConstruct
    public void readNacosFreemarkerPython(){
        try {
            if (!baseConfiguration.getOpenDynamicTuning()){
                log.info("Close nacos template debugging");
                return;
            }
            String group=environment.getProperty("nacos.config.group");
            String serverAddr=environment.getProperty("nacos.config.server-addr");
            Properties properties = new Properties();
            properties.put("serverAddr",serverAddr);
            ConfigService configService= NacosFactory.createConfigService(properties);
            for (String freemarkerPythonPath : DataConstant.FREEMARKER_PYTHON_PATHS) {
                String content=configService.getConfig(freemarkerPythonPath,group,3000);
                log.info(" nacos freemarkerPythonPath:{}",freemarkerPythonPath);
                log.debug(" nacos freemarkerPythonPath:{} data:{}",freemarkerPythonPath,content);
                if (StringUtils.isNotBlank(content)){
                    TemplatesHelper.getTemplatesMap().put(freemarkerPythonPath,content);
                    log.info(" nacos freemarkerPythonPath:{} no null",freemarkerPythonPath);
                }
                configService.addListener(freemarkerPythonPath, group, new Listener() {
                    @Override
                    public Executor getExecutor() {
                        return null;
                    }

                    @Override
                    public void receiveConfigInfo(String config) {
                        log.info(" nacos read freemarkerPython:{}",freemarkerPythonPath);
                        log.debug(" nacos read freemarkerPython:{} data:{}",freemarkerPythonPath,config);
                        if (StringUtils.isNotBlank(config)){
                            TemplatesHelper.getTemplatesMap().put(freemarkerPythonPath,config);
                            log.info(" nacos read freemarkerPython:{} no null",freemarkerPythonPath);
                        }
                    }
                });
                configService.removeListener(freemarkerPythonPath, group, new Listener() {
                    @Override
                    public Executor getExecutor() {
                        return null;
                    }

                    @Override
                    public void receiveConfigInfo(String config) {
                        log.info(" nacos remove freemarkerPython:{}",freemarkerPythonPath);
                        log.debug(" nacos remove freemarkerPython:{} data:{}",freemarkerPythonPath,config);
                        TemplatesHelper.getTemplatesMap().remove(freemarkerPythonPath);
                    }
                });
            }

        }catch (Exception e){
            log.info("nacos freemarkerPython Exception:{}",e.getMessage());
        }
    }
}
