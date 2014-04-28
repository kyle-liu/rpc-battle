package com.taobao.rpc.fish.server.queue.impl;

import java.util.concurrent.Future;

import com.lmax.disruptor.EventHandler;
import com.taobao.gecko.core.core.Session;
import com.taobao.gecko.core.nio.NioSession;
import com.taobao.gecko.service.Connection;
import com.taobao.rpc.fish.common.command.BatchResponseCommand;
import com.taobao.rpc.fish.common.command.RpcBooleanCommand;
import com.taobao.rpc.fish.common.command.RpcResponseCommand;
import com.taobao.rpc.fish.common.command.codec.Serializer;
import com.taobao.rpc.fish.common.command.codec.impl.KryoSerializer;
/**
 * ��Ӧ������
 * @author zhouqi.zhm
 *
 */
public class ResponseSerializerHandler implements EventHandler<RequestEvent>{
	public volatile int opaque=0;
	public final Serializer ser=new KryoSerializer();
	public final BatchResponseCommand batchCommand=new BatchResponseCommand();
	@Override
	public void onEvent(RequestEvent event, long sequence, boolean endOfBatch)
			throws Exception {
		if(!event.isNeedResponse())return;
		RpcResponseCommand command = (RpcResponseCommand)event.getResponse();
		//this.opaque=command.getOpaque();
		//System.out.println("response one command opa="+command.getOpaque());
		if(command==null)return;
		try {
			command.serializer(ser);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Session session=event.getSession();
			session.asyncWrite(new RpcBooleanCommand(command.getOpaque(), "响应序列化出错"));
			event.setNeedResponse(false);
		}
		//Session session=event.getSession();
		/*if(endOfBatch){
		//session.asyncWrite(command);
			if(batchCommand.addCommand(command, session)){
				Future<Boolean> future=((NioSession)session).asyncWriteInterruptibly(batchCommand);
				batchCommand.reset();
			}else{
				if(batchCommand.isNeedSend()){
					Future<Boolean> future=((NioSession)session).asyncWriteInterruptibly(batchCommand);
					batchCommand.reset();
				}
				Future<Boolean> future=((NioSession)session).asyncWriteInterruptibly(command);	
			}
			 
		 }else{
			 if(!batchCommand.addCommand(command, session)){
					((NioSession)session).asyncWriteInterruptibly(batchCommand);
					batchCommand.reset();
					batchCommand.addCommand(command, session);
			}
		 }*/
		
		
	}

}
