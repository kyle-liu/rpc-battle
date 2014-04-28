package com.taobao.rpc.zaza.impl.grizzly;

import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.memory.Buffers;
import org.slf4j.LoggerFactory;

import com.taobao.rpc.zaza.ZazaRequest;
import com.taobao.rpc.zaza.ZazaResponse;

public class GrizzlyZazaProtocol {
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(GrizzlyZazaProtocol.class);

    private static final byte REQUEST = (byte) 0;
    private static final byte RESPONSE = (byte) 1;

    public static Buffer encode(Object message, FilterChainContext ctx) throws Exception {
        if (message instanceof ZazaRequest) {
            ZazaRequest wrapper = (ZazaRequest) message;
            int capacity = calculateCapacityOfRequest(wrapper);
            Buffer byteBuffer = Buffers.wrap(ctx.getMemoryManager(), new byte[capacity]);
            writeOneRequest(wrapper, byteBuffer);
            return byteBuffer;
        } else if (message instanceof ZazaResponse) {
            int capacity = ((ZazaResponse) message).getResponseObject().length
                    + ((ZazaResponse) message).getResponseType().length + 1 + 1 + 1 + 2;
            Buffer byteBuffer = Buffers.wrap(ctx.getMemoryManager(), new byte[capacity]);
            writeOneResponse(message, byteBuffer);
            return byteBuffer;
        } else {
            throw new Exception("only support send ZazaRequest and ZazaResponse");
        }
    }

    private static int calculateCapacityOfRequest(ZazaRequest wrapper) {
        int argsLen = 0;
        byte[][] requestArgTypeStrings = wrapper.getRequestTypes();
        byte[][] requestObjects = wrapper.getRequestObjects();
        if (requestArgTypeStrings != null) {
            for (byte[] requestArgType : requestArgTypeStrings) {
                argsLen += requestArgType.length;
            }
            for (byte[] requestArg : requestObjects) {
                argsLen += requestArg.length;
            }
            argsLen += requestObjects.length * (2 + 1);
        }
        int capacity = 1 + 1 + 1 + 1 + argsLen;
        return capacity;
    }

    private static void writeOneResponse(Object message, Buffer byteBuffer) {
        byteBuffer.put(RESPONSE);
        byteBuffer.put(((ZazaResponse) message).getRequestId());
        byteBuffer.put((byte) ((ZazaResponse) message).getResponseType().length);
        byteBuffer.putShort((short) ((ZazaResponse) message).getResponseObject().length);
        byteBuffer.put(((ZazaResponse) message).getResponseType());
        byteBuffer.put(((ZazaResponse) message).getResponseObject());
    }

    private static void writeOneRequest(ZazaRequest message, Buffer byteBuffer) {
        byteBuffer.put(REQUEST);
        byteBuffer.put(message.getRequestID());
        byteBuffer.put(message.getMethodCode());
        byte[][] requestArgTypeStrings = message.getRequestTypes();
        byte[][] requestObjects = message.getRequestObjects();
        byteBuffer.put((byte) (requestObjects == null ? 0 : requestObjects.length));
        if (requestArgTypeStrings != null) {
            for (byte[] requestArgType : requestArgTypeStrings) {
                byteBuffer.put((byte) requestArgType.length);
            }

            for (byte[] requestArg : requestObjects) {
                byteBuffer.putShort((short) requestArg.length);
            }

            for (byte[] requestArgType : requestArgTypeStrings) {
                byteBuffer.put(requestArgType);
            }

            for (byte[] requestArg : requestObjects) {
                byteBuffer.put(requestArg);
            }
        }
    }

    public static Object decode(Buffer wrapper) {
        final int originPos = wrapper.position();
        if (wrapper.remaining() < 1) {
            wrapper.position(originPos);
            return null;
        }
        byte type = wrapper.get();
        if (type == REQUEST) {
            if (wrapper.remaining() < 3) {
                wrapper.position(originPos);
                return null;
            }
            byte requestId = wrapper.get();
            byte methodCode = wrapper.get();
            byte argsCount = wrapper.get();

            int argInfosLen = argsCount * (2 + 1);
            if (wrapper.remaining() < argInfosLen) {
                wrapper.position(originPos);
                return null;
            }
            int[] argsTypeLen = new int[argsCount];
            int expectedLen = 0;
            for (int i = 0; i < argsCount; i++) {
                argsTypeLen[i] = wrapper.get();
                expectedLen += argsTypeLen[i];
            }
            int[] argsLen = new int[argsCount];
            for (int i = 0; i < argsCount; i++) {
                argsLen[i] = wrapper.getShort();
                expectedLen += argsLen[i];
            }

            if (wrapper.remaining() < expectedLen) {
                wrapper.position(originPos);
                return null;
            }
            byte[][] argTypes = new byte[argsCount][];
            for (int i = 0; i < argsCount; i++) {
                byte[] argTypeByte = new byte[argsTypeLen[i]];
                wrapper.get(argTypeByte);
                argTypes[i] = argTypeByte;
            }
            byte[][] argObjects = new byte[argsCount][];
            for (int i = 0; i < argsCount; i++) {
                byte[] argByte = new byte[argsLen[i]];
                wrapper.get(argByte);
                argObjects[i] = argByte;
            }
            ZazaRequest requestWrapper = new ZazaRequest(requestId, methodCode, argObjects, argTypes);
            return requestWrapper;
        } else if (type == RESPONSE) {
            if (wrapper.remaining() < 4) {
                wrapper.position(originPos);
                return null;
            }
            byte requestId = wrapper.get();
            byte classNameLen = wrapper.get();
            short bodyLen = wrapper.getShort();

            if (wrapper.remaining() < classNameLen + bodyLen) {
                wrapper.position(originPos);
                return null;
            }
            byte[] classNameBytes = new byte[classNameLen];
            wrapper.get(classNameBytes);
            byte[] bodyBytes = new byte[bodyLen];
            wrapper.get(bodyBytes);
            ZazaResponse responseWrapper = new ZazaResponse(requestId, bodyBytes, classNameBytes);
            return responseWrapper;
        } else {
            logger.error("protocol type : " + type + " is not supported!");
            return null;
        }
    }

}
