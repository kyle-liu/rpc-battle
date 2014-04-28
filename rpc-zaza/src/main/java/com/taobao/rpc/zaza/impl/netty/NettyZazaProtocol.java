package com.taobao.rpc.zaza.impl.netty;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.slf4j.LoggerFactory;

import com.taobao.rpc.zaza.ZazaRequest;
import com.taobao.rpc.zaza.ZazaResponse;

public class NettyZazaProtocol {
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(NettyZazaProtocol.class);

    private static final byte REQUEST = (byte) 0;
    private static final byte RESPONSE = (byte) 1;

    public static ChannelBuffer encode(Object message) throws Exception {
        if (message instanceof ZazaRequest) {
            ZazaRequest wrapper = (ZazaRequest) message;
            int capacity = calculateCapacityOfRequest(wrapper);
            ChannelBuffer byteBuffer = ChannelBuffers.dynamicBuffer(capacity);
            writeOneRequest(wrapper, byteBuffer);
            return byteBuffer;
        } else if (message instanceof ZazaResponse) {
            int capacity = ((ZazaResponse) message).getResponseObject().length
                    + ((ZazaResponse) message).getResponseType().length + 1 + 1 + 1 + 2;
            ChannelBuffer byteBuffer = ChannelBuffers.dynamicBuffer(capacity);
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

    private static void writeOneResponse(Object message, ChannelBuffer byteBuffer) {
        byteBuffer.writeByte(RESPONSE);
        byteBuffer.writeByte(((ZazaResponse) message).getRequestId());
        byteBuffer.writeByte((byte) ((ZazaResponse) message).getResponseType().length);
        byteBuffer.writeShort((short) ((ZazaResponse) message).getResponseObject().length);
        byteBuffer.writeBytes(((ZazaResponse) message).getResponseType());
        byteBuffer.writeBytes(((ZazaResponse) message).getResponseObject());
    }

    private static void writeOneRequest(ZazaRequest message, ChannelBuffer byteBuffer) {
        byteBuffer.writeByte(REQUEST);
        byteBuffer.writeByte(message.getRequestID());
        byteBuffer.writeByte(message.getMethodCode());
        byte[][] requestArgTypeStrings = message.getRequestTypes();
        byte[][] requestObjects = message.getRequestObjects();
        byteBuffer.writeByte((byte) (requestObjects == null ? 0 : requestObjects.length));
        if (requestArgTypeStrings != null) {
            for (byte[] requestArgType : requestArgTypeStrings) {
                byteBuffer.writeByte((byte) requestArgType.length);
            }

            for (byte[] requestArg : requestObjects) {
                byteBuffer.writeShort((short) requestArg.length);
            }

            for (byte[] requestArgType : requestArgTypeStrings) {
                byteBuffer.writeBytes(requestArgType);
            }

            for (byte[] requestArg : requestObjects) {
                byteBuffer.writeBytes(requestArg);
            }
        }
    }

    public static Object decode(ChannelBuffer wrapper) {
        final int originPos = wrapper.readerIndex();
        if (wrapper.readableBytes() < 1) {
            wrapper.setIndex(originPos, wrapper.writerIndex());
            return null;
        }
        byte type = wrapper.readByte();
        if (type == REQUEST) {
            if (wrapper.readableBytes() < 3) {
                wrapper.setIndex(originPos, wrapper.writerIndex());
                return null;
            }
            byte requestId = wrapper.readByte();
            byte methodCode = wrapper.readByte();
            byte argsCount = wrapper.readByte();

            int argInfosLen = argsCount * (2 + 1);
            if (wrapper.readableBytes() < argInfosLen) {
                wrapper.setIndex(originPos, wrapper.writerIndex());
                return null;
            }
            int[] argsTypeLen = new int[argsCount];
            int expectedLen = 0;
            for (int i = 0; i < argsCount; i++) {
                argsTypeLen[i] = wrapper.readByte();
                expectedLen += argsTypeLen[i];
            }
            int[] argsLen = new int[argsCount];
            for (int i = 0; i < argsCount; i++) {
                argsLen[i] = wrapper.readShort();
                expectedLen += argsLen[i];
            }

            if (wrapper.readableBytes() < expectedLen) {
                wrapper.setIndex(originPos, wrapper.writerIndex());
                return null;
            }
            byte[][] argTypes = new byte[argsCount][];
            for (int i = 0; i < argsCount; i++) {
                byte[] argTypeByte = new byte[argsTypeLen[i]];
                wrapper.readBytes(argTypeByte);
                argTypes[i] = argTypeByte;
            }
            byte[][] argObjects = new byte[argsCount][];
            for (int i = 0; i < argsCount; i++) {
                byte[] argByte = new byte[argsLen[i]];
                wrapper.readBytes(argByte);
                argObjects[i] = argByte;
            }
            ZazaRequest requestWrapper = new ZazaRequest(requestId, methodCode, argObjects, argTypes);
            return requestWrapper;
        } else if (type == RESPONSE) {
            if (wrapper.readableBytes() < 4) {
                wrapper.setIndex(originPos, wrapper.writerIndex());
                return null;
            }
            byte requestId = wrapper.readByte();
            byte classNameLen = wrapper.readByte();
            short bodyLen = wrapper.readShort();

            if (wrapper.readableBytes() < classNameLen + bodyLen) {
                wrapper.setIndex(originPos, wrapper.writerIndex());
                return null;
            }
            byte[] classNameBytes = new byte[classNameLen];
            wrapper.readBytes(classNameBytes);
            byte[] bodyBytes = new byte[bodyLen];
            wrapper.readBytes(bodyBytes);
            ZazaResponse responseWrapper = new ZazaResponse(requestId, bodyBytes, classNameBytes);
            return responseWrapper;
        } else {
            logger.error("protocol type : " + type + " is not supported!");
            return null;
        }
    }

}
