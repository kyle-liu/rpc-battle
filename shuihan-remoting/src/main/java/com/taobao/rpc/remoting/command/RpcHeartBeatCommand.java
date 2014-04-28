package com.taobao.rpc.remoting.command;

import com.taobao.gecko.core.command.kernel.HeartBeatRequestCommand;
import com.taobao.gecko.core.util.OpaqueGenerator;


/**
 * 
 * ÐÄÌøÃüÁî
 * 
 * @author boyan
 * 
 * @since 1.0, 2009-12-18 ÏÂÎç03:26:36
 */

public class RpcHeartBeatCommand extends RpcRequestCommand implements HeartBeatRequestCommand {
    static final long serialVersionUID = -98010017355L;


    public RpcHeartBeatCommand() {
        this.opCode = OpCode.HEARTBEAT;
        this.opaque = OpaqueGenerator.getNextOpaque();
    }


    public RpcHeartBeatCommand(final OpCode opCode) {
        super(opCode);
    }


    public void decodeContent() {
        // no header and no body
    }


    public void encodeContent() {
        // no header and no body

    }

}
