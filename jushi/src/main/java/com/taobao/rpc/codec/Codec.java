package com.taobao.rpc.codec;

import io.netty.buffer.ByteBuf;

/** @author <a href="mailto:jushi@taobao.com">jushi<a> */
public interface Codec {
    <T> ByteBuf encode(T object);

    <T> T decode(Class<T> type);

    void prepare(ByteBuf in);
}
