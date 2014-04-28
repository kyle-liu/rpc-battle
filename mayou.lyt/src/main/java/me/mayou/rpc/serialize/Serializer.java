package me.mayou.rpc.serialize;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class Serializer {

	private static ThreadLocal<Kryo> kryoThreadLocal;
	
	static{
		kryoThreadLocal = new ThreadLocal<Kryo>(){
			
			@Override
			public Kryo initialValue(){
				return new Kryo();
			}
			
		};
	}
	
	public static byte[] serialize(Object object) throws SerializeException {
		if (object == null) {
			throw new SerializeException("ָ��object����ָ��Ϊ��");
		}

		Kryo kryo = kryoThreadLocal.get();
		ByteArrayOutputStream stream = new ByteArrayOutputStream(2000);
		Output output = new Output(stream);
		kryo.writeObject(output, object);
		output.close();

		return stream.toByteArray();
	}
	
	public static <T> T deserializer(byte[] data, Class<T> clazz) throws SerializeException {
		if (data == null || data.length == 0) {
			throw new SerializeException("����Ϊ��");
		}

		InputStream stream = new BufferedInputStream(new ByteArrayInputStream(
				data));
		Input input = new Input(stream);
		return kryoThreadLocal.get().readObject(input, clazz);
	}

}
