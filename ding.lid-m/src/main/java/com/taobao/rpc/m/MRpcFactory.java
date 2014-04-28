package com.taobao.rpc.m;

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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class MRpcFactory implements RpcFactory {

    // =====================
    // Thread Config
    // =====================

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    private static final int CPU_THREADS = CPU_COUNT  + 1;

    private static final int FEWER_THREADS = 2;

    // =====================
    // decide whether compress
    // =====================

    private final static String rate_value = System.getProperty("COMPRESS_RATE", "0");
    private final static int rate = Integer.parseInt(rate_value);
    static {
        System.out.println("compress rate: " + rate + "%");
    }

    private static final AtomicInteger counter = new AtomicInteger();

    private static boolean skipCompress() {
        return rate <= 0 || counter.getAndIncrement() % 100 >= rate - 1;
    }

    // =====================
    // Serialization Util
    // =====================

    static final int FLAG_ZIP = 0x01;
    static final int FLAG_MM = 0x02;
    static final int FLAG_RESP = 0x04;

    static void objectToBytes0(Object obj, OutputStream os, boolean skipCompress) throws IOException {

        int flag = 0;
        if(!skipCompress) flag = flag | FLAG_ZIP;
        if(obj instanceof MultiMessage) flag = flag | FLAG_MM;
        boolean isResp = obj instanceof Response;
        if(isResp) flag = flag | FLAG_RESP;


        os.write(flag);
        if(isResp) {
            Response response = (Response) obj;
            os.write(Bytes.int2bytes(response.id()));
        }

        if(!skipCompress) {
            os = new GZIPOutputStream(os, 1 << 16);
        }

        Output output = new Output(os);
        if(isResp) {
            Response response = (Response) obj;
            kryos.get().writeClassAndObject(output, response.result());
        }
        else
            kryos.get().writeObject(output, obj);
        output.flush();

        if(!skipCompress) {
            os.flush();
            ((GZIPOutputStream)os).finish();
        }
    }

    private static byte[] objectToBytes(Object obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        objectToBytes0(obj, baos, skipCompress());
        return baos.toByteArray();
    }

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

    private static ThreadLocal<com.esotericsoftware.kryo.Kryo> kryos = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            return kryo();
        }
    };

    static Object bytes2Object(byte[] bytes, Class<?> type) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

        int flag = bais.read();
        InputStream is = bais;
        if((flag & FLAG_ZIP) > 0) {
            is = new GZIPInputStream(bais, 1 << 16);
        }
        if((flag & FLAG_MM) > 0) {
            type = MultiMessage.class;
        }
        if((flag & FLAG_RESP) > 0) {
            byte[] id = new byte[4];
            is.read(id);
            Input input = new Input(is);
            return new Response(Bytes.bytes2int(id), kryos.get().readClassAndObject(input));
        }
        return kryos.get().readObject(new Input(is), type);
    }

    // =====================
    // Encoder & Decoder
    // =====================

    private static class MultiMessage {
        public MultiMessage() {
           messages = new ArrayList<Object>();
        }

        public MultiMessage(List<Object> messages) {
            this.messages = messages;
        }

        public final List<Object> messages;
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

            objectToBytes0(msg, bout, skipCompress());

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

    static ThreadPoolExecutor serverReceiveExecutor = new ThreadPoolExecutor(CPU_THREADS, CPU_THREADS, 2000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

    static ServerBootstrap serverBootstrap;

    public static class ServerHandler extends ChannelInboundMessageHandlerAdapter<Object> {
        final List<Invoker> invokers;

        ServerHandler(List<Invoker> invokers) {
            this.invokers = invokers;
        }

        void proccessRequest(ChannelHandlerContext ctx, Request request) throws IOException {
            Object result = null;
            try {
                result = handle(request);
            } catch (Exception e) {
                result = e; // TODO 异常情况没有处理！
            }
            ctx.write(objectToBytes(new Response(request.id(), result)));
        }

        @Override
        public void messageReceived(final ChannelHandlerContext ctx, final Object msg) throws Exception {
            serverReceiveExecutor.submit(new Runnable() {
                public void run() {
                    try {
                        Object decoded = bytes2Object((byte[]) msg, Request.class);
                        if(decoded instanceof Request) {
                           proccessRequest(ctx, (Request) decoded);
                        }
                        else if(decoded instanceof MultiMessage) {
                            MultiMessage mm = (MultiMessage) decoded;
                            for(Object m : mm.messages) {
                                proccessRequest(ctx, (Request) m); // TODO 可以考虑派发线程！不能在同一线程中序列化地处理多个Biz请求
                            }
                        }
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
                            ch.pipeline().addLast(new DefaultEventExecutorGroup(FEWER_THREADS),
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

    static final int clientBatchSenderCount = CPU_COUNT;
    static List<BlockingQueue<DataAndFuture>> clientWriteBatchQueue = new ArrayList<BlockingQueue<DataAndFuture>>();
    static  {
        for(int i = 0; i < clientBatchSenderCount; ++i) {
            clientWriteBatchQueue.add(new ArrayBlockingQueue<DataAndFuture>(1024));
        }
    }
    static final ThreadPoolExecutor clientBatchSendExecutor = new ThreadPoolExecutor(clientBatchSenderCount, clientBatchSenderCount, 2000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

    static void configBatchSendExecutor(final List<BlockingQueue<DataAndFuture>> clientWriteBatchQueue, ThreadPoolExecutor batchSendExecutor) {
        for(int i = 0; i < batchSendExecutor.getCorePoolSize(); ++i) {
            final int idx = i;
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    while(true) {
                        long firstPoll = 0;
                        List<DataAndFuture> polls = new ArrayList<DataAndFuture>();

                        while(true) {
                            DataAndFuture poll = null;
                            try {
                                poll = clientWriteBatchQueue.get(idx).poll(1000, TimeUnit.MILLISECONDS);
                            } catch (InterruptedException e) {
                                // ignore
                            }

                            if(poll != null) {
                                if(firstPoll == 0) firstPoll = System.currentTimeMillis();
                                polls.add(poll);
                            }

                            if(firstPoll > 0 && System.currentTimeMillis() - firstPoll > 1000 || polls.size() >= 4) {
                                final List<DataAndFuture> finalPolls = polls;
                                final Object write;
                                if(finalPolls.size() > 1) {
                                    MultiMessage mm = new MultiMessage();
                                    for(DataAndFuture p : polls) {
                                        mm.messages.add(p.data);
                                    }
                                    write = mm;
                                }
                                else {
                                    write = finalPolls.get(0).data;
                                }

                                try {
                                    channel.write(objectToBytes(write)).addListener(new ChannelFutureListener() {
                                        @Override
                                        public void operationComplete(ChannelFuture f) throws Exception {
                                            if (f.isSuccess()) return;

                                            for (DataAndFuture p : finalPolls) {
                                                if (f.isCancelled())
                                                    p.future.done(new RpcException("Cancelled request"));
                                                else
                                                    p.future.done(new RpcException(f.cause()));
                                            }
                                        }
                                    });
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                            }
                        }
                    }
                }
            };
            batchSendExecutor.submit(r);
        }
    }

    private static void matchResponse(Object response, int id) {
        ResponseFuture future = ResponseFuture.findResponseFuture(id);
        if (future == null) return;
        future.done(response);
    }

    public class ClientHandler extends ChannelInboundMessageHandlerAdapter<Object> {
        @Override
        public void messageReceived(ChannelHandlerContext ctx, final Object msg) throws Exception {
            byte[] data = (byte[]) msg;
            assert (data[0] | FLAG_RESP) > 0;

            int id = Bytes.bytes2int(data, 1);
            matchResponse(data, id);
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

    public static class DataAndFuture {
        public DataAndFuture(Object data, ResponseFuture future) {
            this.data = data;
            this.future = future;
        }
        public Object data;
        public ResponseFuture future;
    }

    static final AtomicInteger clietThreadIdxCount = new AtomicInteger();

    static ThreadLocal<Integer> clietThreadIdx = new ThreadLocal<Integer>() {
        @Override
        protected Integer initialValue() {
            return clietThreadIdxCount.getAndIncrement() % clientBatchSenderCount;
        }
    };

    @SuppressWarnings("unchecked")
    public <T> T proxy(final Class<T> type) {
         return (T)Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                final Request request = new Request(invokerId(method),args);
                final ResponseFuture future = ResponseFuture.createResponseFuture(request.id());

                DataAndFuture dataAndListener = new DataAndFuture(request, future);
                clientWriteBatchQueue.get(clietThreadIdx.get()).add(dataAndListener);

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

    static volatile Channel channel;

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
                                    new DefaultEventExecutorGroup(FEWER_THREADS),
                                    new Encoder(),
                                    new Decoder(),
                                    new ClientHandler());
                        }
                    });
            channel = clientBootstrap.connect().sync().channel();
            configBatchSendExecutor(clientWriteBatchQueue, clientBatchSendExecutor);

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
        return "ding.lid-m";
    }
}
