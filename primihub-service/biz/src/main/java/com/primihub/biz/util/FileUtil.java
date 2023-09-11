package com.primihub.biz.util;

import com.primihub.biz.constant.DataConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.*;
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

    public static boolean isFileExists(String filePath){
        File file = new File(filePath);
        boolean isFile = file.isFile();
        if (!isFile){
            isFile = !Files.notExists(Paths.get(filePath));
        }
        return isFile;
    }

    public static String getFileContent(String filePath){
        try {
            File file = new File(filePath);
            if (!file.exists()){
                log.info("{}-不存在",filePath);
                return null;
            }
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath),charset(file)));
            StringBuilder sb = new StringBuilder();
            String str;
            while((str = bufferedReader.readLine())!=null) {
                sb.append(str+"\t\n");
            }
            bufferedReader.close();
            return sb.toString();
        } catch (IOException e) {
            log.info("{}-IOException: {}",filePath,e.getMessage());
        }
        return null;
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
                    if (list.size()==severalLines) {
                        break;
                    }
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
                    if (read >= 0xF0) {
                        break;
                    }
                    if (0x80 <= read && read <= 0xBF) // 单独出现BF以下的，也算是GBK
                    {
                        break;
                    }
                    if (0xC0 <= read && read <= 0xDF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) // 双字节 (0xC0 - 0xDF)
                            // (0x80 - 0xBF),也可能在GB编码内
                        {
                            continue;
                        } else {
                            break;
                        }
                    } else if (0xE0 <= read && read <= 0xEF) { // 也有可能出错，但是几率较小
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) {
                            read = bis.read();
                            if (0x80 <= read && read <= 0xBF) {
                                charset = "UTF-8";
                                break;
                            } else {
                                break;
                            }
                        } else {
                            break;
                        }
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
        if(!tempFile.exists()) {
            tempFile.mkdirs();
        }
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

    public static List<LinkedHashMap<String, Object>> getCsvData(String filePath, Integer pageSize){
        List<LinkedHashMap<String, Object>> dataList = new ArrayList<>();
        try{
            List<String[]> list = CsvUtil.csvReader(filePath, pageSize + 1);
            String[] fields = list.get(0);
            if (fields[0].startsWith(DataConstant.UTF8_BOM)) {
                fields[0] = fields[0].substring(1);
            }
            log.info(Arrays.toString(fields));
            for(int i=1;i<list.size();i++) {
                String[] data = list.get(i);
                log.info(Arrays.toString(data));
                if (Integer.valueOf(data.length).equals(Integer.valueOf(fields.length))) {
                    dataList.add(readValues(data,fields));
                }
            }
        } catch (Exception e) {
            log.info("getCsvData",e);
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
    /**
     * 计算文件hash值
     */
    public static String hashFile(File file) throws Exception {
        FileInputStream fis = null;
        String sha256 = null;
        try {
            fis = new FileInputStream(file);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte buffer[] = new byte[1024];
            int length = -1;
            while ((length = fis.read(buffer, 0, 1024)) != -1) {
                md.update(buffer, 0, length);
            }
            byte[] digest = md.digest();
            sha256 = byte2hexLower(digest);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("计算文件hash值错误");
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sha256;
    }

    private static String byte2hexLower(byte[] b) {
        String hs = "";
        String stmp = "";
        for (int i = 0; i < b.length; i++) {
            stmp = Integer.toHexString(b[i] & 0XFF);
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs;
    }

    public static String md5HashCode(File file) {
        try {
            InputStream fis = new FileInputStream(file);
            //拿到一个MD5转换器,如果想使用SHA-1或SHA-256，则传入SHA-1,SHA-256
            MessageDigest md = MessageDigest.getInstance("MD5");

            //分多次将一个文件读入，对于大型文件而言，比较推荐这种方式，占用内存比较少。
            byte[] buffer = new byte[1024];
            int length = -1;
            while ((length = fis.read(buffer, 0, 1024)) != -1) {
                md.update(buffer, 0, length);
            }
            fis.close();
            //转换并返回包含16个元素字节数组,返回数值范围为-128到127
            byte[] md5Bytes  = md.digest();
            BigInteger bigInt = new BigInteger(1, md5Bytes);//1代表绝对值
            return bigInt.toString(16);//转换为16进制
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }


    public static List<String> getFileContentData(String filePath,Integer severalLines){
        List<String> list = new ArrayList<>();
        try {
            File file = new File(filePath);
            if (!file.exists()){
                log.info("{}-不存在",filePath);
                return list;
            }
            Scanner sc = new Scanner(file);
            while (sc.hasNext()) {
                list.add(sc.nextLine());
                if (severalLines!=null&&severalLines>0){
                    if (list.size()==severalLines) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            log.info("{}-IOException: {}",filePath,e.getMessage());
        }
        return list;
    }



    public static void main(String[] args) throws Exception {
//        System.out.println(FileUtil.getCsvData("E://x.csv",0,50));
//        File file = new File("/Users/zhongziqian/Downloads/7a1ff80f-9e00-48f1-9a46-338fdc5f3aa9.csv");
        long start = System.currentTimeMillis();
        File file = new File("/Users/zhongziqian/Downloads/c0e309f0-0924-4b8d-84e1-947a1617aafc.csv");
        System.out.println(hashFile(file));
        long end1 = System.currentTimeMillis();
        System.out.println(end1 - start);
        System.out.println(md5HashCode(file));
        long end2 = System.currentTimeMillis();
        System.out.println(end2 - start);
    }
}
