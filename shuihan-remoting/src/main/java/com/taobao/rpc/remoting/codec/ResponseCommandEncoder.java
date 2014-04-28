package com.taobao.rpc.remoting.codec;

import com.taobao.gecko.core.buffer.IoBuffer;
import com.taobao.gecko.core.core.CodecFactory;
import com.taobao.gecko.core.core.Session;
import com.taobao.rpc.remoting.command.Constants;
import com.taobao.rpc.remoting.command.RpcResponseCommand;
import com.taobao.rpc.remoting.command.ResponseStatusCode;

public class ResponseCommandEncoder implements CodecFactory.Encoder {

	public IoBuffer encode(final Object message, final Session session) {
		if (message instanceof RpcResponseCommand) {
			final RpcResponseCommand responseCommand = (RpcResponseCommand) message;
			responseCommand.encodeContent();
			final IoBuffer buffer = IoBuffer
					.allocate(Constants.RESPONSE_HEADER_LENGTH
							+ responseCommand.getTotalBodyLength());
			buffer.setAutoExpand(true);
			this.putHeader(responseCommand, buffer);
			this.putContent(message, responseCommand, buffer);
			buffer.flip();
			return buffer;

		} else {
			throw new IllegalArgumentException("Illegal response message,"
					+ message);
		}
	}

	private void putContent(final Object message,
			final RpcResponseCommand responseCommand, final IoBuffer buffer) {
		if (responseCommand.getHeaderLength() > 0) {
			if (responseCommand.getHeader() == null) {
				throw new IllegalArgumentException("Illegal response header,"
						+ message);
			}
			buffer.put(responseCommand.getHeader());
		}
		if (responseCommand.getTotalBodyLength()
				- responseCommand.getHeaderLength() > 0) {
			if (responseCommand.getBody() == null) {
				throw new IllegalArgumentException("Illegal response body,"
						+ message);
			}
			buffer.put(responseCommand.getBody());
		}
	}

	private void putHeader(final RpcResponseCommand responseCommand,
			final IoBuffer buffer) {
		buffer.put(responseCommand.getMagic());
		buffer.put(responseCommand.getOpCode().getValue());
		buffer.putShort(ResponseStatusCode.getValue(responseCommand
				.getResponseStatus()));
		buffer.putShort(responseCommand.getHeaderLength());
		buffer.putShort(Constants.RESERVED);
		buffer.putInt(responseCommand.getTotalBodyLength());
		buffer.putInt(responseCommand.getOpaque());
	}

}
