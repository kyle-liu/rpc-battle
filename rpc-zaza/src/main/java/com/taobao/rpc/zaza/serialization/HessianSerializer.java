package com.taobao.rpc.zaza.serialization;
import java.io.ByteArrayOutputStream;

import com.caucho.hessian.io.Hessian2Output;
public class HessianSerializer {

	public static byte[] encode(Object object) throws Exception {
		ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
		Hessian2Output output = new Hessian2Output(byteArray);
		output.writeObject(object);
		output.close();
		byte[] bytes = byteArray.toByteArray();
		return bytes;
	}

}
