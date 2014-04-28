package com.taobao.rpc.remoting.codec;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.gecko.core.buffer.IoBuffer;
import com.taobao.gecko.core.command.ResponseStatus;
import com.taobao.gecko.core.core.CodecFactory;
import com.taobao.gecko.core.core.Session;
import com.taobao.rpc.remoting.command.Constants;
import com.taobao.rpc.remoting.command.RpcBooleanAckCommand;
import com.taobao.rpc.remoting.command.RpcCommandFactory;
import com.taobao.rpc.remoting.command.RpcResponseCommand;
import com.taobao.rpc.remoting.command.OpCode;
import com.taobao.rpc.remoting.command.ResponseStatusCode;


public class ResponseCommandDecoder implements CodecFactory.Decoder {

    private static final Log log = LogFactory.getLog(ResponseCommandDecoder.class);


    public Object decode(final IoBuffer in, final Session session) {
        final DecoderState decoderState = (DecoderState) session.getAttribute(RpcWrapDecoder.DECODER_STATE_KEY);
        if (decoderState.decodeCommand == null) {
            if (in.remaining() < Constants.RESPONSE_HEADER_LENGTH) {
                return null;
            }
            else {
                this.decodeHeader(in, session, decoderState);
            }
        }
        if (decoderState.decodeCommand != null) {
            final RpcResponseCommand responseCommand = (RpcResponseCommand) decoderState.decodeCommand;
            if (in.remaining() < responseCommand.getTotalBodyLength()) {
                return null;
            }
            else {
                return this.decodeContent(in, decoderState, responseCommand);
            }
        }
        return null;

    }


    private Object decodeContent(final IoBuffer in, final DecoderState decoderState,
            final RpcResponseCommand responseCommand) {
        if (responseCommand.getTotalBodyLength() > 0) {
            if (responseCommand.getHeaderLength() > 0) {
                final byte[] header = new byte[responseCommand.getHeaderLength()];
                in.get(header);
                responseCommand.setHeader(header);
            }
            final int bodyLen = responseCommand.getTotalBodyLength() - responseCommand.getHeaderLength();
            if (bodyLen > 0) {
                final byte[] body = new byte[bodyLen];
                in.get(body);
                responseCommand.setBody(body);
            }
            responseCommand.decodeContent();
        }
        decoderState.decodeCommand = null;// reset status
        return responseCommand;
    }


    private void decodeHeader(final IoBuffer in, final Session session, final DecoderState decoderState) {
        final byte magic = in.get();
        if (magic != Constants.RESPONSE_MAGIC) {
            log.error("应答命令的magic数值错误,expect " + Constants.RESPONSE_MAGIC + ",real " + magic);
            session.close();
            return;
        }

        final OpCode opCode = OpCode.valueOf(in.get());
        final ResponseStatus responseStatus = ResponseStatusCode.valueOf(in.getShort());
        RpcResponseCommand responseCommand = null;
        if (responseStatus == ResponseStatus.NO_ERROR) {
            responseCommand = (RpcResponseCommand) RpcCommandFactory.newResponseCommand(opCode);
        }
        else {
            responseCommand = new RpcBooleanAckCommand(opCode);
        }
        responseCommand.setResponseHost(session.getRemoteSocketAddress());
        responseCommand.setOpCode(opCode);
        responseCommand.setResponseStatus(responseStatus);
        responseCommand.setHeaderLength(in.getShort());
        // skip reserved field
        in.skip(2);
        responseCommand.setTotalBodyLength(in.getInt());
        responseCommand.setOpaque(in.getInt());
        decoderState.decodeCommand = responseCommand;
    }

}
