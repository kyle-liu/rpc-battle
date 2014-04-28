package shenfeng.simplerpc;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.Adler32;

import org.xerial.snappy.Snappy;

public class Utils {

	private static final Map<Short, Signal> id2Signal = new ConcurrentHashMap<Short, Signal>();

	private static ThreadLocal<Signal> signal = new ThreadLocal<Signal>() {
		@Override
		protected Signal initialValue() {
			return createSignal();
		}
	};

	private static ThreadLocal<byte[]> zipBuffer = new ThreadLocal<byte[]>() {
		@Override
		protected byte[] initialValue() {
			return new byte[1024 * 1024];
		}
	};

	public static byte[] compress(byte[] input) {
		//		Deflater compresser = new Deflater();
		//		compresser.setInput(input);
		//		compresser.finish();
		//		byte[] out = zipBuffer.get();
		//		int size = compresser.deflate(out);
		//		byte[] result = new byte[size];
		//		System.arraycopy(out, 0, result, 0, size);
		//		return result;
		try {
			return Snappy.compress(input);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static Signal createSignal() {
		Signal result = new Signal();
		synchronized (id2Signal) {
			id2Signal.put(result.getId(), result);
		}
		return result;
	}

	public static int getCode(Method method) {
		Adler32 ader = new Adler32();
		ader.update(method.toGenericString().getBytes());
		return (int) ader.getValue();
	}

	public static Map<Integer, Method> getCode2methodMap(Class<?> cl) {
		Map<Integer, Method> result = new HashMap<Integer, Method>();

		if (cl.isInterface()) {
			for (Method method : cl.getMethods()) {
				result.put(getCode(method), method);
			}
		}

		do {
			for (Class<?> intf : cl.getInterfaces()) {
				for (Method method : intf.getMethods()) {
					result.put(getCode(method), method);
				}
			}
			cl = cl.getSuperclass();
		} while (cl != null);
		return result;
	}

	public static Signal getCurrentThreadSignal() {
		return signal.get();
	}

	public static Map<Method, Integer> getMethod2CodeMap(Class<?> cl) {
		Map<Method, Integer> result = new HashMap<Method, Integer>();

		if (cl.isInterface()) {
			for (Method method : cl.getMethods()) {
				result.put(method, getCode(method));
			}
		}

		do {
			for (Class<?> intf : cl.getInterfaces()) {
				for (Method method : intf.getMethods()) {
					result.put(method, getCode(method));
				}
			}
			cl = cl.getSuperclass();
		} while (cl != null);
		return result;
	}

	public static Signal getSignal(short id) {
		Signal result = id2Signal.get(id);
		if (result == null) {
			synchronized (id2Signal) {
				if (result == null) {
					result = new Signal(id);
					id2Signal.put(id, result);
				}
			}
		}
		return result;
	}

	public static Collection<Signal> getSignals() {
		return id2Signal.values();
	}

	public static byte[] uncompress(byte[] compress) {
		//		Inflater decompresser = new Inflater();
		//		decompresser.setInput(compress, 0, compress.length);
		//		byte[] out = zipBuffer.get();
		try {
			//			int resultLength = decompresser.inflate(out);
			//			byte[] result = new byte[resultLength];
			//			System.arraycopy(out, 0, result, 0, resultLength);
			//			decompresser.end();
			//			return result;
			return Snappy.uncompress(compress);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
