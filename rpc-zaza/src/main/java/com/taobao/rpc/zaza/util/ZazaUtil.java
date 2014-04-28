package com.taobao.rpc.zaza.util;

import java.lang.reflect.Method;

public class ZazaUtil {
    public static long generateCodeOfMethod(Class<?> providerClass, Method method) {
        StringBuilder buider = new StringBuilder(method.getName());
        long classCode = providerClass.getName().hashCode();
        Class<?>[] paramTypes = method.getParameterTypes();
        for (Class<?> c : paramTypes) {
            buider.append(c.getName());
        }
        return classCode << 32 + buider.toString().hashCode();
    }

    public static String generateNameOfMethod(Class<?> providerClass, Method method) {
        StringBuilder buider = new StringBuilder(providerClass.getName()).append(method.getName());
        Class<?>[] paramTypes = method.getParameterTypes();
        for (Class<?> c : paramTypes) {
            buider.append(c.getName());
        }
        return buider.toString();
    }

    public static String[] getMethodParameter(Method method) {
        Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes.length == 0) {
            return null;
        }
        String[] result = new String[paramTypes.length];
        int i = 0;
        for (Class<?> c : paramTypes) {
            result[i++] = c.getName();
        }
        return result;
    }

    public static String[] getMethodParameter(Object[] parameters) {
        if (parameters.length == 0) {
            return null;
        }
        String[] result = new String[parameters.length];
        int i = 0;
        for (Object c : parameters) {
            result[i++] = c.getClass().getName();
        }
        return result;
    }
}
