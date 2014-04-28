package shenfeng.simplerpc;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

class InputDataTask implements Runnable {

	private volatile boolean isRunning = true;

	private final Notifier notifier;

	private final DataInputStream in;

	private final Closeable closeHandler;

	public InputDataTask(InputStream inputStream, Notifier notifier, Closeable closeHandler) {
		this.in = new DataInputStream(inputStream);
		this.notifier = notifier;
		this.closeHandler = closeHandler;
	}

	public void close() {
		isRunning = false;
	}

	@Override
	public void run() {
		while (isRunning) {
			try {
				int length = in.readInt();
				byte[] compress = new byte[length];
				in.readFully(compress);
				byte[] datas = Utils.uncompress(compress);
				DataInputStream in = new DataInputStream(new ByteArrayInputStream(datas));
				while (in.available() > 0) {
					short id = in.readShort();
					short simpleLength = in.readShort();
					byte[] data = new byte[simpleLength];
					in.read(data);
					Signal signal = Utils.getSignal(id);
					signal.setIn(data);
					notifier.notifySignal(signal);
				}
				Thread.yield();
			} catch (Exception e) {
				try {
					closeHandler.close();
				} catch (IOException e1) {
					// ignore
				}
			}
		}
	}

}