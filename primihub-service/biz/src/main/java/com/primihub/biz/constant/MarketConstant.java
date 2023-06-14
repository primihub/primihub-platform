package com.primihub.biz.constant;

import java.util.HashMap;
import java.util.Map;

public class MarketConstant {

    public static final String MARKET_DISPLAY_MAP_KEY = "market:display:key";


    public static final Map<String,String> DICTIONARY = new HashMap(){{
        put("familiarity_practitioner","从业者");
        put("familiarity_AlreadyInUse","已在应用");
        put("familiarity_veryFamiliar","非常熟悉");
        put("familiarity_generalFamiliar","一般熟悉");
        put("familiarity_notKnow","完全不懂");
        put("gender_male","男");
        put("gender_female","女");
        put("city_beijing","北京");
        put("city_shanghai","上海");
        put("city_shenzhen","深圳");
        put("city_hangzhou","杭州");
        put("city_changsha","长沙");
        put("industry_internet","互联网");
        put("industry_financial","金融");
        put("industry_government","政府");
        put("industry_medical","医疗");
        put("industry_industrial","工业");
        put("industry_car","汽车");
        put("industry_newEnergy","新能源");
        put("industry_other","其他");
        put("visitPurposes_cooperation","商业合作");
        put("visitPurposes_learning","学习");
        put("visitPurposes_trial","试用");
        put("visitPurposes_browse","随便看看");
        put("age_age","年龄");
        put("jobPosition_manager","管理者");
        put("jobPosition_PM","产品");
        put("jobPosition_developer","技术");
        put("jobPosition_commerceAffairs","商务");
        put("jobPosition_solution","解决方案");
        put("jobPosition_other","其他");
    }};

}