package com.taobao.rpc.handle;

import com.google.common.base.Function;
import com.taobao.rpc.codec.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;

/** @author <a href="mailto:jushi@taobao.com">jushi<a> */
public interface BufferHandle {
    void write(Function<Codec, ChannelFuture> callback);

    void read(ByteBuf in, Function<Codec, Void> function);

    void readAndwrite(ByteBuf in, Function<Codec, ChannelFuture> function);
}
