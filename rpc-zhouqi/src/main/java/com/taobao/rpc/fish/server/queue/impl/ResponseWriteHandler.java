package com.taobao.rpc.fish.server.queue.impl;

import java.util.concurrent.Future;

import com.lmax.disruptor.EventHandler;
import com.taobao.gecko.core.core.Session;
import com.taobao.gecko.core.nio.NioSession;
import com.taobao.rpc.fish.common.command.RpcResponseCommand;

public class ResponseWriteHandler implements EventHandler<RequestEvent>{

	@Override
	public void onEvent(RequestEvent event, long sequence, boolean endOfBatch)
			throws Exception {
		// TODO Auto-generated method stub
		if(!event.isNeedResponse())return;
		RpcResponseCommand command = (RpcResponseCommand)event.getResponse();
		Session session=event.getSession();
		Future<Boolean> future=((NioSession)session).asyncWriteInterruptibly(command);		
		//event.setCommand(null);
		event.setSession(null);
	}

}
