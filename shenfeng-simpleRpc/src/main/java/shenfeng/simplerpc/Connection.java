package shenfeng.simplerpc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Connection {

	private Socket socket;
	private InputStream in;
	private OutputStream out;
	private SocketChannel channel;
	private ByteBuffer buffer;

	public Connection(Socket socket) {
		this.socket = socket;
	}

	public Connection(String remoteHost, int remotePort) {
		try {
			channel = SocketChannel.open(new InetSocketAddress(remoteHost, remotePort));
			channel.socket().setReceiveBufferSize(1024 * 64);
			channel.socket().setTcpNoDelay(true);
			buffer = ByteBuffer.allocateDirect(5125);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void close() {
		try {
			if (socket != null)
				socket.close();
			else {
				in.close();
				out.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ByteBuffer getBuffer() {
		return buffer;
	}

	public SocketChannel getChannel() {
		return channel;
	}

	public InputStream getInputStream() {
		if (in == null) {
			try {
				in = new BufferedInputStream(socket.getInputStream());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return in;
	}

	public OutputStream getOutputStream() {
		if (out == null) {
			try {
				out = new BufferedOutputStream(socket.getOutputStream());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return out;
	}

}
