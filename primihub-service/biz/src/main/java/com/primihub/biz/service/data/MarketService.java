package com.primihub.biz.service.data;

import com.primihub.biz.constant.MarketConstant;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.po.DataVisitingUsers;
import com.primihub.biz.entity.data.req.DataVisitingUsersReq;
import com.primihub.biz.entity.data.vo.DataVisitingUsersVo;
import com.primihub.biz.repository.primarydb.data.DataMarketPrimarydbRepository;
import com.primihub.biz.repository.secondarydb.data.DataMarketRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MarketService {
    @Autowired
    private DataMarketPrimarydbRepository dataMarketPrimarydbRepository;
    @Autowired
    private DataMarketRepository dataMarketRepository;
    @Resource(name="primaryStringRedisTemplate")
    private StringRedisTemplate primaryStringRedisTemplate;

    public BaseResultEntity submitVisitingUsers(List<DataVisitingUsersReq> req) {
        Map<String, String> keySet = req.stream().collect(Collectors.toMap(DataVisitingUsersReq::getKeyValLowerCase, DataVisitingUsersReq::getValue));
        String age = keySet.get("age");
        int intAge = Integer.parseInt(age);
        if (intAge<18 || intAge>100){
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"年龄异常");
        }
        try {
            DataVisitingUsers d = new DataVisitingUsers();
            Field[] fields = d.getClass().getDeclaredFields();
            for (Field field : fields) {
                Class<?> type = field.getType();
                if (type.isAssignableFrom(Integer.class)) {
                    field.setAccessible(true);
                    String name = field.getName().toLowerCase();
                    if (keySet.containsKey(name)){
                        field.set(d,1);
                    }
                }
            }
            d.setAgeAge(Integer.valueOf(age));
            dataMarketPrimarydbRepository.saveDataVisitingUsers(d);
        }catch (Exception e){
            e.printStackTrace();
            return BaseResultEntity.failure(BaseResultEnum.FAILURE);
        }
        return BaseResultEntity.success();
    }

    public BaseResultEntity getVisitingUsers() {
        Map<String,Long> dataVisitingUsers = dataMarketRepository.selectDataVisitingUsers();
        Long total = dataVisitingUsers.get("total");
        if (total==0L) {
            return BaseResultEntity.success();
        }
        dataVisitingUsers.remove("total");
        Map<String,BigDecimal> totalMap = new HashMap<>();
        Map<String,List<DataVisitingUsersVo>> map = new HashMap<>();
        Iterator<Map.Entry<String, Long>> iterator = dataVisitingUsers.entrySet().iterator();
        DecimalFormat df = new DecimalFormat("0.00");
        while (iterator.hasNext()){
            Map.Entry<String, Long> next = iterator.next();
            String key = next.getKey();
            Long value = next.getValue();
            if ("age_age".equals(key)){
                map.put("age",new ArrayList(){{add(new DataVisitingUsersVo(MarketConstant.DICTIONARY.get("age_age"), String.valueOf(value/total),"age"));}});
            }else {
                String[] keys = key.split("_");
                if(!map.containsKey(keys[0])) {
                    map.put(keys[0],new ArrayList<>());
                }
                BigDecimal price = new BigDecimal(value).divide(new BigDecimal(total), 4, BigDecimal.ROUND_DOWN).multiply(BigDecimal.valueOf(100));
                DataVisitingUsersVo vo = new DataVisitingUsersVo(MarketConstant.DICTIONARY.get(key), df.format(price), keys[1]);
                if (totalMap.containsKey(keys[0])) {
                    totalMap.put(keys[0], totalMap.get(keys[0]).add(price));
                } else {
                    totalMap.put(keys[0], price);
                }
                map.get(keys[0]).add(vo);
            }
        }
        Iterator<String> totalMapIt = totalMap.keySet().iterator();
        BigDecimal expectDecimal = new BigDecimal(0);
        while (totalMapIt.hasNext()){
            String next = totalMapIt.next();
            BigDecimal subtract = new BigDecimal("100").subtract(totalMap.get(next));
            if (subtract.compareTo(expectDecimal)!=0){
                DataVisitingUsersVo vo = map.get(next).get(map.get(next).size() - 1);
                vo.setValue(df.format(new BigDecimal(vo.getValue()).add(subtract)));
//                map.get(next).add(map.get(next).size() - 1,vo);
            }
        }
        return BaseResultEntity.success(map);
    }

    public Map<Object, Object> display(String type, Integer operation) {
        if (operation!=0){
            if (operation == 1){
                primaryStringRedisTemplate.opsForHash().increment(MarketConstant.MARKET_DISPLAY_MAP_KEY,type,1);
            }else if (operation == 2){
                String[] split = type.split(",");
                for (String t : split) {
                    primaryStringRedisTemplate.opsForHash().put(MarketConstant.MARKET_DISPLAY_MAP_KEY,t,String.valueOf((int)(Math.random()*1000)));
                }
            }
        }
        return new TreeMap<Object, Object>(primaryStringRedisTemplate.opsForHash().entries(MarketConstant.MARKET_DISPLAY_MAP_KEY));
    }
}
