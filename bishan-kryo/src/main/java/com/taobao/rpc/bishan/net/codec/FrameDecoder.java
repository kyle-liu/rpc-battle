package com.taobao.rpc.bishan.net.codec;

import java.nio.ByteBuffer;

import com.taobao.rpc.bishan.net.reactor.BsNetClient;
import com.taobao.rpc.bishan.net.util.BsThreadPool;

/**
 * 针对流式消息的处理
 * 
 * From Netty
 * 
 * @author bishan.ct
 * 
 */
public abstract class FrameDecoder {

	private ByteBuffer cumulation;

	/**
	 * 收到字节流，对字节流进行处理
	 * 
	 * @param clientChannel
	 * @param input
	 * @throws Exception
	 */
	public void decodeMsg(BsNetClient clientChannel, ByteBuffer input) {
		// cumulation表示缓存的数据
		// 1.如果之前没有缓存数据
		if (cumulation == null) {
			// 1.1直接尝试decode
			// Wrap in try / finally.
			//
			// See https://github.com/netty/netty/issues/364
			try {
				// the cumulation buffer is not created yet so just pass the
				// input to callDecode(...) method
				callDecode(clientChannel, input);
			} finally {
				// 1.2 decode之后还有剩余，则将剩下的放到新的ByteBuffer
				// 如果在callDecode并且完整解析出消息之后，抛出异常，可能会使得input剩余的数据丢失
				// https://github.com/netty/netty/issues/364
				if (input.remaining() > 0) {
					// seems like there is something readable left in the input
					// buffer.
					// So create the cumulation buffer and copy the input into
					// it
					cumulation = ByteBuffer.allocate(
							Math.max(input.remaining(), 256)).put(input);
				}
			}

		} else {
			// 2.之前就有缓存数据

			// 假设新到的数据有20字节可写
			int readable = input.remaining();
//			// cumlation只剩余10可写（即还需要10字节的空间）
//			int writable = cumulation.p() - cumulation.position();
//			int w = writable - readable;
//
//			// 缓冲区可写的大于新写入的
//			if (w > 0) {
//				fit = true;
//			}
			cumulation.flip();
			
			ByteBuffer buf= ByteBuffer.allocate(readable + cumulation.remaining());
			buf.put(cumulation);
			buf.put(input);
			buf.flip();

			// Wrap in try / finally.
			// See https://github.com/netty/netty/issues/364
			try {
				callDecode(clientChannel, buf);
			} finally {
				if (buf.remaining() == 0) {
					// nothing readable left so reset the state
					cumulation = null;
				} else {
					cumulation = ByteBuffer.allocate(Math.max(buf.remaining(),256));
					cumulation.put(buf);
				}
			}

		}
	}

	/**
	 * 将buffer解析为对象
	 * 
	 * @param bsNet
	 * @param buffer
	 * @return
	 * @throws Exception
	 */
	protected abstract Object decode(BsNetClient bsNet, ByteBuffer buffer);

	private void callDecode(final BsNetClient clientChannel, ByteBuffer cumulation) {

		while (cumulation.remaining() > 0) {
			int oldReaderIndex = cumulation.position();

			// 是否解析成功的逻辑由子类实现
			final Object frame = decode(clientChannel, cumulation);
			if (frame == null) {
				if (oldReaderIndex == cumulation.position()) {
					// Seems like more data is required.
					// Let us wait for the next notification.
					break;
				} else {
					// Previous data has been discarded.
					// Probably it is reading on.
					continue;
				}
			}
			
			clientChannel.onRead(frame);
		}
	}

	public void cleanup(BsNetClient clientChannel) {
		ByteBuffer cumulation = this.cumulation;
		if (cumulation == null) {
			return;
		}

		this.cumulation = null;

		if (cumulation.remaining() > 0) {
			// Make sure all frames are read before notifying a closed channel.
			callDecode(clientChannel, cumulation);
		}
	}

}
