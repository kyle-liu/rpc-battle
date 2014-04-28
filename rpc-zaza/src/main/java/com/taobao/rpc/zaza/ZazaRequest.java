package com.taobao.rpc.zaza;

import com.taobao.rpc.zaza.interfaces.ZazaClient;

public class ZazaRequest {
    private final byte requestID;
    private final byte methodCode;
    private final byte[][] requestObjects;
    private final byte[][] requestTypes;

    public ZazaRequest(byte methodCode, byte[][] requestObjects, byte[][] requestTypes) {
        this(ZazaClient.getIndexOfThread(), methodCode, requestObjects, requestTypes);
    }

    public ZazaRequest(byte requestId, byte methodCode, byte[][] requestObjects, byte[][] requestTypes) {
        this.methodCode = methodCode;
        this.requestObjects = requestObjects;
        this.requestTypes = requestTypes;
        this.requestID = requestId;
    }

    public byte getMethodCode() {
        return methodCode;
    }

    public byte[][] getRequestObjects() {
        return requestObjects;
    }

    public byte getRequestID() {
        return requestID;
    }

    public byte[][] getRequestTypes() {
        return requestTypes;
    }

}
