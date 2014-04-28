package com.taobao.rpc.fish.server.queue.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.BlockingWaitStrategy;

import com.lmax.disruptor.ClaimStrategy;
import com.lmax.disruptor.MultiThreadedClaimStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.Sequencer;
import com.lmax.disruptor.SingleThreadedClaimStrategy;
import com.lmax.disruptor.WaitStrategy;

import com.lmax.disruptor.YieldingWaitStrategy;
import com.taobao.gecko.core.buffer.IoBuffer;
import com.taobao.gecko.core.core.Session;
import com.taobao.gecko.service.Connection;
import com.taobao.rpc.fish.common.command.RpcRequestCommand;
import com.taobao.rpc.fish.common.util.MD5;
import com.taobao.rpc.fish.server.ServerRegister;
import com.taobao.rpc.fish.server.queue.RpcRequestQueue;

public class DisruptorRpcRequestQueue implements RpcRequestQueue {
	private int BUFFER_SIZE=1024*50;
	private static final int NUM_EVENT_PROCESSORS = 3;
	private ServerRegister serverRegister;
	private RingBuffer<RequestEvent> ringBuffer;
	
	private SequenceBarrier stepOneSequenceBarrier;
	private RequestDeserializerHandler deserializerHandler;
	private BatchEventProcessor<RequestEvent> stepOneBatchProcessor ;
	
	private SequenceBarrier stepTwoSequenceBarrier;
	private MethdoInvokeHandler methodInvokeHandler;
	private BatchEventProcessor<RequestEvent> stepTwoBatchProcessor ;
	
	private SequenceBarrier stepThreeSequenceBarrier;
	private ResponseSerializerHandler serializerHandler;
	private BatchEventProcessor<RequestEvent> stepThreeBatchProcessor ;
	private SequenceBarrier stepFourSequenceBarrier;
	private ResponseWriteHandler responseWriteHandler;
	private BatchEventProcessor<RequestEvent> stepFourBatchProcessor ;
	private final ExecutorService EXECUTOR = Executors.newFixedThreadPool(NUM_EVENT_PROCESSORS);
	private Sequencer sequencer;
	public DisruptorRpcRequestQueue(ServerRegister serverRegister){
		this(serverRegister,1024*8);
	}
	public DisruptorRpcRequestQueue(ServerRegister serverRegister,int bufferSize){
		this.serverRegister=serverRegister;
		this.BUFFER_SIZE=bufferSize;
		init();
	}
	public void init2(){
		ringBuffer =
	        new RingBuffer<RequestEvent>(RequestEvent.EVENT_FACTORY, 
	                BUFFER_SIZE);
		stepOneSequenceBarrier = ringBuffer.newBarrier();
		 deserializerHandler=new RequestDeserializerHandler();
		 stepOneBatchProcessor =
		        new BatchEventProcessor<RequestEvent>(ringBuffer, stepOneSequenceBarrier, deserializerHandler);
		
		 stepTwoSequenceBarrier=ringBuffer.newBarrier(stepOneBatchProcessor.getSequence());
		 methodInvokeHandler=new MethdoInvokeHandler(serverRegister);
		 stepTwoBatchProcessor =
		        new BatchEventProcessor<RequestEvent>(ringBuffer, stepTwoSequenceBarrier, methodInvokeHandler);
		
		 stepThreeSequenceBarrier=ringBuffer.newBarrier(stepTwoBatchProcessor.getSequence());
		 serializerHandler=new ResponseSerializerHandler();
		 stepThreeBatchProcessor =
		        new BatchEventProcessor<RequestEvent>(ringBuffer, stepThreeSequenceBarrier, serializerHandler);
		 
		 ringBuffer.setGatingSequences(stepThreeBatchProcessor.getSequence());
		 //sequencer=ringBuffer.;
		 EXECUTOR.submit(stepOneBatchProcessor);
		 EXECUTOR.submit(stepTwoBatchProcessor);
		 EXECUTOR.submit(stepThreeBatchProcessor);
		 /*EXECUTOR.submit(new Runnable() {
			
			@Override
			public void run() {
				while(!Thread.currentThread().isInterrupted()){
						int op1=DisruptorRpcRequestQueue.this.deserializerHandler.opaque;
						int op2=DisruptorRpcRequestQueue.this.methodInvokeHandler.opaque;
						int op3=DisruptorRpcRequestQueue.this.serializerHandler.opaque;
						System.out.println("op1="+op1+",op2="+op2+",op3="+op3);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							Thread.currentThread().interrupt();
						}
				}
				
			}
		});*/
	}
	public void init(){
		ClaimStrategy claim=new SingleThreadedClaimStrategy(BUFFER_SIZE);
		WaitStrategy wait=new BlockingWaitStrategy();
		ringBuffer =
	        new RingBuffer<RequestEvent>(RequestEvent.EVENT_FACTORY, 
	                claim,wait);
		/*stepOneSequenceBarrier = ringBuffer.newBarrier();
		 deserializerHandler=new RequestDeserializerHandler();
		 stepOneBatchProcessor =
		        new BatchEventProcessor<RequestEvent>(ringBuffer, stepOneSequenceBarrier, deserializerHandler);
		*/
		 stepTwoSequenceBarrier=ringBuffer.newBarrier();//stepOneBatchProcessor.getSequence());
		 methodInvokeHandler=new MethdoInvokeHandler(serverRegister);
		 stepTwoBatchProcessor =
		        new BatchEventProcessor<RequestEvent>(ringBuffer, stepTwoSequenceBarrier, methodInvokeHandler);
		
		stepThreeSequenceBarrier=ringBuffer.newBarrier(stepTwoBatchProcessor.getSequence());
		 serializerHandler=new ResponseSerializerHandler();
		 stepThreeBatchProcessor =
		        new BatchEventProcessor<RequestEvent>(ringBuffer, stepThreeSequenceBarrier, serializerHandler);
		 stepFourSequenceBarrier=ringBuffer.newBarrier(stepThreeBatchProcessor.getSequence());
		 responseWriteHandler=new ResponseWriteHandler();
		 stepFourBatchProcessor =
		        new BatchEventProcessor<RequestEvent>(ringBuffer, stepFourSequenceBarrier, responseWriteHandler);
		 
		 ringBuffer.setGatingSequences(stepFourBatchProcessor.getSequence());
		 //sequencer=ringBuffer.;
		 //EXECUTOR.submit(stepOneBatchProcessor);
		 EXECUTOR.submit(stepTwoBatchProcessor);
		EXECUTOR.submit(stepThreeBatchProcessor);
		EXECUTOR.submit(stepFourBatchProcessor);
	}
	@Override
	public boolean addRpcRequest(RpcRequestCommand command,Session session) {
		long sequence=ringBuffer.next();
		//System.out.println("add sequence="+sequence);
		RequestEvent event=ringBuffer.get(sequence);
		event.setRequest(command);
		event.setSession(session);
		ringBuffer.publish(sequence);
		return true;
	}
	public void addRpcRequest(int opaque,int length,IoBuffer buffer,Session session){
		long sequence=ringBuffer.next();
		RequestEvent event=ringBuffer.get(sequence);
		RpcRequestCommand command=event.getRequest();
		command.setOpaque(opaque);
		//command.setHexDigests(MD5.toHex(buffer));
		buffer.get(event.getMethodDigest().digest);//read method digest
		length=length-16;
		if(length>0){									
			byte data[]=new byte[length];
			buffer.get(data);
			command.setRequestData(data);
		}
		event.setSession(session);
		ringBuffer.publish(sequence);
	}
	@Override
	public void dispose() {
		stepOneBatchProcessor.halt();
        stepTwoBatchProcessor.halt();
        stepThreeBatchProcessor.halt();
        

	}

}
