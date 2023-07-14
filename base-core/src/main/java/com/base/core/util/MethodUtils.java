package com.base.core.util;

import com.base.core.entity.LocalMethodDto;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MethodUtils {

    public static List<LocalMethodDto> getLocalMethod(String classpath) throws ClassNotFoundException {
        Class<?> _class = Class.forName(classpath);
        return getLocalMethod(_class);
    }

    public static List<LocalMethodDto> getLocalMethod(Class<?> _class){
        List<LocalMethodDto> list = new ArrayList<>();
        for(Method method: _class.getMethods()){
            LocalMethodDto dto = new LocalMethodDto();
            dto.setClassPath(_class.getName());
            dto.setMethodName(method.getName());
            Class<?>[] parameterTypes = method.getParameterTypes();
            String[] paramsTypes = new String[parameterTypes.length];
            for(int i = 0; i < parameterTypes.length; i++){
                paramsTypes[i] = parameterTypes[i].getName();
            }
            dto.setParamsType(paramsTypes);
            dto.setReturnType(method.getReturnType().getName());
            list.add(dto);
        }
        return list;
    }


    public static List<LocalMethodDto> scanPackage(String packagePath){
        Set<Class<?>> classes =  ClassLoaderMapperUtil.scanPackage(packagePath, null);
        List<LocalMethodDto> list = new ArrayList<>();
        classes.forEach(
                item->{
                    list.addAll(getLocalMethod(item));
                }
        );
        return list;
    }

    public static void main(String[] args) {
        System.out.println(scanPackage("com.base.rpc.api"));
    }
}
