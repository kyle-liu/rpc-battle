package com.taobao.rpc.remoting.codec;

import com.taobao.gecko.core.buffer.IoBuffer;
import com.taobao.gecko.core.core.CodecFactory;
import com.taobao.gecko.core.core.Session;
import com.taobao.gecko.service.impl.DefaultConnection;
import com.taobao.rpc.remoting.command.Constants;
import com.taobao.rpc.remoting.command.RpcRequestCommand;

public class RequestCommandEncoder implements CodecFactory.Encoder {
	static final IoBuffer EMPTY = IoBuffer.allocate(0);

	public IoBuffer encode(final Object message, final Session session) {
		if (message instanceof RpcRequestCommand) {
			final RpcRequestCommand requestCommand = (RpcRequestCommand) message;
			try {
				requestCommand.encodeContent();
				final IoBuffer buffer = IoBuffer
						.allocate(Constants.REQUEST_HEADER_LENGTH
								+ requestCommand.getTotalBodyLength());
				buffer.setAutoExpand(true);
				this.putHeader(message, requestCommand, buffer);
				this.putContent(message, requestCommand, buffer);
				buffer.flip();
				return buffer;
			} catch (final Exception e) {
				session.getHandler().onExceptionCaught(session, e);
				// 捕捉mashall异常，返回给用户
				final DefaultConnection conn = (DefaultConnection) session
						.getAttribute(com.taobao.gecko.core.command.Constants.CONNECTION_ATTR);
				if (conn != null) {
					conn.notifyClientException(requestCommand, e);
				}
				// 最后返回一个空buffer
				return EMPTY.slice();
			}

		} else {
			throw new IllegalArgumentException("Illegal request message,"
					+ message);
		}
	}

	private void putContent(final Object message,
			final RpcRequestCommand requestCommand, final IoBuffer buffer) {
		if (requestCommand.getHeaderLength() > 0) {
			if (requestCommand.getHeader() == null) {
				throw new IllegalArgumentException("Illegal request header,"
						+ message);
			}
			buffer.put(requestCommand.getHeader());
		}
		if (requestCommand.getTotalBodyLength()
				- requestCommand.getHeaderLength() > 0) {
			if (requestCommand.getBody() == null) {
				throw new IllegalArgumentException("Illegal request body,"
						+ message);
			}
			buffer.put(requestCommand.getBody());
		}
	}

	private void putHeader(final Object message,
			final RpcRequestCommand requestCommand, final IoBuffer buffer) {
		buffer.put(requestCommand.getMagic());
		buffer.put(requestCommand.getOpCode().getValue());
		buffer.putShort(requestCommand.getHeaderLength());
		buffer.putInt(requestCommand.getTotalBodyLength());
		buffer.putInt(requestCommand.getOpaque());
	}
}
