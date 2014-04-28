package com.taobao.rpc.service;

import com.google.common.base.Function;
import com.taobao.rpc.api.RpcException;
import com.taobao.rpc.codec.Codec;
import com.taobao.rpc.codec.CodecFactory;
import com.taobao.rpc.handle.BufferHandle;
import com.taobao.rpc.handle.Handler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import java.util.Map;
import java.util.concurrent.*;

/** @author <a href="mailto:jushi@taobao.com">jushi<a> */
public class Invoker extends Handler {
    private final Map<Long, Promise> promises;
    private final long timeout;

    public Invoker(CodecFactory factory, long timeout) {
        super(factory);
        this.timeout = timeout;
        promises = new ConcurrentHashMap<Long, Promise>();
    }

    @Override
    protected void receive(ByteBuf frame, BufferHandle bufferHandle, ChannelHandlerContext ctx) {
        bufferHandle.read(frame, new Function<Codec, Void>() {
            @Override
            public Void apply(Codec codec) {
                Response response = codec.decode(Response.class);
                Promise promise = promises.get(response.id());
                if (promise != null) promise.done(response.result());
                return null;
            }
        });
    }

    public Object invoke(final Channel channel, int method, Object[] args) {
        final Request request = new Request(method, args);
        final Promise promise = new Promise();
        promises.put(request.id(), promise);

        try {
            return send(request, channel, promise);
        } catch (RpcException e) {
            throw e;
        } catch (Exception e) {
            throw new RpcException(e);
        } finally {
            promises.remove(request.id());
        }
    }

    private Object send(final Request request, final Channel channel, final Promise promise)
            throws InterruptedException, TimeoutException {

        final long start = System.currentTimeMillis();

        BufferHandle executor = acquireHandle(timeout, TimeUnit.MILLISECONDS);
        if (executor == null) throw new RpcException(new TimeoutException());

        executor.write(new Function<Codec, ChannelFuture>() {
            @Override
            public ChannelFuture apply(Codec codec) {
                return channel.write(codec.encode(request)).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) return;

                        if (future.isCancelled())
                            promise.done(new RpcException("Cancelled request"));
                        else
                            promise.done(new RpcException(future.cause()));
                    }
                });
            }
        });

        long leftTimeout = timeout - (System.currentTimeMillis() - start);
        return promise.get(leftTimeout, TimeUnit.MILLISECONDS);
    }

    private static class Promise {
        private final BlockingQueue<Object> queue = new SynchronousQueue<Object>();

        public void done(Object result) {
            queue.offer(result);
        }

        public Object get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
            Object result = queue.poll(timeout, unit);
            if (result == null) throw new TimeoutException();
            if (result instanceof RpcException) throw (RpcException) result;
            return result;
        }
    }
}
