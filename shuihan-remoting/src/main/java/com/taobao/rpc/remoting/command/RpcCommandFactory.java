package com.taobao.rpc.remoting.command;

import com.taobao.gecko.core.command.CommandFactory;
import com.taobao.gecko.core.command.CommandHeader;
import com.taobao.gecko.core.command.RequestCommand;
import com.taobao.gecko.core.command.ResponseCommand;
import com.taobao.gecko.core.command.ResponseStatus;
import com.taobao.gecko.core.command.kernel.BooleanAckCommand;
import com.taobao.gecko.core.command.kernel.HeartBeatRequestCommand;


/**
 * 
 * 
 * 协议命令工厂类，任何实现的协议都需要在此工厂注册，提供给编解码器使用
 * 
 * @author boyan
 * 
 * @since 1.0, 2009-12-18 上午11:13:33
 */

public final class RpcCommandFactory implements CommandFactory {

    public BooleanAckCommand createBooleanAckCommand(final CommandHeader request, final ResponseStatus responseStatus,
            final String errorMsg) {
        return new RpcBooleanAckCommand(request, responseStatus, errorMsg);
    }


    public HeartBeatRequestCommand createHeartBeatCommand() {
        return new RpcHeartBeatCommand();
    }


    public static final ResponseCommand newResponseCommand(final OpCode opCode) {
        ResponseCommand responseCommand = null;
        switch (opCode) {
        case SEND_SUBSCRIPTION:
            responseCommand = new RpcAckCommand(opCode);
            break;
        case HEARTBEAT:
            responseCommand = new RpcBooleanAckCommand(opCode);
            break;
        default:
            throw new IllegalArgumentException("Unknow response command for " + opCode.name());
        }
        return responseCommand;
    }


    public static final RequestCommand newRequestCommand(final OpCode opCode) {
        RequestCommand requestCommand = null;
        switch (opCode) {
        case HEARTBEAT:
            requestCommand = new RpcHeartBeatCommand(opCode);
            break;
        case SEND_SUBSCRIPTION:
            requestCommand = new RpcInvokeCommand(opCode);
            break;
        default:
            throw new IllegalArgumentException("Could not new request command by opCode,opCode=" + opCode);
        }
        return requestCommand;
    }

}
