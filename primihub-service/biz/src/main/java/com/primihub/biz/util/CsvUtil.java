package com.primihub.biz.util;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class CsvUtil {
    /**
     * 简单的读取
     * @throws Exception
     */
    public static List<String[]> csvReader(String path,Integer size) throws Exception {
        List<String[]> list = new ArrayList<>();
        try {
            InputStreamReader reader = new InputStreamReader(new FileInputStream(path), FileUtil.charset(new File(path)));
            CSVReader csvReader = new CSVReaderBuilder(reader).build();
            Iterator<String[]> iterator = csvReader.iterator();
            while (iterator.hasNext()){
                list.add(iterator.next());
                if (size!=null&&size>0){
                    if (list.size()==size) {
                        break;
                    }
                }
            }
            csvReader.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return list;

    }

//    public static void main(String[] args) throws Exception {
//        csvReader("/Users/zhongziqian/Downloads/qqqmo.csv",100);
//    }
}
