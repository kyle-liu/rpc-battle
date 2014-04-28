package com.taobao.rpc.fish.common.command;

import java.io.IOException;

import com.taobao.gecko.core.buffer.IoBuffer;
import com.taobao.gecko.core.command.CommandHeader;
import com.taobao.gecko.core.command.RequestCommand;
import com.taobao.rpc.fish.common.command.codec.Deserializer;
import com.taobao.rpc.fish.common.command.codec.impl.CodecFacory;

/**
 * RPC调用请求命令
 * @author zhouqi.zhm
 *
 */
public class RpcRequestCommand extends BaseCommand implements RequestCommand{
	
	private Object params[];//rpc请求参数
	private byte[] requestData;//rpc请求参数
	private byte[] targetDigests;//rpc方法签名
	private String hexDigests;//rpc方法签名16进制表示
	public RpcRequestCommand(){
		super(BaseCommand.RPC_REQUEST);
	}
	public RpcRequestCommand(int opaque) {
		super(BaseCommand.RPC_REQUEST);
		this.setOpaque(opaque);
	}
	
	/**
	 * 编码命令
	 * 
	 * @return
	 */
	@Override
	public  IoBuffer encode(){
		byte data[]=this.getData();
		int dataLength=16;//前面16位为调用方法的数字签名
		if(data!=null)dataLength=dataLength+data.length;
		IoBuffer buffer=IoBuffer.allocate(this.getHeaderLength()+dataLength);
		buffer.put(this.getType());
		buffer.putInt(this.getOpaque());
		buffer.putInt(dataLength);
		buffer.put(targetDigests);
		if(data!=null){
			buffer.put(data);
		}
		buffer.flip();

		return buffer;
	}
	/**
	 * 只有编码的时候才会调用这个方法，其它的时候不会调用
	 */
	public byte[] getData() {
		try {
			return CodecFacory.getSerializer().encodeObject(params);
		} catch (IOException e) {
			throw new RuntimeException("序列化对象出错");
		}
	}
	/**
	 * 反序列话对象
	 */
	public void deserializer(){
		if(requestData==null)return;
		try {
			params=(Object[])CodecFacory.getDeserializer().decodeObject(requestData);
			requestData=null;
		} catch (IOException e) {
			throw new RuntimeException("返序列化对象出错");
		}
	}
	public void deserializer(Deserializer des){
		if(requestData==null)return;
		try {
			params=(Object[])des.decodeObject(requestData);
			requestData=null;
		} catch (IOException e) {
			throw new RuntimeException("返序列化对象出错");
		}
	}
	public Object[] getParams() {
		return params;
	}
	public void setParams(Object[] params) {
		this.params = params;
	}
	public byte[] getRequestData() {
		return requestData;
	}
	public void setRequestData(byte[] requestData) {
		this.requestData = requestData;
	}
	public byte[] getTargetDigests() {
		return targetDigests;
	}
	public void setTargetDigests(byte[] targetDigests) {
		this.targetDigests = targetDigests;
	}
	public String getHexDigests() {
		return hexDigests;
	}
	public void setHexDigests(String hexDigests) {
		this.hexDigests = hexDigests;
	}
	@Override
	public CommandHeader getRequestHeader() {
		// TODO Auto-generated method stub
		 return new CommandHeader(){

			@Override
			public int getOpaque() {
				// TODO Auto-generated method stub
				return RpcRequestCommand.this.getOpaque();
			}
			
		};
	}
	public String toString(){
		return 		"send command type="+this.getType()+",opa="+this.getOpaque();

	}
}
