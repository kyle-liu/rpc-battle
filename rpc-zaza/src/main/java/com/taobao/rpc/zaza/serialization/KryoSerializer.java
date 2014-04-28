package com.taobao.rpc.zaza.serialization;

import com.esotericsoftware.kryo.io.Output;
import com.taobao.rpc.zaza.util.ZazaKryoUtils;

public class KryoSerializer {
	/**
	 * @param object
	 * @return
	 * @throws Exception
	 */
	public static byte[] encode(Object object) throws Exception {
		Output output = new Output(4126, -1);
		ZazaKryoUtils.getKryo().writeObject(output, object);
		return output.toBytes();
	}

}
