package com.taobao.rpc.remoting.factory;

import com.taobao.gecko.core.command.CommandFactory;
import com.taobao.gecko.core.core.CodecFactory;
import com.taobao.gecko.service.config.WireFormatType;
import com.taobao.rpc.remoting.codec.RpcProtocolCodecFactory;
import com.taobao.rpc.remoting.command.RpcCommandFactory;


public class RpcWireFormatType extends WireFormatType {

    @Override
    public String name() {
        return "RPC_V1";
    }


    @Override
    public String getScheme() {
        return "tcp";
    }


    @Override
    public CodecFactory newCodecFactory() {
        return new RpcProtocolCodecFactory();
    }


    @Override
    public CommandFactory newCommandFactory() {
        return new RpcCommandFactory();
    }
}
