package shenfeng.simplerpc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Connection implements Closeable {

	private final Notifier notifier = new Notifier() {

		@Override
		public void notifySignal(Signal signal) {
		}

	};

	private final InputDataTask inputDataTask;

	private final OutputDataTask outputDataTask;

	private final Socket socket;

	public Connection(String remoteHost, int remotePort) throws UnknownHostException, IOException {
		try {
			this.socket = new Socket(remoteHost, remotePort);
			socket.setTcpNoDelay(true);
			socket.setReceiveBufferSize(1024 * 64);
			inputDataTask = new InputDataTask(new BufferedInputStream(socket.getInputStream()), notifier, this);
			Thread t1 = new Thread(inputDataTask, "inputDataTask");
			t1.setDaemon(true);
			t1.start();
			outputDataTask = new OutputDataTask(new BufferedOutputStream(socket.getOutputStream()));
			Thread t2 = new Thread(outputDataTask, "OutputDataTask");
			t2.setDaemon(true);
			t2.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() {
		inputDataTask.close();
		outputDataTask.close();
		try {
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public InputStream getInputStream() {
		Signal signal = Utils.getCurrentThreadSignal();
		return new ByteArrayInputStream(signal.getIn());
	}

	public OutputStream getOutputStream() {
		return new ByteArrayOutputStream(5125) {
			@Override
			public void flush() throws IOException {
				super.flush();
				Signal signal = Utils.getCurrentThreadSignal();
				signal.setOut(toByteArray());
				outputDataTask.put(signal);
			}
		};
	}

}
