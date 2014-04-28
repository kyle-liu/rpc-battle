package shenfeng.simplerpc;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class JavaSerializer implements Serializer {

	private static final Object[] emptyObjectArray = new Object[0];

	private ObjectInput in;

	private ObjectOutput out;

	public JavaSerializer() {
	}

	public JavaSerializer(InputStream in) {
		try {
			this.in = new ObjectInputStream(in);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public JavaSerializer(OutputStream out) {
		try {
			this.out = new ObjectOutputStream(out);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Serializer create(InputStream in) {
		return new JavaSerializer(in);
	}

	@Override
	public Serializer create(OutputStream out) {
		return new JavaSerializer(out);
	}

	@Override
	public void flush() {
		// java序列化不需要flush
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T readObject(Class<T> type) {
		if (type == null) {
			return null;
		}
		try {
			return (T) unmarshalValue(type, in);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Object[] readObjects(Class<?>[] types) {
		if (types == null || types.length == 0) {
			return emptyObjectArray;
		}
		Object[] result = new Object[types.length];
		try {
			for (int i = 0; i < result.length; i++) {
				result[i] = unmarshalValue(types[i], in);
			}
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void writeObject(Class<?> type, Object obj) {
		try {
			marshalValue(type, obj, out);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void writeObjects(Class<?>[] types, Object[] objs) {
		if (objs == null || objs.length == 0) {
			return;
		}
		try {
			for (int i = 0; i < types.length; i++) {
				marshalValue(types[i], objs[i], out);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void marshalValue(Class<?> type, Object value, ObjectOutput out) throws IOException {
		if (type.isPrimitive()) {
			if (type == int.class) {
				out.writeInt(((Integer) value).intValue());
			} else if (type == boolean.class) {
				out.writeBoolean(((Boolean) value).booleanValue());
			} else if (type == byte.class) {
				out.writeByte(((Byte) value).byteValue());
			} else if (type == char.class) {
				out.writeChar(((Character) value).charValue());
			} else if (type == short.class) {
				out.writeShort(((Short) value).shortValue());
			} else if (type == long.class) {
				out.writeLong(((Long) value).longValue());
			} else if (type == float.class) {
				out.writeFloat(((Float) value).floatValue());
			} else if (type == double.class) {
				out.writeDouble(((Double) value).doubleValue());
			} else {
				throw new Error("Unrecognized primitive type: " + type);
			}
		} else {
			out.writeObject(value);
		}
	}

	private Object unmarshalValue(Class<?> type, ObjectInput in) throws IOException, ClassNotFoundException {
		if (type.isPrimitive()) {
			if (type == int.class) {
				return Integer.valueOf(in.readInt());
			} else if (type == boolean.class) {
				return Boolean.valueOf(in.readBoolean());
			} else if (type == byte.class) {
				return Byte.valueOf(in.readByte());
			} else if (type == char.class) {
				return Character.valueOf(in.readChar());
			} else if (type == short.class) {
				return Short.valueOf(in.readShort());
			} else if (type == long.class) {
				return Long.valueOf(in.readLong());
			} else if (type == float.class) {
				return Float.valueOf(in.readFloat());
			} else if (type == double.class) {
				return Double.valueOf(in.readDouble());
			} else {
				throw new Error("Unrecognized primitive type: " + type);
			}
		} else {
			return in.readObject();
		}
	}

}
