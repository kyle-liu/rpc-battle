package com.taobao.rpc.m;

import java.lang.reflect.Method;

/** @author <a href="mailto:jushi@taobao.com">jushi<a> */
public class Invoker {
    private final Method method;
    private final Object service;

    Invoker(Method method, Object service) {
        this.method = method;
        this.service = service;
    }

    Object invoke(Object[] args) throws Exception {
        return method.invoke(service, args);
    }

    Method method() {
        return method;
    }

    @Override
    public String toString() {
        return "Invoker{" +
                "method=" + method +
                ", service=" + service +
                '}';
    }
}
