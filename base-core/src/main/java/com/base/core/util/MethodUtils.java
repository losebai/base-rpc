package com.base.core.util;

import com.base.core.entity.LocalMethodDto;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MethodUtils {

    public static List<LocalMethodDto> getLocalMethod(String classpath) throws ClassNotFoundException {
        Class<?> _class = Class.forName(classpath);
        List<LocalMethodDto> list = new ArrayList<>();
        for(Method method: _class.getMethods()){
            LocalMethodDto dto = new LocalMethodDto();
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



}
