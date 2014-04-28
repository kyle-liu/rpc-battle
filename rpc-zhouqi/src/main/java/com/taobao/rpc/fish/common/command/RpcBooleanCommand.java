package com.taobao.rpc.fish.common.command;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.InetSocketAddress;

import com.taobao.gecko.core.command.ResponseStatus;
import com.taobao.gecko.core.command.kernel.BooleanAckCommand;

public class RpcBooleanCommand extends BaseCommand implements BooleanAckCommand{
	private String errorMsg;
	private ResponseStatus responseStatus;
	private long responseTime;
	public RpcBooleanCommand(){
		super(BaseCommand.RPC_BOOLEAN);
	}
	public RpcBooleanCommand(int opaque,byte data[]) {
		super(BaseCommand.RPC_BOOLEAN);
		// TODO Auto-generated constructor stub
		this.setOpaque(opaque);
		if(data==null||data.length==0){
			this.responseStatus=ResponseStatus.NO_ERROR;
		}else{
			this.responseStatus=ResponseStatus.ERROR;
			try {
				this.errorMsg=new String(data,"utf-8");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("解码消息出错,opaque="+this.getOpaque());
			}
		}
	}
	public RpcBooleanCommand(int opaque,String errorMsg) {
		super(BaseCommand.RPC_BOOLEAN);
		// TODO Auto-generated constructor stub
		this.setOpaque(opaque);
		if(errorMsg==null){
			this.responseStatus=ResponseStatus.NO_ERROR;
		}else{
			this.responseStatus=ResponseStatus.ERROR;
			this.errorMsg=errorMsg;
			
		}
	}
	@Override
	public byte[] getData() {
		if(errorMsg!=null){
			try {
				return errorMsg.getBytes("utf-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				throw new RuntimeException("编码消息出处,errorMsg="+errorMsg+e);
			}
		}
		return null;
	}

	@Override
	public ResponseStatus getResponseStatus() {
		// TODO Auto-generated method stub
		return responseStatus;
	}

	@Override
	public void setResponseStatus(ResponseStatus responseStatus) {
		this.responseStatus=responseStatus;
		
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
		return this.responseTime;
	}

	@Override
	public void setResponseTime(long time) {
		// TODO Auto-generated method stub
		this.responseTime=time;
	}
	
	@Override
	public String getErrorMsg() {
		// TODO Auto-generated method stub
		return this.errorMsg;
	}

	@Override
	public void setErrorMsg(String errorMsg) {
		this.errorMsg=errorMsg;
		
	}

}
