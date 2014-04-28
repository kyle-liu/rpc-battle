package com.taobao.rpc.remoting.command;

import com.taobao.gecko.core.util.OpaqueGenerator;


/**
 * ·¢ËÍ¶©ÔÄ¹ØÏµÃüÁî
 * 
 * @author boyan
 * @Date 2010-7-27
 * 
 */
public class RpcInvokeCommand extends RpcSendRequestCommand {

    private static final long serialVersionUID = -5137387572144564328L;


    public RpcInvokeCommand(final OpCode opCode) {
        super(opCode);
    }


    public RpcInvokeCommand(final String headerInfo,byte[] data) {
        super(headerInfo,data);
        this.opCode = OpCode.SEND_SUBSCRIPTION;
        this.opaque = OpaqueGenerator.getNextOpaque();
    }

}
