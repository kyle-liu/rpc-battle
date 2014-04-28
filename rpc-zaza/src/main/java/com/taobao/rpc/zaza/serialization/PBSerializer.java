package com.taobao.rpc.zaza.serialization;

import com.google.protobuf.Message;

public class PBSerializer{

	public static byte[] encode(Object object) throws Exception {
		if (!(object instanceof Message)) {
			throw new Exception(
					"Send object is not type of com.google.protobuf.Message,pls sure the object is generated by pb,object is: "
							+ object);
		}
		Message message = (Message) object;
		return message.toByteArray();
	}

}