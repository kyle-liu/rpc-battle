package com.taobao.rpc.fish.common.command;

import java.io.IOException;

import com.taobao.gecko.core.buffer.IoBuffer;
import com.taobao.gecko.core.command.CommandHeader;
import com.taobao.gecko.core.command.RequestCommand;
import com.taobao.rpc.fish.common.command.codec.Deserializer;
import com.taobao.rpc.fish.common.command.codec.impl.CodecFacory;

/**
 * RPC������������
 * @author zhouqi.zhm
 *
 */
public class RpcRequestCommand extends BaseCommand implements RequestCommand{
	
	private Object params[];//rpc�������
	private byte[] requestData;//rpc�������
	private byte[] targetDigests;//rpc����ǩ��
	private String hexDigests;//rpc����ǩ��16���Ʊ�ʾ
	public RpcRequestCommand(){
		super(BaseCommand.RPC_REQUEST);
	}
	public RpcRequestCommand(int opaque) {
		super(BaseCommand.RPC_REQUEST);
		this.setOpaque(opaque);
	}
	
	/**
	 * ��������
	 * 
	 * @return
	 */
	@Override
	public  IoBuffer encode(){
		byte data[]=this.getData();
		int dataLength=16;//ǰ��16λΪ���÷���������ǩ��
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
	 * ֻ�б����ʱ��Ż�������������������ʱ�򲻻����
	 */
	public byte[] getData() {
		try {
			return CodecFacory.getSerializer().encodeObject(params);
		} catch (IOException e) {
			throw new RuntimeException("���л��������");
		}
	}
	/**
	 * �����л�����
	 */
	public void deserializer(){
		if(requestData==null)return;
		try {
			params=(Object[])CodecFacory.getDeserializer().decodeObject(requestData);
			requestData=null;
		} catch (IOException e) {
			throw new RuntimeException("�����л��������");
		}
	}
	public void deserializer(Deserializer des){
		if(requestData==null)return;
		try {
			params=(Object[])des.decodeObject(requestData);
			requestData=null;
		} catch (IOException e) {
			throw new RuntimeException("�����л��������");
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
