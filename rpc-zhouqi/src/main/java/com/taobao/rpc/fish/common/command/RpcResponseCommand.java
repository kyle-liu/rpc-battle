package com.taobao.rpc.fish.common.command;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.taobao.gecko.core.command.ResponseCommand;
import com.taobao.gecko.core.command.ResponseStatus;
import com.taobao.rpc.fish.common.command.codec.Serializer;
import com.taobao.rpc.fish.common.command.codec.impl.CodecFacory;

public class RpcResponseCommand extends BaseCommand implements ResponseCommand{

	private Object result;
	private long time;
	private byte resultBytes[];//result的序列化结果
	public RpcResponseCommand(int opaque,byte data[]){
		super(BaseCommand.RPC_REPONSE);
		try {
			//result=CodecFacory.getDeserializer().decodeObject(data);
			resultBytes=data;
			this.setOpaque(opaque);
		} catch (Exception e) {
			throw new RuntimeException("反序列化对象出错");
		}
	}
	public RpcResponseCommand(){
		super(BaseCommand.RPC_REPONSE);
	}
	public RpcResponseCommand(Object result,int opaque) {
		super(BaseCommand.RPC_REPONSE);
		this.result=result;
		this.setOpaque(opaque);
	}
	
	public Object getResult() {
		return result;
	}
	public void setResult(Object result) {
		this.result = result;
	}
	@Override
	public byte[] getData() {
			return resultBytes;		
	}
	public void serializer(){
		try {
			resultBytes=CodecFacory.getSerializer().encodeObject(result);			
		} catch (IOException e) {
			throw new RuntimeException("序列化对象出错");
		}
	}
	public void serializer(Serializer ser){
		try {
			resultBytes=ser.encodeObject(result);			
		} catch (IOException e) {
			throw new RuntimeException("序列化对象出错");
		}
	}
	@Override
	public ResponseStatus getResponseStatus() {
		// TODO Auto-generated method stub
		return ResponseStatus.NO_ERROR;
	}
	@Override
	public void setResponseStatus(ResponseStatus responseStatus) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean isBoolean() {
		// TODO Auto-generated method stub
		return true;
	}
	@Override
	public InetSocketAddress getResponseHost() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void setResponseHost(InetSocketAddress address) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public long getResponseTime() {
		// TODO Auto-generated method stub
		return this.time;
	}
	@Override
	public void setResponseTime(long time) {
		// TODO Auto-generated method stub
		this.time=time;
	}
	public int encodeLength(){		
		byte data[]=this.getData();
		int dataLength=0;
		if(data!=null)dataLength=data.length;
		return this.getHeaderLength()+dataLength;
	}

}
