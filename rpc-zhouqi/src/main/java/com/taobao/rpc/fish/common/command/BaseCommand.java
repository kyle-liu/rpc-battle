package com.taobao.rpc.fish.common.command;

import com.taobao.gecko.core.buffer.IoBuffer;
import com.taobao.gecko.core.command.ResponseCommand;


/**
 * 命令基类
 * 
 * @author zhouqi.zhm
 * 
 */
public abstract class BaseCommand{

	public final static byte RPC_REQUEST=0;
	public final static byte RPC_REPONSE=1;
	public final static byte RPC_BOOLEAN=2;
	public final static byte RPC_HEARTBEAT=3;
	private int opaque;
	private byte type;

	public BaseCommand(byte type){
		this.type=type;
	}
	/**
	 * 编码命令
	 * 
	 * @return
	 */
	public  IoBuffer encode(){
		byte data[]=this.getData();
		int dataLength=0;
		if(data!=null)dataLength=data.length;
		IoBuffer buffer=IoBuffer.allocate(this.getHeaderLength()+dataLength);
		buffer.put(this.getType());
		buffer.putInt(this.getOpaque());
		buffer.putInt(dataLength);
		if(data!=null){
			buffer.put(data);
		}
		buffer.flip();
		//System.out.println("send command type="+this.getType()+",opa="+this.getOpaque()+",dataLength="+dataLength);
		return buffer;
	}
	/**
	 * 返回头的长度 header=type+opaque+dataLength
	 * 
	 * @return
	 */
	final public int getHeaderLength() {
		return 1 +4+ 4;
	}

	public abstract byte[] getData();

	public int getOpaque() {
		return opaque;
	}

	public void setOpaque(int opaque) {
		this.opaque = opaque;
	}

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

}
