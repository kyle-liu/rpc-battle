package com.taobao.rpc.remoting.codec;

import com.taobao.gecko.core.buffer.IoBuffer;
import com.taobao.gecko.core.core.CodecFactory;
import com.taobao.gecko.core.core.CodecFactory.Decoder;
import com.taobao.gecko.core.core.Session;
import com.taobao.gecko.core.util.RemotingUtils;
import com.taobao.rpc.remoting.command.Constants;


public class RpcWrapDecoder implements CodecFactory.Decoder {
    private final RequestCommandDecoder requestDecoder;
    private final ResponseCommandDecoder responseDecoder;


    public RpcWrapDecoder() {
        this.requestDecoder = new RequestCommandDecoder();
        this.responseDecoder = new ResponseCommandDecoder();
    }


    public RequestCommandDecoder getRequestDecoder() {
        return this.requestDecoder;
    }


    public ResponseCommandDecoder getResponseDecoder() {
        return this.responseDecoder;
    }

    public static final String DECODER_STATE_KEY = RpcWrapDecoder.class.getName() + ".STATE";
    public static final String CURRENT_DECODER = RpcWrapDecoder.class.getName() + ".DECODER";


    public Object decode(final IoBuffer buff, final Session session) {
        if (!buff.hasRemaining()) {
            return null;
        }
        final DecoderState decoderState = this.getDecoderStateFromSession(session);
        if (decoderState.decodeCommand == null) {
            return this.decodeNewCommand(buff, session);
        }
        else {
            return this.decodeCurrentCommand(buff, session);
        }

    }


    private Object decodeCurrentCommand(final IoBuffer buff, final Session session) {
        return ((Decoder) session.getAttribute(CURRENT_DECODER)).decode(buff, session);
    }


    private Object decodeNewCommand(final IoBuffer buff, final Session session) {
        final byte magic = buff.get(buff.position());
        if (magic == Constants.REQUEST_MAGIC) {
            return this.decodeRequestCommand(buff, session);
        }
        else if (magic == Constants.RESPONSE_MAGIC) {
            return this.decodeResponseCommand(buff, session);
        }
        else {
            throw new IllegalArgumentException("Unknow command magic " + magic + " Buffer: "
                    + RemotingUtils.dumpBuffer(buff).toString());
        }
    }


    private Object decodeResponseCommand(final IoBuffer buff, final Session session) {
        session.setAttribute(CURRENT_DECODER, this.responseDecoder);
        return this.responseDecoder.decode(buff, session);
    }


    private Object decodeRequestCommand(final IoBuffer buff, final Session session) {
        session.setAttribute(CURRENT_DECODER, this.requestDecoder);
        return this.requestDecoder.decode(buff, session);
    }


    /**
     * 从连接属性中获取当前的decode状态，如果不存在就创建
     * 
     * @param session
     * @return
     */
    private DecoderState getDecoderStateFromSession(final Session session) {
        DecoderState decoderState = (DecoderState) session.getAttribute(DECODER_STATE_KEY);
        if (decoderState == null) {
            decoderState = new DecoderState();
            final DecoderState oldState =
                    (DecoderState) session.setAttributeIfAbsent(RpcWrapDecoder.DECODER_STATE_KEY, decoderState);
            if (oldState != null) {
                decoderState = oldState;
            }
        }
        return decoderState;
    }
}
