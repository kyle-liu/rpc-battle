package com.taobao.rpc.remoting.command;

import java.net.InetSocketAddress;
import java.util.Arrays;

import com.taobao.gecko.core.command.ResponseCommand;
import com.taobao.gecko.core.command.ResponseStatus;
import com.taobao.gecko.core.command.kernel.BooleanAckCommand;


public abstract class RpcResponseCommand implements ResponseCommand, RpcCommand {
    static final long serialVersionUID = 77788812547386438L;
    private static final byte magic = Constants.RESPONSE_MAGIC;
    protected OpCode opCode;
    protected ResponseStatus responseStatus;
    protected short headerLength;
    protected int totalBodyLength;
    protected byte[] header;
    protected byte[] body;
    protected Integer opaque;

    private long responseTime;

    private InetSocketAddress responseHost;


    public RpcResponseCommand(final OpCode opCode) {
        super();
        this.opCode = opCode;
        this.responseTime = System.currentTimeMillis();
    }


    public void setResponseTime(final long responseTime) {
        this.responseTime = responseTime;
    }


    /**
     * 检测消息是否为boolean类型
     * 
     * @return
     */

    public boolean isBoolean() {
        return this instanceof BooleanAckCommand;
    }


    public InetSocketAddress getResponseHost() {
        return this.responseHost;
    }


    public void setResponseHost(final InetSocketAddress responseHost) {
        this.responseHost = responseHost;
    }


    public long getResponseTime() {
        return this.responseTime;
    }


    public RpcResponseCommand() {

    }


    public Integer getOpaque() {
        return this.opaque;
    }


    public void setOpaque(final Integer opaque) {
        this.opaque = opaque;
    }


    public byte getMagic() {
        return this.magic;
    }


    public OpCode getOpCode() {
        return this.opCode;
    }


    public void setOpCode(final OpCode opCode) {
        this.opCode = opCode;
    }


    public ResponseStatus getResponseStatus() {
        return this.responseStatus;
    }


    public void setResponseStatus(final ResponseStatus status) {
        this.responseStatus = status;
    }


    public short getHeaderLength() {
        return this.headerLength;
    }


    public void setHeaderLength(final short headerLength) {
        this.totalBodyLength = this.totalBodyLength - this.headerLength + headerLength;
        this.headerLength = headerLength;
    }


    public void setHeader(final byte[] header) {
        this.header = header;
        if (this.header.length > Short.MAX_VALUE) {
            throw new IllegalStateException("Illegal header,too long");
        }
        this.setHeaderLength((short) this.header.length);
    }


    public void setBody(final byte[] body) {
        if (body == null) {
            throw new NullPointerException("NUll body");
        }
        this.body = body;
        this.totalBodyLength = this.headerLength + this.body.length;
    }


    public int getTotalBodyLength() {
        return this.totalBodyLength;
    }


    public void setTotalBodyLength(final int totalBodyLength) {
        this.totalBodyLength = totalBodyLength;
    }


    public byte[] getHeader() {
        return this.header;
    }


    public byte[] getBody() {
        return this.body;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(this.body);
        result = prime * result + Arrays.hashCode(this.header);
        result = prime * result + this.headerLength;
        result = prime * result + (this.opCode == null ? 0 : this.opCode.hashCode());
        result = prime * result + (this.opaque == null ? 0 : this.opaque.hashCode());
        result = prime * result + (this.responseStatus == null ? 0 : this.responseStatus.hashCode());
        result = prime * result + this.totalBodyLength;
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
        final RpcResponseCommand other = (RpcResponseCommand) obj;
        if (!Arrays.equals(this.body, other.body)) {
            return false;
        }
        if (!Arrays.equals(this.header, other.header)) {
            return false;
        }
        if (this.headerLength != other.headerLength) {
            return false;
        }
        if (this.opCode == null) {
            if (other.opCode != null) {
                return false;
            }
        }
        else if (!this.opCode.equals(other.opCode)) {
            return false;
        }
        if (this.opaque == null) {
            if (other.opaque != null) {
                return false;
            }
        }
        else if (!this.opaque.equals(other.opaque)) {
            return false;
        }
        if (this.responseStatus == null) {
            if (other.responseStatus != null) {
                return false;
            }
        }
        else if (!this.responseStatus.equals(other.responseStatus)) {
            return false;
        }
        if (this.totalBodyLength != other.totalBodyLength) {
            return false;
        }
        return true;
    }

}
