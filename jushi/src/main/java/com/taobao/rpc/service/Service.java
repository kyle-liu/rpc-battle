package com.taobao.rpc.service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/** @author <a href="mailto:jushi@taobao.com">jushi<a> */
public class Service {

    public static <T> List<Service> services(Class<T> exporter, T serviceProvider) {
        checkArgument(exporter.isInterface(), exporter + " should be a interface.");
        checkNotNull(serviceProvider, "ServcieProvider should not be null");

        ArrayList<Service> list = new ArrayList<Service>();
        for (Method method : exporter.getMethods()) {
            list.add(new Service(method, serviceProvider));
        }
        return list;
    }

    private final Method method;
    private final Object service;

    Service(Method method, Object service) {
        this.method = method;
        this.service = service;
    }

    public Object invoke(Object[] args) throws Exception {
        return method.invoke(service, args);
    }

    public String name() {
        return method.toString();
    }

    public String belongs() {
        return method.getDeclaringClass().toString();
    }

    @Override
    public String toString() {
        return "Service{method=" + method + ", service=" + service + '}';
    }
}
