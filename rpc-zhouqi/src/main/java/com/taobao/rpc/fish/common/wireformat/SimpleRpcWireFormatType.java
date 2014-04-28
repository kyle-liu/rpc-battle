package com.taobao.rpc.fish.common.wireformat;


import com.taobao.gecko.core.buffer.IoBuffer;
import com.taobao.gecko.core.command.CommandFactory;
import com.taobao.gecko.core.command.CommandHeader;
import com.taobao.gecko.core.command.ResponseStatus;
import com.taobao.gecko.core.command.kernel.BooleanAckCommand;
import com.taobao.gecko.core.command.kernel.HeartBeatRequestCommand;
import com.taobao.gecko.core.core.CodecFactory;
import com.taobao.gecko.core.core.Session;
import com.taobao.gecko.core.nio.impl.Reactor;
import com.taobao.gecko.core.nio.impl.SelectorManager;
import com.taobao.gecko.service.config.WireFormatType;
import com.taobao.rpc.fish.common.command.BaseCommand;
import com.taobao.rpc.fish.common.command.RpcBooleanCommand;
import com.taobao.rpc.fish.common.command.RpcHeartBeatCommand;
import com.taobao.rpc.fish.common.command.RpcRequestCommand;
import com.taobao.rpc.fish.common.command.RpcResponseCommand;
import com.taobao.rpc.fish.common.util.MD5;
import com.taobao.rpc.fish.server.queue.RpcRequestQueue;

public class SimpleRpcWireFormatType extends WireFormatType{

	@Override
	public String getScheme() {
		// TODO Auto-generated method stub
		return "rpc";
	}

	@Override
	public CodecFactory newCodecFactory() {
		// TODO Auto-generated method stub
		return new SimpleRpcCodecFactory();
	}

	@Override
	public CommandFactory newCommandFactory() {
		// TODO Auto-generated method stub
		return new SimpleRpcCommandFactory();
	}

	@Override
	public String name() {
		// TODO Auto-generated method stub
		return "simple-rpc";
	}
	public static class SimpleRpcCodecFactory implements CodecFactory{

		public static Encoder encoder=new Encoder() {
			
			@Override
			public IoBuffer encode(Object message, Session session) {
				// TODO Auto-generated method stub
				/*if(message instanceof RpcHeartBeatCommand){
					System.out.println("����������Ϣ:"+message.toString());
					throw new RuntimeException("����������Ϣ");
				}else{
					System.out.println("������Ϣ:"+message.toString());
				}*/
				return ((BaseCommand)message).encode();
			}
		};
		public static Decoder decoder=new Decoder() {
			public int headerLength=9;
			@Override
			public Object decode(IoBuffer buff, Session session){
				if (buff == null || buff.remaining()<headerLength) {
                    return null;
                }
                buff.mark();
                byte type=buff.get();
                int opaque=buff.getInt();
                int length=buff.getInt();
                if(buff.remaining()<length){
                	buff.reset();
                	return null;
                }
              // System.out.println("�յ���Ϣ��type="+type+",opq="+opaque+",length="+length);
                if(length>0){
                	// data[]=new byte[length];
                	//buff.get(data);
                	return decode(type, opaque,length,buff,session);
                } else if(length==0){
                	return decode(type,opaque, length,null,session);
                }else{
                	throw new RuntimeException("�����쳣��length����ȷ��"+length);
                }              
			}
			/**
			 * type:
			 * @param type
			 * @param length
			 * @param data
			 * @return
			 */
			public Object decode(byte type,int opaque,int length,IoBuffer buffer,Session session){
				switch(type){
				case 0:return decodeRpcRequest(opaque,length,buffer,session);
				case 1:return decodeRpcResponse(opaque,length,buffer);
				case 3:return decodeRpcHeartBeat(opaque,length,buffer);
				case 2:return decodeRpcBoolean(opaque,length,buffer);
					default: throw new RuntimeException("�����쳣���޷�ʶ�����Ϣ����type="+type);
				}
			}
			public Object decodeRpcRequest(int opaque,int length,IoBuffer buffer,Session session){
				
				
				Reactor reactor=(Reactor)session.getAttribute(SelectorManager.REACTOR_ATTRIBUTE);
				if(reactor!=null){
					RpcRequestQueue queue=reactor.getRequestQueue();
					if(queue!=null){
						queue.addRpcRequest(opaque,length,buffer, session);
						return this.Skip_Message;
					}					
				}
				RpcRequestCommand command=new RpcRequestCommand(opaque);
				command.setHexDigests(MD5.toHex(buffer));
				length=length-16;
				if(length>0){									
					byte data[]=new byte[length];
					buffer.get(data);
					command.setRequestData(data);
				}
				return command;
			}
			public RpcBooleanCommand decodeRpcBoolean(int opaque,int length,IoBuffer buffer){
				byte data[]=null;
				if(length>0){
					data=new byte[length];
					buffer.get(data);
				}
				RpcBooleanCommand command=new RpcBooleanCommand(opaque, data);
				return command;
			}
			public RpcResponseCommand decodeRpcResponse(int opaque,int length,IoBuffer buffer){
				byte data[]=null;
				if(length>0){
					data=new byte[length];
					buffer.get(data);
				}
				RpcResponseCommand command=new RpcResponseCommand(opaque,data);
				return command;
			}
			public RpcHeartBeatCommand decodeRpcHeartBeat(int opaque,int length,IoBuffer buffer){
				
				return new RpcHeartBeatCommand(opaque);
			}
		};
		@Override
		public Encoder getEncoder() {
			// TODO Auto-generated method stub
			return encoder;
		}

		@Override
		public Decoder getDecoder() {
			// TODO Auto-generated method stub
			return decoder;
		}
		
	}
	
	 static class SimpleRpcCommandFactory implements CommandFactory{

		@Override
		public BooleanAckCommand createBooleanAckCommand(CommandHeader request,
				ResponseStatus responseStatus, String errorMsg) {
			BooleanAckCommand command=new RpcBooleanCommand();
			command.setResponseStatus(responseStatus);
			command.setErrorMsg(errorMsg);
			command.setOpaque(request.getOpaque());
			return command;
		}

		@Override
		public HeartBeatRequestCommand createHeartBeatCommand() {
			// TODO Auto-generated method stub
			return new RpcHeartBeatCommand();
		}
		 
	 }
}
