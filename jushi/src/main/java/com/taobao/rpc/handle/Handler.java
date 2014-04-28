package com.taobao.rpc.handle;

import com.google.common.base.Function;
import com.taobao.rpc.codec.CodecFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundByteHandlerAdapter;
import io.netty.channel.DefaultEventExecutorGroup;
import io.netty.channel.EventExecutorGroup;
import java.util.concurrent.TimeUnit;

import static com.taobao.rpc.codec.LengthFieldBaseFrameCodecUtil.extractFrame;

/** @author <a href="mailto:jushi@taobao.com">jushi<a> */
public abstract class Handler extends ChannelInboundByteHandlerAdapter {
    private static final int THREADS = Runtime.getRuntime().availableProcessors();

    private final BufferHandleExeuctor exeuctor;
    private final EventExecutorGroup group;

    protected Handler(CodecFactory factory) {
        Integer threads = Integer.getInteger("nk.eg", 2);
        Integer handles = Integer.getInteger("nk.cg", THREADS * 8);
        group = new DefaultEventExecutorGroup(threads);
        exeuctor = new BufferHandleExeuctor(handles, group, factory);
    }

    @Override
    public void inboundBufferUpdated(final ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        extractFrame(in, new Function<ByteBuf, Boolean>() {
            @Override
            public Boolean apply(final ByteBuf frame) {
                final BufferHandle BufferHandle = exeuctor.acquireHandle();
                if (BufferHandle == null) return false;
                receive(frame, BufferHandle, ctx);
                return true;
            }
        });
    }

    public void shutdown() {
        group.shutdown();
    }

    protected final BufferHandle acquireHandle(long timeout, TimeUnit unit) throws InterruptedException {
        return exeuctor.acquireHandle(timeout, unit);
    }

    protected abstract void receive(ByteBuf frame, BufferHandle bufferHandle, ChannelHandlerContext ctx);
}
