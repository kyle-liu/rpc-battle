package shenfeng.simplerpc;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

class ByteBufferBackedOutputStream extends OutputStream {

	private final ByteBuffer buf;

	ByteBufferBackedOutputStream(ByteBuffer buf) {
		this.buf = buf;
	}

	@Override
	public synchronized void write(byte[] bytes, int off, int len) throws IOException {
		buf.put(bytes, off, len);
	}

	@Override
	public synchronized void write(int b) throws IOException {
		buf.put((byte) b);
	}

}
