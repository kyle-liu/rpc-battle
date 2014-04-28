package com.taobao.rpc.zaza.interfaces;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.LoggerFactory;

import com.taobao.rpc.zaza.ZazaRequest;
import com.taobao.rpc.zaza.ZazaResponse;
import com.taobao.rpc.zaza.model.ZazaMethodDataModel;
import com.taobao.rpc.zaza.serialization.KryoDeSerializer;
import com.taobao.rpc.zaza.serialization.KryoSerializer;
import com.taobao.rpc.zaza.util.ZazaConfigUtil;
import com.taobao.rpc.zaza.util.ZazaUtil;

public abstract class ZazaClient {
    protected final static org.slf4j.Logger logger = LoggerFactory.getLogger(ZazaClient.class);
    private static AtomicInteger indexOfClientThreadCounter = new AtomicInteger(0);

    private final static List<ArrayBlockingQueue<Object>> resultQueue = new ArrayList<ArrayBlockingQueue<Object>>(
            ZazaConfigUtil.getClentThreadNum());

    static {
        for (int i = 0; i <= ZazaConfigUtil.getClentThreadNum(); i++) {
            resultQueue.add(i, new ArrayBlockingQueue<Object>(1));
        }
    }

    private static final ThreadLocal<Byte> indexOfClientThreadLocal = new ThreadLocal<Byte>() {
        protected Byte initialValue() {
            return (byte) indexOfClientThreadCounter.getAndIncrement();
        }
    };

    public Object invokeSync(Class<?> classType, Method method, String[] argTypes, Object[] args, int timeout)
            throws Exception {
        byte[][] argTypeBytes = null;
        if (argTypes != null) {
            argTypeBytes = new byte[argTypes.length][];
            for (int i = 0; i < argTypes.length; i++) {
                Byte code = ZazaMethodDataModel.instance.getCodeOfClass(argTypes[i]);
                if (code != null) {
                    argTypeBytes[i] = new byte[] { code.byteValue() };
                } else {
                    argTypeBytes[i] = argTypes[i].getBytes();
                }
            }
        }
        byte[][] argBytes = new byte[args.length][];
        int i = 0;
        for (Object object : args) {
            argBytes[i++] = KryoSerializer.encode(object);
        }

        ZazaRequest wrapper = new ZazaRequest(ZazaMethodDataModel.instance.getCodeOfMethodAsClientSide(ZazaUtil
                .generateNameOfMethod(classType, method)), argBytes, argTypeBytes);
        return invokeSyncIntern(wrapper, timeout);
    }

    public Object invokeSyncIntern(ZazaRequest wrapper, int timeout) throws Exception {
        ZazaResponse responseWrapper = null;
        try {
            sendRequest(wrapper, timeout);
        } catch (Exception e) {
            throw e;
        }
        int index = getIndexOfThread();
        if (index < 0) {
            index += 256;
        }
        ArrayBlockingQueue<Object> responseQueue = resultQueue.get(index);
        Object result = responseQueue.take();
        if (result instanceof ZazaResponse) {
            responseWrapper = (ZazaResponse) result;
        } else if (result instanceof List) {
            @SuppressWarnings("unchecked")
            List<ZazaResponse> responseWrappers = (List<ZazaResponse>) result;
            for (ZazaResponse response : responseWrappers) {
                if (response.getRequestId() == wrapper.getRequestID()) {
                    responseWrapper = response;
                } else {
                    putResponse(response);
                }
            }
        } else {
            logger.error("only receive ResponseWrapper or List as response");
            throw new RuntimeException();
        }
        return KryoDeSerializer.decode(new String(responseWrapper.getResponseType()),
                responseWrapper.getResponseObject());
    }

    /**
     * receive response
     */
    public void putResponse(ZazaResponse wrapper) throws Exception {
        int index = wrapper.getRequestId();
        if (index < 0) {
            index += 256;
        }
        resultQueue.get(index).put(wrapper);
    }

    /**
     * receive responses
     */
    public void putResponses(List<ZazaResponse> wrappers) throws Exception {
        int index = wrappers.get(0).getRequestId();
        if (index < 0) {
            index += 256;
        }
        resultQueue.get(index).put(wrappers);
    }

    public static byte getIndexOfThread() {
        return indexOfClientThreadLocal.get();
    }

    abstract public void sendRequest(final ZazaRequest wrapper, final int timeout) throws Exception;
}
