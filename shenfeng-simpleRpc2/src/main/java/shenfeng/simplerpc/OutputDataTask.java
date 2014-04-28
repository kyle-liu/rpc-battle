package shenfeng.simplerpc;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

class OutputDataTask implements Runnable {

	class CompressTask implements Runnable {

		ByteArrayOutputStream byteStream = new ByteArrayOutputStream(20000);

		private final List<Signal> signalBuffer = new ArrayList<Signal>();

		@Override
		public void run() {
			try {
				while (isRunning) {
					byteStream.reset();
					DataOutputStream dataOutput = new DataOutputStream(byteStream);
					{
						Signal s = queue.take(); // 为了防止穷忙状态，第一个元素使用阻塞读
						dataOutput.writeShort(s.getId());
						byte[] data = s.getOut();
						dataOutput.writeShort(data.length);
						dataOutput.write(data);
					}
					{
						signalBuffer.clear();
						int size = queue.drainTo(signalBuffer);
						for (int i = 0; i < size; i++) {
							Signal s = signalBuffer.get(i);
							dataOutput.writeShort(s.getId());
							byte[] data = s.getOut();
							dataOutput.writeShort(data.length);
							dataOutput.write(data);
						}
					}
					compressQueue.put(Utils.compress(byteStream.toByteArray()));
					Thread.yield();
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private final BlockingQueue<Signal> queue = new ArrayBlockingQueue<Signal>(1000);

	private final BlockingQueue<byte[]> compressQueue = new ArrayBlockingQueue<byte[]>(100);

	private volatile boolean isRunning = true;

	private final DataOutputStream out;

	private final List<byte[]> bytesBuffer = new ArrayList<byte[]>();

	public OutputDataTask(OutputStream outputStream) {
		this.out = new DataOutputStream(outputStream);
		Thread thread = new Thread(new CompressTask(), "compressTask");
		thread.setDaemon(true);
		thread.start();
	}

	public void close() {
		isRunning = false;
	}

	public void put(Signal signal) {
		try {
			queue.put(signal);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void run() {
		try {
			while (isRunning) {
				{
					byte[] compress = compressQueue.take();
					out.writeInt(compress.length);
					out.write(compress);
				}

				bytesBuffer.clear();
				int size = compressQueue.drainTo(bytesBuffer);
				for (int i = 0; i < size; i++) {
					out.writeInt(bytesBuffer.get(i).length);
					out.write(bytesBuffer.get(i));
				}
				out.flush();
				Thread.yield();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}