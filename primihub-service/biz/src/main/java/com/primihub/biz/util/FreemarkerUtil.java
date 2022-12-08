package com.primihub.biz.util;

import com.primihub.biz.constant.DataConstant;
import com.primihub.biz.tool.TemplatesHelper;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class FreemarkerUtil {

    public static String configurerCreateFreemarkerContent(String ftlName, FreeMarkerConfigurer freeMarkerConfigurer, Map<String, String> map){
        if (TemplatesHelper.getTemplatesMap().containsKey(ftlName)){
            return stringTemplateLoaderFreemarkerContent(ftlName,map);
        }
        Configuration configuration = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        configuration.setDefaultEncoding("utf-8");
        try {
            Template template = freeMarkerConfigurer.getConfiguration().getTemplate(ftlName);
            return FreeMarkerTemplateUtils.processTemplateIntoString(template,map);
        } catch (IOException | TemplateException e) {
            log.info("创建模板失败:{}",e.getMessage());
        }
        return null;
    }

    public static  String stringTemplateLoaderFreemarkerContent(String ftlName, Map<String, String> map){
        try {
            Configuration configuration = new Configuration(Configuration.getVersion());
            StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
            stringTemplateLoader.putTemplate(ftlName, TemplatesHelper.getTemplatesMap().get(ftlName));
            configuration.setTemplateLoader(stringTemplateLoader);
            Template template = configuration.getTemplate(ftlName);
            return FreeMarkerTemplateUtils.processTemplateIntoString(template,map);
        }catch (Exception e){
            log.info("创建模板失败:{}",e.getMessage());
        }
        return null;
    }


    public static String configurerCreateFreemarker(String path, String fileName,String ftlName, FreeMarkerConfigurer freeMarkerConfigurer, Map<String, String> map){
        if (TemplatesHelper.getTemplatesMap().containsKey(ftlName)){
            return stringTemplateLoaderFreemarkerContent(ftlName,map);
        }
        Configuration configuration = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        configuration.setDefaultEncoding("utf-8");
        try {
            Template template = freeMarkerConfigurer.getConfiguration().getTemplate(ftlName);
            File outFile = new File(path);
            if (!outFile.exists()){
                outFile.mkdirs();
            }
            String allPath = path + fileName;
            Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(allPath),"utf-8"));
            template.process(map,out);
            out.close();
            return allPath;
        } catch (IOException | TemplateException e) {
            log.info("{}:{}-{}:{}-{}-{}---创建模板失败:{}",DataConstant.PYTHON_GUEST_DATASET,map.get(DataConstant.PYTHON_GUEST_DATASET),
                    DataConstant.PYTHON_LABEL_DATASET,map.get(DataConstant.PYTHON_LABEL_DATASET),
                    path,fileName,e.getMessage());
        }
        return null;
    }

    public static String createFreemarker(String path,String fileName,String packagePaht,String ftlName, Map<String, String> map){
        if (TemplatesHelper.getTemplatesMap().containsKey(ftlName)){
            return stringTemplateLoaderFreemarkerContent(ftlName,map);
        }
        Configuration configuration = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        configuration.setDefaultEncoding("utf-8");
        try {
            configuration.setClassForTemplateLoading(FreemarkerUtil.class.getClass(), packagePaht);
            Template template = configuration.getTemplate(ftlName);
            File outFile = new File(path);
            if (!outFile.exists()){
                outFile.mkdirs();
            }
            String allPath = path + fileName;
            Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(allPath),"utf-8"));
            template.process(map,out);
            out.close();
            return allPath;
        } catch (IOException | TemplateException e) {
            log.info("{}:{}-{}:{}-{}-{}---创建模板失败:{}",DataConstant.PYTHON_GUEST_DATASET,map.get(DataConstant.PYTHON_GUEST_DATASET),
                    DataConstant.PYTHON_LABEL_DATASET,map.get(DataConstant.PYTHON_LABEL_DATASET),
                    path,fileName,e.getMessage());
        }
        return null;
    }

    public static void main(String[] args) {
        Map<String, String> map = new HashMap<>();
        map.put(DataConstant.PYTHON_GUEST_DATASET,"1118");
        map.put(DataConstant.PYTHON_LABEL_DATASET,"1119");
//        String pythonFreemarker = createFreemarker("/Users/zhongziqian/data/freemarker", "ceshi.py", DataConstant.FREEMARKER_PACKAGE_PAHT,DataConstant.FREEMARKER_PYTHON_PAHT, map);
//        String pythonFreemarker = configurerCreateFreemarkerContent(DataConstant.FREEMARKER_PACKAGE_PAHT,DataConstant.FREEMARKER_PYTHON_PAHT, map);
//        System.out.println(pythonFreemarker);
    }
}
