package com.taobao.rpc.remoting.command;

import java.util.Arrays;




public class RpcAckCommand extends RpcResponseCommand {
    /**
     * 
     */
    private static final long serialVersionUID = -306568926854544242L;
    private byte[] serverData;

    public RpcAckCommand() {

    }


    public RpcAckCommand(final OpCode opCode) {
        super(opCode);
    }


    public RpcAckCommand(final RpcRequestCommand request,
            final com.taobao.gecko.core.command.ResponseStatus responseStatus, final byte[] serverData) {
        if (request == null) {
            throw new NullPointerException("Null request");
        }
        this.opaque = request.getOpaque();
        this.responseStatus = responseStatus;
        this.opCode = request.getOpCode();
        this.serverData = serverData;
    }


    public void decodeContent() {
      
    }


    public void encodeContent() {
       if(this.serverData != null){
    	   this.setBody(serverData);
       }
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Arrays.hashCode(this.serverData);
        return result;
    }


    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final RpcAckCommand other = (RpcAckCommand) obj;
        if (!Arrays.equals(this.serverData, other.serverData)) {
            return false;
        }
        return true;
    }

}
