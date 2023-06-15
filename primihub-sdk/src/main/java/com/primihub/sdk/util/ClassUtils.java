package com.primihub.sdk.util;

import com.primihub.sdk.task.cache.CacheService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class ClassUtils {

    public static List<Class> getAllClassByInterface(Class c){
        List<Class> returnClassList = new ArrayList<Class>();
        if(c.isInterface()){
            String packageName = c.getPackage().getName();
            try {
                List<Class> allClass = getClasses(packageName);
                for(int i=0;i<allClass.size();i++){
                    if(c.isAssignableFrom(allClass.get(i))){
                        if(!c.equals(allClass.get(i))){
                            returnClassList.add(allClass.get(i));
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return returnClassList;
    }
    private static List<Class> getClasses(String packageName)
            throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread()
                .getContextClassLoader();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList<Class> classes = new ArrayList<Class>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes;
    }


    private static List<Class> findClasses(File directory, String packageName)
            throws ClassNotFoundException {
        List<Class> classes = new ArrayList<Class>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." +
                        file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' +
                        file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }

    public static void main(String[] args) {
        List<Class> allClassByInterface = ClassUtils.getAllClassByInterface(CacheService.class);
        for (Class aClass : allClassByInterface) {
            System.out.println(aClass.getSimpleName());
        }
    }
}
