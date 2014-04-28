package com.taobao.rpc.s;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.taobao.rpc.api.RpcException;
import com.taobao.rpc.api.RpcFactory;
import com.taobao.rpc.benchmark.dataobject.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SRpcFactory implements RpcFactory {
    private static final int WORKER_THREADS = Runtime.getRuntime().availableProcessors() + 1;

    private static final int BIZ_THREADS = Runtime.getRuntime().availableProcessors() * 2;

    static void objectToBytes0(Object obj, OutputStream os) throws IOException {
        Output output = new Output(os);
        kryos.get().writeObject(output, obj);
        output.flush();
    }

    private static byte[] objectToBytes(Object obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        objectToBytes0(obj, baos);
        return baos.toByteArray();
    }

    private static Object bytes2Object(byte[] bytes, Class<?> type) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        return kryos.get().readObject(new Input(bais), type);
    }

    // =====================
    // Encoder & Decoder
    // =====================

    private static ThreadLocal<com.esotericsoftware.kryo.Kryo> kryos = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            return kryo();
        }
    };

    private static final Class[] kryoClasses = {Request.class, Response.class, ArrayList.class,
            Person.class};
    private static List<Class<?>> clazzList = new ArrayList<Class<?>>();

    private static void addClass(Class<?> clazz, List<Class<?>> clazzList) {
        if(clazz.isPrimitive() || clazzList.contains(clazz)) return;

        clazzList.add(clazz);
        System.out.println(clazz);

        if(clazz.getName().startsWith("java.")) return;
        for(Field field : clazz.getDeclaredFields()) {
            addClass(field.getType(), clazzList);
        }
    }


    static {
        for(Class<?> clazz : kryoClasses) {
            addClass(clazz, clazzList);
        }
    }

    static com.esotericsoftware.kryo.Kryo kryo() {
        final com.esotericsoftware.kryo.Kryo kryo = new com.esotericsoftware.kryo.Kryo();

        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
        for (Class clazz : clazzList) {
            kryo.register(clazz);
        }
        return kryo;
    }

    private static class Encoder extends MessageToByteEncoder<Object> {
        private static final byte[] LENGTH_PLACEHOLDER = new byte[4];

        @Override
        public void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
            if(msg instanceof byte[]) {
                byte[] bytes = (byte[]) msg;
                out.writeInt(bytes.length);
                out.writeBytes(bytes);
                return;
            }

            final int startIdx = out.writerIndex();

            ByteBufOutputStream bout = new ByteBufOutputStream(out);
            bout.write(LENGTH_PLACEHOLDER);

            objectToBytes0(msg, bout);

            int endIdx = out.writerIndex();
            out.setInt(startIdx, endIdx - startIdx - 4);
        }
    }

    private static class Decoder extends LengthFieldBasedFrameDecoder {
        public Decoder() {
            super(1 << 16, 0, 4, 0, 4);
        }

        @Override
        public Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
            ByteBuf frame = (ByteBuf) super.decode(ctx, in);
            if (frame == null) {
                return null;
            }
            byte[] buf = frame.array();
            byte[] copy = new byte[buf.length];
            System.arraycopy(buf, 0, copy, 0, buf.length);
            return copy;
        }
    }

    // ====================
    // Server
    // ====================

    static ThreadPoolExecutor serverExecutor = new ThreadPoolExecutor(BIZ_THREADS, BIZ_THREADS, 2000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

    ServerBootstrap serverBootstrap;

    public static class ServerHandler extends ChannelInboundMessageHandlerAdapter<Object> {
        final List<Invoker> invokers;

        ServerHandler(List<Invoker> invokers) {
            this.invokers = invokers;
        }

        @Override
        public void messageReceived(final ChannelHandlerContext ctx, final Object msg) throws Exception {
            serverExecutor.submit(new Runnable() {
                public void run() {
                    try {
                        Request request = (Request) bytes2Object((byte[]) msg, Request.class);
                        Object result;
                        try {
                            result = handle(request);
                        } catch (Exception e) {
                            result = e; // FIXME 异常情况没有处理！
                        }
                        ctx.write(objectToBytes(new Response(request.id(), result)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        private Object handle(Request request) {
            try {
                return invokers.get(request.invokerId()).invoke(request.args());
            } catch (Exception e) {
                return new RpcException(e);
            }
        }
    }

    private static <T> List<Invoker> buildInvokers(Class<T> type, T service) {
        ArrayList<Invoker> list = new ArrayList<Invoker>();
        for (Method method : type.getMethods()) {
            list.add(new Invoker(method, service));
        }
        return list;
    }

    private static volatile List<Invoker> invokers = new ArrayList<Invoker>();

    @Override
    public <T> void export(final Class<T> type, final T serviceObject) {
        try {
            List<Invoker> tmpInvokers = buildInvokers(InvokerList.class, new InvokerList() {
                @Override
                public List<String> get() {
                    ArrayList<String> list = new ArrayList<String>(invokers.size());
                    for (Invoker invoker : invokers) {
                        list.add(invoker.method().toString());
                    }
                    return list;
                }
            });
            tmpInvokers.addAll(buildInvokers(type, serviceObject));
            invokers = tmpInvokers;

            serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(new NioEventLoopGroup(1), new NioEventLoopGroup())
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 65536)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .localAddress(new InetSocketAddress(DEFAULT_PORT))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new DefaultEventExecutorGroup(WORKER_THREADS),
                                    new Encoder(),
                                    new Decoder(),
                                    new ServerHandler(invokers));
                        }
                    });

            serverBootstrap.bind().sync();
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    // ====================
    // Client
    // ====================

    static ThreadPoolExecutor clientExecutor = new ThreadPoolExecutor(BIZ_THREADS, BIZ_THREADS, 2000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

    public class ClientHandler extends ChannelInboundMessageHandlerAdapter<Object> {
        @Override
        public void messageReceived(ChannelHandlerContext ctx, final Object msg) throws Exception {
            clientExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        final Response response = (Response) bytes2Object((byte[])msg, Response.class);
                        ResponseFuture future = ResponseFuture.findResponseFuture(response.id());
                        if (future == null) return;
                        future.done(response.result());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private volatile Map<String, Integer> invokerIds;

    private Integer invokerId(Method method) {
        if (method.getDeclaringClass().equals(InvokerList.class)) {
            return 0;
        }
        Integer invokerId = invokerIds.get(method.toString());
        if (invokerId == null) {
            throw new RpcException(new UnsupportedOperationException(method.toString()));
        }
        return invokerId;
    }

    @SuppressWarnings("unchecked")
    public <T> T proxy(final Class<T> type) {
         return (T)Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                final Request request = new Request(invokerId(method),args);
                final ResponseFuture future = ResponseFuture.createResponseFuture(request.id());

                channel.write(objectToBytes(request)).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture f) throws Exception {
                        if (f.isSuccess()) return;

                        if (f.isCancelled())
                            future.done(new RpcException("Cancelled request"));
                        else
                            future.done(new RpcException(f.cause()));
                    }
                });

                Object result;
                try {
                    result = future.get(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    throw new RpcException(e);
                }
                if (result instanceof RpcException) {
                    throw (RpcException) result;
                }
                return result;
            }
        });
    }

    volatile Channel channel;

    @Override
    public <T> T getReference(final Class<T> type, String ip) {
        try {
            Bootstrap clientBootstrap = new Bootstrap();
            clientBootstrap.group(new NioEventLoopGroup(1))
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .remoteAddress(new InetSocketAddress(ip, DEFAULT_PORT))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(
                                    new DefaultEventExecutorGroup(WORKER_THREADS),
                                    new Encoder(),
                                    new Decoder(),
                                    new ClientHandler());
                        }
                    });
            channel = clientBootstrap.connect().sync().channel();

            List<String> invokers = proxy(InvokerList.class).get();

            Map<String, Integer> tmp = new HashMap<String, Integer>();
            for (int i = 0; i < invokers.size(); i++) {
                tmp.put(invokers.get(i), i);
            }
            invokerIds = tmp;

            return proxy(type);
        }
        catch (Exception e) {
            throw new RpcException(e);
        }
    }

    @Override
    public int getClientThreads() {
        return Runtime.getRuntime().availableProcessors() * 8;
    }

    @Override
    public String getAuthorId() {
        return "ding.lid(s)";
    }
}
