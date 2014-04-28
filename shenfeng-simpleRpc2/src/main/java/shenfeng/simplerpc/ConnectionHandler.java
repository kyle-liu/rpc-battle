package shenfeng.simplerpc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectionHandler implements Closeable {

	class BizTask implements Runnable {

		private final Signal signal;

		public BizTask(Signal signal) {
			this.signal = signal;
		}

		@Override
		public void run() {
			try {
				ByteArrayInputStream in = new ByteArrayInputStream(signal.getIn());
				Serializer inSerializer = ref.serializer(in);
				int methodCode = inSerializer.readObject(Integer.class);
				Method method = ref.getMethod(methodCode);
				Class<?>[] types = method.getParameterTypes();
				Object result = method.invoke(ref.getServiceInstance(), inSerializer.readObjects(types));
				Class<?> rtype = method.getReturnType();
				if (rtype != void.class) {
					ByteArrayOutputStream out = new ByteArrayOutputStream(5000);
					Serializer outSerializer = ref.serializer(out);
					outSerializer.writeObject(rtype, result);
					outSerializer.flush();
					signal.setOut(out.toByteArray());
					outputDataTask.put(signal);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

	}

	private final Notifier notifier = new Notifier() {

		private final ExecutorService pool = Executors
				.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);

		@Override
		public void notifySignal(Signal signal) {
			pool.execute(new BizTask(signal));
		}

	};

	private final AcceptLoop ref;

	private final Socket socket;

	private final InputDataTask inputDataTask;

	private final OutputDataTask outputDataTask;

	public ConnectionHandler(Socket socket, AcceptLoop ref) {
		this.ref = ref;
		try {
			this.socket = socket;
			socket.setTcpNoDelay(true);
			inputDataTask = new InputDataTask(new BufferedInputStream(socket.getInputStream()), notifier, this);
			new Thread(inputDataTask, "inputDataTask").start();
			outputDataTask = new OutputDataTask(new BufferedOutputStream(socket.getOutputStream()));
			new Thread(outputDataTask, "OutputDataTask").start();
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
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
