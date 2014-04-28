package com.taobao.rpc.service;

/** @author <a href="mailto:jushi@taobao.com">jushi<a> */
public class Response {
    private final long id;
    private final Object result;

    public Response(long id, Object result) {
        this.id = id;
        this.result = result;
    }

    public long id() {
        return id;
    }

    public Object result() {
        return result;
    }

    @Override
    public String toString() {
        return "Response{id=" + id + ", result=" + result + '}';
    }
}
