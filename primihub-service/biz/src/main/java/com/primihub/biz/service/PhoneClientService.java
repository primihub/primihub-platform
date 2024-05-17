package com.primihub.biz.service;


import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PhoneClientService {
    public Map<String, String> findSM3PhoneForSM3IdNum(Set<String> fieldValueSet) {
        HashSet<String> filteredValue = filterHashSet(fieldValueSet, 0.8);
        Map<String, String> map = new HashMap<>();
        filteredValue.forEach(value -> {
            map.put(value, generateRandomPhoneNumber());
        });
        return map;

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
