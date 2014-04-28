package com.taobao.rpc.service;

/** @author <a href="mailto:jushi@taobao.com">jushi<a> */
public class ServiceIndex {
    private final int index;
    private final String name;

    public ServiceIndex(int index, String name) {
        this.index = index;
        this.name = name;
    }

    public int index() {
        return index;
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return "ServiceIndex{index=" + index + ", name='" + name + "'}";
    }
}
