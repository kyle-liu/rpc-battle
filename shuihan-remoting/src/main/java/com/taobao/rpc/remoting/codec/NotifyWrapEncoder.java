package com.taobao.rpc.remoting.codec;

import com.taobao.gecko.core.buffer.IoBuffer;
import com.taobao.gecko.core.command.RequestCommand;
import com.taobao.gecko.core.command.ResponseCommand;
import com.taobao.gecko.core.core.CodecFactory;
import com.taobao.gecko.core.core.Session;

public class NotifyWrapEncoder implements CodecFactory.Encoder {
	private final RequestCommandEncoder requestEncoder;
	private final ResponseCommandEncoder responseEncoder;

	public NotifyWrapEncoder() {
		this.requestEncoder = new RequestCommandEncoder();
		this.responseEncoder = new ResponseCommandEncoder();
	}

	public IoBuffer encode(final Object message, final Session session) {
		if (message instanceof RequestCommand) {
			return this.requestEncoder.encode(message, session);
		} else if (message instanceof ResponseCommand) {
			return this.responseEncoder.encode(message, session);
		} else {
			throw new IllegalArgumentException("Unknow command type "
					+ message.getClass().getName());
		}
	}
}
