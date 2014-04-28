package com.taobao.rpc.s;

import java.io.Serializable;

/** @author <a href="mailto:jushi@taobao.com">jushi<a> */
public class Response implements Serializable {
    private final int id;
    private final Object result;

    public Response(int id, Object result) {
        this.id = id;
        this.result = result;
    }

    public int id() {
        return id;
    }

    public Object result() {
        return result;
    }

    @Override
    public String toString() {
        return "Response{" +
                "id=" + id +
                ", result=" + result +
                '}';
    }
}
