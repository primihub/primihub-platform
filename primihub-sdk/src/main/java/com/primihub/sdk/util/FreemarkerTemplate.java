package com.primihub.sdk.util;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class FreemarkerTemplate {
    private final static Logger logger = LoggerFactory.getLogger(FreemarkerTemplate.class);
    private final static String SLASH = "/templates/";
    // freemarker配置
    private Configuration configuration = null;

    private FreemarkerTemplate() {
        configuration = new Configuration(Configuration.getVersion());
        configuration.setDefaultEncoding("utf-8");
        configuration.setClassForTemplateLoading(this.getClass(), SLASH);
        configuration.setNumberFormat("#.######");
        configuration.setBooleanFormat("true,false");
    }

    //单例
    private volatile static FreemarkerTemplate instance;
    public static FreemarkerTemplate getInstance(){
        if (instance == null) {
            synchronized (FreemarkerTemplate.class) {
                if (instance == null) {
                    instance = new FreemarkerTemplate();
                }
            }
        }
        return instance;
    }

    /**
     * 输出模板字符串
     * @param dataMap
     * @param templatePath
     * @return
     */
    public String generateTemplateStr(Map<String, Object> dataMap, String templatePath){
        try{
            if (TemplatesHelper.getTemplatesMap().containsKey(templatePath)){
                return generateTemplateStrFreemarkerContent(templatePath,TemplatesHelper.getTemplatesMap().get(templatePath),dataMap);
            }
            Writer writer = new StringWriter();
            generateTemplate(dataMap, templatePath, writer);
            String content = writer.toString();
            writer.flush();
            writer.close();
            return content;
        }catch (Exception e){
            logger.error(e.getMessage(), e);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 模板
     * @param dataMap
     * @param templatePath
     * @param writer
     * @throws IOException
     * @throws TemplateException
     */
    public void generateTemplate(Map<String, Object> dataMap, String templatePath, Writer writer) throws IOException, TemplateException {
        Template template = null;
        template = configuration.getTemplate(templatePath);
        template.process(dataMap, writer);
    }

    public String generateTemplateStrFreemarkerContent(String ftlName,String templatesContent, Map<String, Object> map){
        try {
            StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
            stringTemplateLoader.putTemplate(ftlName, templatesContent);
            configuration.setTemplateLoader(stringTemplateLoader);
            Template template = configuration.getTemplate(ftlName);
            StringWriter result = new StringWriter(1024);
            template.process(map, result);
            return result.toString();
        }catch (Exception e){
            logger.error(e.getMessage(), e);
            e.printStackTrace();
            return null;
        }
    }

//    public static void main(String[] args) {
//        Map<String, Object> map = new HashMap<>();
//        map.put("indicatorFileName","1");
//        map.put("predictFileName","1");
//        map.put("label_dataset","1");
//        map.put("hostLookupTable","1");
//        map.put("guest_dataset","1");
//        map.put("guestModelFileName","1");
//        map.put("guestLookupTable","1");
//        map.put("hostModelFileName","1");
//        System.out.println(FreemarkerTemplate.getInstance().generateTemplateStr(map,"hetero_xgb.ftl"));
//    }
}
