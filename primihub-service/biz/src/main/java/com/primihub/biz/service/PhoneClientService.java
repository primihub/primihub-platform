package com.primihub.biz.service;


import com.primihub.biz.entity.data.po.DataCore;
import com.primihub.biz.util.crypt.SM3Util;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PhoneClientService {
    public Map<String, String> findSM3PhoneForSM3IdNum(Set<String> fieldValueSet) {
        HashSet<String> filteredValue = filterHashSet(fieldValueSet, 0.8);
        Set<DataCore> dataCoreSet = filteredValue.stream().map(value -> {
            DataCore dataCore = new DataCore();
            dataCore.setIdNum(value);
            String SM3PhoneNum = SM3Util.encrypt(generateRandomPhoneNumber());
            dataCore.setPhoneNum(SM3PhoneNum);
            // todo 处理Y值
            return dataCore;
        }).collect(Collectors.toSet());
        return dataCoreSet;

    }

    public <T> HashSet<T> filterHashSet(Set<T> originalSet, double filterPercentage) {
        int originalSize = originalSet.size();
        int filterSize = (int) (originalSize * filterPercentage);
        Random random = new Random();
        HashSet<T> filteredSet = new HashSet<>(filterSize);

        while (filteredSet.size() < filterSize) {
            int randomIndex = random.nextInt(originalSize);
            T randomElement = (T) originalSet.toArray()[randomIndex];
            filteredSet.add(randomElement);
        }

        return filteredSet;
    }

    public static String generateRandomPhoneNumber() {
        Random random = new Random();
        StringBuilder phoneNumber = new StringBuilder();

        // 生成手机号前两位
        int[] prefixOptions = {13, 14, 15, 16, 17, 18, 19};
        int randomPrefixIndex = random.nextInt(prefixOptions.length);
        phoneNumber.append(prefixOptions[randomPrefixIndex]);

        // 生成手机号后八位
        for (int i = 0; i < 8; i++) {
            int digit = random.nextInt(10);
            phoneNumber.append(digit);
        }

        return phoneNumber.toString();
    }
}
