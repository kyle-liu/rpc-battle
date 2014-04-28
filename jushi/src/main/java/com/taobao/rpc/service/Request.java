package com.taobao.rpc.service;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

/** @author <a href="mailto:jushi@taobao.com">jushi<a> */
public class Request {
    private static final AtomicLong idGenerator = new AtomicLong();

    private final long id;
    private final int serviceIndex;
    private final Object[] args;

    public Request(int serviceIndex, Object[] args) {
        id = idGenerator.incrementAndGet();
        this.serviceIndex = serviceIndex;
        this.args = args;
    }

    public long id() {
        return id;
    }

    public int serviceIndex() {
        return serviceIndex;
    }

    public Object[] args() {
        return args;
    }

    @Override
    public String toString() {
        return "Request{id=" + id + ", serviceIndex=" + serviceIndex + ", args=" + (args == null ? null : Arrays.asList(args)) + '}';
    }
}
