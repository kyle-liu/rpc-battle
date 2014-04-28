package com.taobao.rpc.remoting.command;

import com.taobao.gecko.core.command.CommandHeader;
import com.taobao.gecko.core.command.ResponseStatus;
import com.taobao.gecko.core.command.kernel.BooleanAckCommand;



public class RpcBooleanAckCommand extends RpcResponseCommand implements BooleanAckCommand {
    /**
     * 
     */
    private static final long serialVersionUID = -2729908481782608962L;
    private String errorMsg;


    public RpcBooleanAckCommand(final OpCode opCode) {
        super(opCode);
    }


    public RpcBooleanAckCommand(final RpcRequestCommand request, final ResponseStatus responseStatus,
            final String errorMsg) {
        if (request == null) {
            throw new NullPointerException("Null request");
        }
        if (responseStatus == null) {
            throw new NullPointerException("Null ResponseStatus");
        }
        this.opCode = request.getOpCode();
        this.opaque = request.getOpaque();
        this.responseStatus = responseStatus;
        this.errorMsg = errorMsg;
    }


    public RpcBooleanAckCommand(final CommandHeader header, final ResponseStatus responseStatus,
            final String errorMsg) {
        if (header == null) {
            throw new NullPointerException("Null header");
        }
        if (responseStatus == null) {
            throw new NullPointerException("Null ResponseStatus");
        }
        if (header instanceof RpcRequestCommandHeader) {
            this.opCode = ((RpcRequestCommandHeader) header).getOpCode();
        }
        else {
            // remoting自身返回的header，可能没有设置opcode，那么默认设置为dummy
            this.opCode = OpCode.DUMMY;
        }
        this.opaque = header.getOpaque();
        this.responseStatus = responseStatus;
        this.errorMsg = errorMsg;
    }


    public void decodeContent() {
        if (this.header != null) {
            this.errorMsg = new String(header);
        }
    }


    public void encodeContent() {
        if (this.errorMsg != null) {
            this.setHeader(this.errorMsg.getBytes());
        }
    }


    public String getErrorMsg() {
        return this.errorMsg;
    }


    public void setErrorMsg(final String errorMsg) {
        this.errorMsg = errorMsg;
    }

}
