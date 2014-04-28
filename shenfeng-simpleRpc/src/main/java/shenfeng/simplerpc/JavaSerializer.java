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

	@Override
	public Object readObject(InputStream inputStream, Class<?> type) {
		if (type == null) {
			return null;
		}
		try {
			return unmarshalValue(type, new ObjectInputStream(inputStream));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Object[] readObjects(InputStream inputStream, Class<?>[] types) {
		if (types == null || types.length == 0) {
			return emptyObjectArray;
		}
		Object[] result = new Object[types.length];
		try {
			ObjectInputStream in = new ObjectInputStream(inputStream);
			for (int i = 0; i < result.length; i++) {
				result[i] = unmarshalValue(types[i], in);
			}
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void writeObject(OutputStream outputStream, Class<?> type, Object obj) {
		try {
			ObjectOutputStream out = new ObjectOutputStream(outputStream);
			marshalValue(type, obj, out);
			out.flush();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void writeObjects(OutputStream outputStream, Class<?>[] types, Object[] objs) {
		if (objs == null || objs.length == 0) {
			return;
		}
		try {
			ObjectOutputStream out = new ObjectOutputStream(outputStream);
			for (int i = 0; i < types.length; i++) {
				marshalValue(types[i], objs[i], out);
			}
			out.flush();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void writeObjects(OutputStream output, Object... objs) {

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
