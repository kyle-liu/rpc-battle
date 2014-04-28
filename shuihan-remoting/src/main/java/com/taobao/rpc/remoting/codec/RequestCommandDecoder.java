package com.taobao.rpc.remoting.codec;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.gecko.core.buffer.IoBuffer;
import com.taobao.gecko.core.core.CodecFactory;
import com.taobao.gecko.core.core.Session;
import com.taobao.rpc.remoting.command.Constants;
import com.taobao.rpc.remoting.command.RpcCommandFactory;
import com.taobao.rpc.remoting.command.RpcRequestCommand;
import com.taobao.rpc.remoting.command.OpCode;


public class RequestCommandDecoder implements CodecFactory.Decoder {

    private static final Log log = LogFactory.getLog(RequestCommandDecoder.class);


    public Object decode(final IoBuffer in, final Session session) {
        final DecoderState decoderState = (DecoderState) session.getAttribute(RpcWrapDecoder.DECODER_STATE_KEY);
        if (decoderState.decodeCommand == null) {
            if (in.remaining() < Constants.REQUEST_HEADER_LENGTH) {
                return null;
            }
            else {
                this.decodeHeader(in, session, decoderState);
            }
        }
        if (decoderState.decodeCommand != null) {
            final RpcRequestCommand requestCommand = (RpcRequestCommand) decoderState.decodeCommand;
            if (in.remaining() < requestCommand.getTotalBodyLength()) {
                return null;
            }
            else {
                return this.decodeContent(in, decoderState, requestCommand);
            }
        }
        return null;
    }


    private Object decodeContent(final IoBuffer in, final DecoderState decoderState,
            final RpcRequestCommand requestCommand) {
        if (requestCommand.getTotalBodyLength() > 0) {
            if (requestCommand.getHeaderLength() > 0) {
                final byte[] header = new byte[requestCommand.getHeaderLength()];
                in.get(header);
                requestCommand.setHeader(header);
            }
            final int bodyLen = requestCommand.getTotalBodyLength() - requestCommand.getHeaderLength();
            if (bodyLen > 0) {
                final byte[] body = new byte[bodyLen];
                in.get(body);
                requestCommand.setBody(body);
            }
        }
        requestCommand.decodeContent();
        decoderState.decodeCommand = null;// reset status
        return requestCommand;
    }


    private void decodeHeader(final IoBuffer in, final Session session, final DecoderState decoderState) {
        final byte magic = in.get();
        if (magic != Constants.REQUEST_MAGIC) {
            log.error("请求命令的magic数值错误,expect " + Constants.REQUEST_MAGIC + ",real " + magic);
            session.close();
            return;
        }
        final OpCode opCode = OpCode.valueOf(in.get());
        final RpcRequestCommand requestCommand =
                (RpcRequestCommand) RpcCommandFactory.newRequestCommand(opCode);
        requestCommand.setOpCode(opCode);
        requestCommand.setHeaderLength(in.getShort());
        requestCommand.setTotalBodyLength(in.getInt());
        requestCommand.setOpaque(in.getInt());
        decoderState.decodeCommand = requestCommand;
    }
}
