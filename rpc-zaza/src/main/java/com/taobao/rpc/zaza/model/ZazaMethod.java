package com.taobao.rpc.zaza.model;

import net.sf.cglib.reflect.FastMethod;

public class ZazaMethod implements Comparable<ZazaMethod> {
    private final FastMethod method;
    private final Object instance;
    private final long code;

    public FastMethod getMethod() {
        return method;
    }

    public long getCode() {
        return code;
    }

    public ZazaMethod(FastMethod method, long code, Object instance) {
        this.instance = instance;
        this.method = method;
        this.code = code;
    }

    public Object getInstance() {
        return instance;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (code ^ (code >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ZazaMethod other = (ZazaMethod) obj;
        if (code != other.code)
            return false;
        return true;
    }

    public int compareTo(ZazaMethod o) {
        return (int) (this.code - o.code);
    }

}
