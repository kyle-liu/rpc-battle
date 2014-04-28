package com.taobao.rpc.remoting.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;

public class SerializeUtil {

	public static Object decodeObject(byte[] in) throws IOException {
		Object obj = null;
		ByteArrayInputStream bais = null;
		Hessian2Input input = null;
		try {
			bais = new ByteArrayInputStream(in);
			input = new Hessian2Input(bais);
			input.startMessage();
			obj = input.readObject();
			input.completeMessage();
		} catch (IOException ex) {
			throw ex;
		} finally {
			if (input != null) {
				try {
					input.close();
					bais.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
		return obj;
	}

	public static byte[] encodeObject(Object obj) throws IOException {
		ByteArrayOutputStream baos = null;
		Hessian2Output output = null;
		try {
			baos = new ByteArrayOutputStream(1024);
			output = new Hessian2Output(baos);
			output.startMessage();
			output.writeObject(obj);
			output.completeMessage();
		} catch (IOException ex) {
			throw ex;
		} finally {
			if (output != null)
				try {
					output.close();
					baos.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
		}
		return baos != null ? baos.toByteArray() : null;
	}

}
