package com.taobao.rpc.s;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/** @author <a href="mailto:jushi@taobao.com">jushi<a> */
public class Request implements Serializable {
    private static final AtomicInteger idGenerator = new AtomicInteger();

    private final int id;

    private final int invokerId;

    private final Object[] args;

    @Deprecated
    public Request(int id, int invokerId, Object[] args) {
        this.id = id;
        this.invokerId = invokerId;
        this.args = args;
    }

    public Request(int invokerId, Object[] args) {
        this.id = idGenerator.incrementAndGet();
        this.invokerId = invokerId;
        this.args = args;
    }

    public int id() {
        return id;
    }

    public int invokerId() {
        return invokerId;
    }

    public Object[] args() {
        return args;
    }
}
