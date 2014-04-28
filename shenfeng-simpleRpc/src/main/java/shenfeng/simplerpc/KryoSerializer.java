package shenfeng.simplerpc;

import java.io.InputStream;
import java.io.OutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class KryoSerializer implements Serializer {

	private final ThreadLocal<Kryo> kryo = new ThreadLocal<Kryo>() {

		@Override
		protected Kryo initialValue() {
			return new Kryo();
		}

	};

	private static final Object[] emptyObjectArray = new Object[0];

	@Override
	public Object readObject(InputStream input, Class<?> type) {
		if (type == null) {
			return null;
		}
		Input in = new Input(input);
		return kryo.get().readObject(in, type);
	}

	@Override
	public Object[] readObjects(InputStream input, Class<?>[] types) {
		if (types == null || types.length == 0) {
			return emptyObjectArray;
		}
		Input in = new Input(input);
		Object[] result = new Object[types.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = kryo.get().readObject(in, types[i]);
		}
		return result;
	}

	@Override
	public void writeObject(OutputStream outputStream, Class<?> rtype, Object obj) {
		Output out = new Output(outputStream);
		kryo.get().writeObject(out, obj);
		out.flush();
	}

	@Override
	public void writeObjects(OutputStream outputStream, Class<?>[] ytpes, Object[] objs) {
		if (objs == null || objs.length == 0) {
			return;
		}
		Output out = new Output(outputStream);
		for (Object obj : objs) {
			kryo.get().writeObject(out, obj);
		}
		out.flush();
	}

}
