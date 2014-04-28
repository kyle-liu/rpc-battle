package com.taobao.rpc.remoting.codec;

import com.taobao.gecko.core.core.CodecFactory;


public class RpcProtocolCodecFactory implements CodecFactory {
    private final Encoder encoder;
    private final Decoder decoder;


    public RpcProtocolCodecFactory() {
        this.encoder = new NotifyWrapEncoder();
        this.decoder = new RpcWrapDecoder();
    }


    public Decoder getDecoder() {
        return this.decoder;
    }


    public Encoder getEncoder() {
        return this.encoder;
    }

}
