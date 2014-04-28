package shenfeng.simplerpc;

import java.io.InputStream;
import java.io.OutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class KryoSerializer implements Serializer {

	private final ThreadLocal<Input> in = new ThreadLocal<Input>();

	private final ThreadLocal<Output> out = new ThreadLocal<Output>();

	private final ThreadLocal<Kryo> kryo = new ThreadLocal<Kryo>() {

		@Override
		protected Kryo initialValue() {
			return new Kryo();
		}

	};

	private static final Object[] emptyObjectArray = new Object[0];

	public KryoSerializer() {
	}

	@Override
	public Serializer create(InputStream input) {
		in.set(new Input(input));
		return this;
	}

	@Override
	public Serializer create(OutputStream output) {
		out.set(new Output(output));
		return this;
	}

	@Override
	public void flush() {
		if (out != null) {
			out.get().flush();
		}
	}

	@Override
	public <T> T readObject(Class<T> type) {
		if (type == null) {
			return null;
		}
		return kryo.get().readObject(in.get(), type);
	}

	@Override
	public Object[] readObjects(Class<?>[] types) {
		if (types == null || types.length == 0) {
			return emptyObjectArray;
		}
		Object[] result = new Object[types.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = kryo.get().readObject(in.get(), types[i]);
		}
		return result;
	}

	@Override
	public void writeObject(Class<?> rtype, Object obj) {
		kryo.get().writeObject(out.get(), obj);
	}

	@Override
	public void writeObjects(Class<?>[] ytpes, Object[] objs) {
		if (objs == null || objs.length == 0) {
			return;
		}
		for (Object obj : objs) {
			kryo.get().writeObject(out.get(), obj);
		}
	}

}
