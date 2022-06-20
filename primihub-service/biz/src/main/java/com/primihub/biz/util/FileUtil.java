package com.primihub.biz.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class FileUtil {

    public static Integer getFileLineNumber(String filePath){
        try {
            FileReader fileReader = new FileReader(filePath);
            LineNumberReader lineNumberReader = new LineNumberReader(fileReader);
            lineNumberReader.skip(Long.MAX_VALUE);
            Integer lineNumber = lineNumberReader.getLineNumber();
            lineNumberReader.close();
            fileReader.close();
            return lineNumber;
        } catch (IOException e) {
            log.info("{}-IOException",filePath,e.getMessage());
        }
        return -0;
    }

    public static List<String> getFileContent(String filePath,Integer severalLines) {
        List<String> list = new ArrayList<>();
        try {
            File file = new File(filePath);
            if (!file.exists()){
                log.info("{}-不存在",filePath);
                return list;
            }
            String charsetName = charset(file);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath),charsetName));
            String str;
            while((str = bufferedReader.readLine())!=null) {
                list.add(str);
                if (severalLines!=null&&severalLines>0){
                    if (list.size()==severalLines)
                        break;
                }
            }
            bufferedReader.close();
        } catch (IOException e) {
            log.info("{}-IOException: {}",filePath,e.getMessage());
        }
        return list;
    }
    /**
     * 判断文本文件的字符集，文件开头三个字节表明编码格式。
     * @return
     * @throws Exception
     * @throws Exception
     */
    public static String charset(File file) {
        String charset = "GBK";
        byte[] first3Bytes = new byte[3];
        try {
            boolean checked = false;
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            bis.mark(0); // 读者注： bis.mark(0);修改为 bis.mark(100);我用过这段代码，需要修改上面标出的地方。
            int read = bis.read(first3Bytes, 0, 3);
            if (read == -1) {
                bis.close();
                return charset; // 文件编码为 ANSI
            } else if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
                charset = "UTF-16LE"; // 文件编码为 Unicode
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xFE && first3Bytes[1] == (byte) 0xFF) {
                charset = "UTF-16BE"; // 文件编码为 Unicode big endian
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xEF && first3Bytes[1] == (byte) 0xBB
                    && first3Bytes[2] == (byte) 0xBF) {
                charset = "UTF-8"; // 文件编码为 UTF-8
                checked = true;
            }
            bis.reset();
            if (!checked) {
                while ((read = bis.read()) != -1) {
                    if (read >= 0xF0)
                        break;
                    if (0x80 <= read && read <= 0xBF) // 单独出现BF以下的，也算是GBK
                        break;
                    if (0xC0 <= read && read <= 0xDF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) // 双字节 (0xC0 - 0xDF)
                            // (0x80 - 0xBF),也可能在GB编码内
                            continue;
                        else
                            break;
                    } else if (0xE0 <= read && read <= 0xEF) { // 也有可能出错，但是几率较小
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) {
                            read = bis.read();
                            if (0x80 <= read && read <= 0xBF) {
                                charset = "UTF-8";
                                break;
                            } else
                                break;
                        } else
                            break;
                    }
                }
            }
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        System.out.println("--文件-> [" + file.getPath() + "] 采用的字符集为: [" + charset + "]");
        return charset;
    }

    public static void writeFile(String filePath,String fileNamePath,List<String> dataList) throws IOException {
        File tempFile=new File(filePath);
        if(!tempFile.exists())
            tempFile.mkdirs();
        FileOutputStream fos=new FileOutputStream(new File(fileNamePath));
        OutputStreamWriter osw=new OutputStreamWriter(fos, "UTF-8");
        BufferedWriter  bw=new BufferedWriter(osw);
        for(String data:dataList){
            bw.write(data+"\t\n");
        }
        bw.close();
        osw.close();
        fos.close();
    }

    public static List<LinkedHashMap<String, Object>> getCsvData(String filePath,Integer pageNo, Integer pageSize){
        List<LinkedHashMap<String, Object>> dataList = new ArrayList<>();
        try(Stream<String> curStream= Files.lines(Paths.get(filePath), Charset.forName(charset(new File(filePath))))) {
            List<String> list=curStream.skip(pageNo).limit(pageSize+1).collect(Collectors.toList());
            if (list.size()==0)
                return dataList;
            String[] fields = list.get(0).split(",");
            for(int i=1;i<list.size();i++) {
                String[] data = StringUtils.splitPreserveAllTokens(list.get(i), ",");
                if (Integer.valueOf(data.length)==Integer.valueOf(fields.length))
                    dataList.add(readValues(data,fields));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataList;
    }

    public static LinkedHashMap<String,Object> readValues(String[] values, String[] headers){
        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
        for (int i = 0; i < headers.length; i++) {
            map.put(headers[i],values[i]);
        }
        return map;
    }

//    public static void main(String[] args) {
//        List<String> fileContent = getFileContent("d:/data/upload/1/2022042912/28cc1fce-2487-47e8-9f59-4ca56aa487d1.csv", 1);
//        fileContent.addAll(getFileContent("d:/data/upload/1/2022042718/28efe5c0-ec48-491a-8722-47b4fea62746.csv", 1));
//        fileContent.addAll(getFileContent("C:\\Users\\17801\\Downloads\\server1.csv", 1));
//        fileContent.addAll(getFileContent("C:\\Users\\17801\\Downloads\\liwehua.txt", 1));
//        for (String s : fileContent) {
//            System.out.println(s);
//        }
//    }
}
