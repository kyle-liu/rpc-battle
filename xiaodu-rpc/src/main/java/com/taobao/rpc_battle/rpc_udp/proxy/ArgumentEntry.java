
/**
 * xiaodu-rpc
 */
package com.taobao.rpc_battle.rpc_udp.proxy;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;


/**
 * @author xiaodu
 *
 * 下午1:24:09
 */
public class ArgumentEntry {


    private int index;
    private Type genericType;

    public ArgumentEntry() {
        this.index = -1;
        this.genericType = null;
    }

    public ArgumentEntry(ArgumentEntry e) {
        this.index = e.index;
        this.genericType = e.genericType;
    }

    public ArgumentEntry(int index, Type genericType) {
        this.index = index;
        this.genericType = genericType;
    }

    public int getIndex() {
        return index;
    }

    public Class<?> getType() {
        if (genericType instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) genericType)
                    .getRawType();
        } else {
            return (Class<?>) genericType;
        }
    }

    public String getJavaTypeName() {
        Class<?> type = getType();
        if (type.isArray()) {
            return arrayTypeToString(type);
        } else {
            return type.getName();
        }
    }

    public Type getGenericType() {
        return genericType;
    }


    static String arrayTypeToString(Class<?> type) {
        int dim = 1;
        Class<?> baseType = type.getComponentType();
        while (baseType.isArray()) {
            baseType = baseType.getComponentType();
            dim += 1;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(baseType.getName());
        for (int i = 0; i < dim; ++i) {
            sb.append("[]");
        }
        return sb.toString();
    }


}
