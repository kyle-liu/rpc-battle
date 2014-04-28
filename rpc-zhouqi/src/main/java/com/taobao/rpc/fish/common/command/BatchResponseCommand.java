package com.taobao.rpc.fish.common.command;

import java.net.InetSocketAddress;
import java.util.ArrayList;

import com.taobao.gecko.core.buffer.IoBuffer;
import com.taobao.gecko.core.command.ResponseCommand;
import com.taobao.gecko.core.command.ResponseStatus;
import com.taobao.gecko.core.core.Session;

public class BatchResponseCommand extends BaseCommand implements ResponseCommand{
	public final static int MAX_SIZE	=2;
	public ArrayList<RpcResponseCommand> commandList=new ArrayList<RpcResponseCommand>(4);
	public Session session;
	public boolean needSend=false;
	public BatchResponseCommand(Session session) {
		super((byte)4);
		// TODO Auto-generated constructor stub
		this.session=session;
	}
	public BatchResponseCommand() {
		super((byte)4);
		
	}
	@Override
	public ResponseStatus getResponseStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setResponseStatus(ResponseStatus responseStatus) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isBoolean() {
		// TODO Auto-generated method stub
		return false;
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
		return 0;
	}

	@Override
	public void setResponseTime(long time) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte[] getData() {
		// TODO Auto-generated method stub
		return null;
	}
	public boolean addCommand(RpcResponseCommand response,Session session){
		if(this.session==null){
			this.session=session;
			commandList.add(response);
			needSend=true;
			return true;
		}
		if(session==this.session&&commandList.size()<MAX_SIZE){
			commandList.add(response);
			needSend=true;
		}else{
			return false;
		}
		return true;
	}
	public  IoBuffer encode(){
		int length=0;
		for(RpcResponseCommand command:commandList){
			length=length+command.encodeLength();
		}
		IoBuffer buffer=IoBuffer.allocate(length);
		for(RpcResponseCommand command:commandList){
			int dataLength=0;
			byte data[]=command.getData();
			if(data!=null)dataLength=data.length;
			buffer.put(command.getType());
			buffer.putInt(command.getOpaque());
			buffer.putInt(dataLength);
			if(data!=null){
				buffer.put(data);
			}
		}
		buffer.flip();		
		return buffer;
	 }
	public boolean reset(){
		this.session=null;
		commandList.clear();
		needSend=false;
		return true;
	}
	
	public boolean isNeedSend() {
		return needSend;
	}
	public void setNeedSend(boolean needSend) {
		this.needSend = needSend;
	}
	public Session getSession() {
		return session;
	}
	public void setSession(Session session) {
		this.session = session;
	}
	
}
