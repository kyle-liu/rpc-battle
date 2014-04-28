package com.taobao.rpc.remoting.command;

import com.taobao.gecko.core.command.CommandHeader;


/**
 * Notify的请求协议头
 * 
 * @author dennis
 * 
 */
public class RpcRequestCommandHeader implements CommandHeader {
    private Integer opaque;
    private OpCode opCode;


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.opCode == null ? 0 : this.opCode.hashCode());
        result = prime * result + this.opaque;
        return result;
    }


    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final RpcRequestCommandHeader other = (RpcRequestCommandHeader) obj;
        if (this.opCode == null) {
            if (other.opCode != null) {
                return false;
            }
        }
        else if (!this.opCode.equals(other.opCode)) {
            return false;
        }
        if (!this.opaque.equals(this.opaque)) {
            return false;
        }
        return true;
    }


    public RpcRequestCommandHeader(final int opaque, final OpCode opCode) {
        super();
        this.opaque = opaque;
        this.opCode = opCode;
    }


    public Integer getOpaque() {
        return this.opaque;
    }


    public void setOpaque(final int opaque) {
        this.opaque = opaque;
    }


    public OpCode getOpCode() {
        return this.opCode;
    }


    public void setOpCode(final OpCode opCode) {
        this.opCode = opCode;
    }

}
