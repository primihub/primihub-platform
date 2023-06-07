package com.primihub.biz.util;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Slf4j
public class ZipUtils {

    public static void pathFileTOZipRegularFile(String path, String zipPath, Set<String> appointFileNames) {
        File sourceFile = new File(path);
        File targetZipFile = new File(zipPath);
        if (!targetZipFile.getParentFile().exists()) {
            targetZipFile.getParentFile().mkdir();
        }
        try {
            FileOutputStream outputStream = new FileOutputStream(targetZipFile);
            ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
            if (sourceFile.isDirectory()) {
                File[] files = sourceFile.listFiles();
                boolean isExclude = appointFileNames!=null;
                if (files.length != 0) {
                    for (File file : files) {
                        if (isExclude&&!appointFileNames.contains(file.getName())) {
                            continue;
                        }
                        executeToZip(zipOutputStream, file);
                    }
                }
            } else {
                executeToZip(zipOutputStream, sourceFile);
            }
            zipOutputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 压缩文件到指定路径---单个文件、多个文件适用
     *
     * @param path    源文件路径
     * @param zipPath 目标路径
     */
    public static void pathFileTOZipFile(String path, String zipPath, Set<String> excludeFileNames) {
        File sourceFile = new File(path);
        File targetZipFile = new File(zipPath);
        if (!targetZipFile.getParentFile().exists()) {
            targetZipFile.getParentFile().mkdir();
        }
        try {
            FileOutputStream outputStream = new FileOutputStream(targetZipFile);
            ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
            if (sourceFile.isDirectory()) {
                File[] files = sourceFile.listFiles();
                boolean isExclude = excludeFileNames!=null;
                if (files.length != 0) {
                    for (File file : files) {
                        if (isExclude&&excludeFileNames.contains(file.getName())) {
                            continue;
                        }
                        executeToZip(zipOutputStream, file);
                    }
                }
            } else {
                executeToZip(zipOutputStream, sourceFile);
            }
            zipOutputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 执行单个文件压缩---可以作为压缩方法单独使用
     *
     * @param zipOutputStream 目标Zip输出流
     * @param sourceFile      源文件
     */
    public static void executeToZip(ZipOutputStream zipOutputStream, File sourceFile) throws IOException {
        FileInputStream inputStream = null;
        try {
            ZipEntry zipEntry = new ZipEntry(sourceFile.getName());
            zipOutputStream.putNextEntry(zipEntry);
            inputStream = new FileInputStream(sourceFile);
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buffer)) >= 0) {
                zipOutputStream.write(buffer, 0, len);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            inputStream.close();
            log.info("文件：{}压缩成功啦！",sourceFile.getAbsolutePath());
        }
    }
}
