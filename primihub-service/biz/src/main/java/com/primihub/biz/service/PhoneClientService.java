package com.primihub.biz.service;


import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.entity.SM3Dict;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhoneClientService {
    @Autowired
    private BaseConfiguration baseConfiguration;

    public Map<String, String> findSM3PhoneForSM3IdNum(Set<String> fieldValueSet) {
        log.info("[grpc][address] --- [{}:{}]",
                baseConfiguration.getLpyGrpcConfig().getAddress(),
                baseConfiguration.getLpyGrpcConfig().getPort());
        SM3DictClient sm3DictClient = new SM3DictClient(baseConfiguration.getLpyGrpcConfig().getAddress(), baseConfiguration.getLpyGrpcConfig().getPort());
        List<SM3Dict> replyList = sm3DictClient.commit(fieldValueSet);
        return replyList.stream().collect(Collectors.toMap(SM3Dict::getIdNum, SM3Dict::getPhoneNum));
    }

    public <T> Set<T> filterHashSet(Set<T> originalSet, double filterPercentage) {
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

    public Map<String, Double> generateScoreForKeySet(Set<String> set) {
        Random random = new Random();
        DecimalFormat format = new DecimalFormat("#.####");

        return set.stream().collect(Collectors.toMap(
                Function.identity(),
                o -> generateRandomScore(random, format)
        ));
    }

    private static double generateRandomScore(Random random, DecimalFormat format) {
        double randNum = random.nextDouble();
        return Double.parseDouble(format.format(randNum));

    }
}
