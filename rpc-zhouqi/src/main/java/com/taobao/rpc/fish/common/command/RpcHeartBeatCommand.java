package com.taobao.rpc.fish.common.command;

import com.taobao.gecko.core.command.CommandHeader;
import com.taobao.gecko.core.command.kernel.HeartBeatRequestCommand;
import com.taobao.gecko.core.util.OpaqueGenerator;

public class RpcHeartBeatCommand extends BaseCommand implements HeartBeatRequestCommand{

	public RpcHeartBeatCommand(int opaque) {
		super(BaseCommand.RPC_HEARTBEAT);
		this.setOpaque(opaque);
	}
	public RpcHeartBeatCommand() {
		super(BaseCommand.RPC_HEARTBEAT);
		// TODO Auto-generated constructor stub
		this.setOpaque(OpaqueGenerator.getNextOpaque());
	}

	@Override
	public byte[] getData() {
		return null;
	}

	@Override
	public CommandHeader getRequestHeader() {
		// TODO Auto-generated method stub
		return new CommandHeader(){

			@Override
			public int getOpaque() {
				// TODO Auto-generated method stub
				return RpcHeartBeatCommand.this.getOpaque();
			}
			
		};
	}

}
