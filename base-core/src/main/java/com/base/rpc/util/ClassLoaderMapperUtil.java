package com.base.rpc.util;


import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 类装入器mapper
 *
 * @author bai
 * @date 2023/03/20
 */
public class ClassLoaderMapperUtil {

    /**
     * 基础数据类型
     */
    private static final Map<String, Class<?>> primitiveClassPoolPool = new ConcurrentHashMap<>();
    static {
        try {
            addClassPackage("com.item", false);
        } catch (URISyntaxException | ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<?> addClass(Class<?> _class){
        return primitiveClassPoolPool.put(_class.getSimpleName(), _class);
    }

    public static Class<?> addClass(String classPath) throws ClassNotFoundException {
        Class<?> _class = Class.forName(classPath);
        return addClass(_class);
    }

    public static int addClass(File file, int offset, int len) throws ClassNotFoundException {
        if (file == null || !file.exists() || !file.isDirectory()){
            return -1;
        }
        String[] classfileName = file.list();
        int i = 0 , l = 0;
        assert classfileName != null;
        for (String _class: classfileName){
            if (offset == 0 & len == 0 || (offset >=i && len > l)){
                l += addClass(_class) != null ? 1 : 0;
            }
            i++;
        }
        return i;
    }

    public static int addClassPackage(String packagePath, boolean recursion) throws URISyntaxException,
            ClassNotFoundException,
            IOException {
        packagePath = packagePath.replaceAll("\\.", "/");
        Enumeration<URL> dirs = Thread.currentThread().getContextClassLoader().getResources(packagePath);
        int num = 0;
        while(dirs.hasMoreElements()) {
            URL url = dirs.nextElement() ;
            if(url.getProtocol().equals("file")) {
                List<File> classes = new ArrayList<File>();
                // 递归 变量路径下面所有的 class文件
                if (recursion){
                    listFiles(new File(url.getFile()),classes);
                }else {
                    classes.add(new File(url.getFile()));
                }
                num += loaderClasses(classes, packagePath);
            }
        }
       return num;
    }

    private static int loaderClasses(List<File> classes,String scan) throws ClassNotFoundException {
        for(File file : classes) {
            String fPath = file.getAbsolutePath().replaceAll("\\\\","/") ;
            String packageName = fPath.substring(fPath.lastIndexOf(scan));
            packageName = packageName.replace(".class","").replaceAll("/", ".");
            addClass(packageName);
        }
        return classes.size();

    }

    private static void listFiles(File dir, List<File> fileList) {
        if (dir.isDirectory()) {
            for (File f : Objects.requireNonNull(dir.listFiles())) {
                listFiles(f, fileList);
            }
        } else {
            if(dir.getName().endsWith(".class")) {
                fileList.add(dir);
            }
        }
    }

    public static Class<?> getClass(String key){
        return primitiveClassPoolPool.get(key);
    }

    public static void main(String[] args) {
        System.out.println(primitiveClassPoolPool);
    }
}
