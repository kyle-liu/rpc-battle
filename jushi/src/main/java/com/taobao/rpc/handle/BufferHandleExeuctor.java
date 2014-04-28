package com.taobao.rpc.handle;

import com.google.common.base.Function;
import com.taobao.rpc.codec.Codec;
import com.taobao.rpc.codec.CodecFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventExecutor;
import io.netty.channel.EventExecutorGroup;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/** @author <a href="mailto:jushi@taobao.com">jushi<a> */
public class BufferHandleExeuctor {
    private final BlockingQueue<Binding> idles;

    public BufferHandleExeuctor(int size, EventExecutorGroup group, CodecFactory factory) {
        idles = new ArrayBlockingQueue<Binding>(size);
        for (int i = 0; i < size; i++) {
            idles.offer(new Binding(factory.create(), group.next()));
        }
    }

    public BufferHandle acquireHandle() {
        return idles.poll();
    }

    public BufferHandle acquireHandle(long timeout, TimeUnit unit) throws InterruptedException {
        return idles.poll(timeout, unit);
    }

    // Bind Codec to EventExecutor for thread-safe.
    private class Binding implements BufferHandle {
        private final Codec codec;
        private final EventExecutor executor;

        private Binding(Codec codec, EventExecutor executor) {
            this.codec = codec;
            this.executor = executor;
        }

        @Override
        public void write(final Function<Codec, ChannelFuture> function) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        function.apply(codec).addListener(new ChannelFutureListener() {
                            @Override
                            public void operationComplete(ChannelFuture future) throws Exception {
                                idles.offer(Binding.this);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace(); // TODO log out
                        idles.offer(Binding.this);
                    }
                }
            });
        }

        @Override
        public void read(ByteBuf in, final Function<Codec, Void> function) {
            codec.prepare(in);
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        function.apply(codec);
                    } catch (Exception e) {
                        e.printStackTrace();  // TODO log out
                    } finally {
                        idles.offer(Binding.this);
                    }
                }
            });

        }

        @Override
        public void readAndwrite(ByteBuf in, final Function<Codec, ChannelFuture> function) {
            codec.prepare(in);
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        function.apply(codec).addListener(new ChannelFutureListener() {
                            @Override
                            public void operationComplete(ChannelFuture future) throws Exception {
                                idles.offer(Binding.this);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace(); // TODO log out
                        idles.offer(Binding.this);
                    }
                }
            });
        }
    }
}
