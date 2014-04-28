package shenfeng.simplerpc;

import java.io.InputStream;
import java.io.OutputStream;

public interface Serializer {

	Serializer create(InputStream in);

	Serializer create(OutputStream output);

	void flush();

	<T> T readObject(Class<T> type);

	Object[] readObjects(Class<?>[] types);

	void writeObject(Class<?> type, Object obj);

	void writeObjects(Class<?>[] types, Object[] objs);

}
