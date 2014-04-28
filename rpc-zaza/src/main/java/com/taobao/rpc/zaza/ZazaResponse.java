package com.taobao.rpc.zaza;

public class ZazaResponse {
    private final byte requestId;
    private final byte[] responseObject;
    private final byte[] responseType;

    public ZazaResponse(byte requestId, byte[] responseObject, byte[] responseType) {
        this.responseObject = responseObject;
        this.responseType = responseType;
        this.requestId = requestId;
    }

    public byte[] getResponseObject() {
        return responseObject;
    }

    public byte[] getResponseType() {
        return responseType;
    }

    public byte getRequestId() {
        return requestId;
    }

}
