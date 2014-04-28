package com.taobao.rpc.fish.common.command.codec;

import java.io.IOException;

public interface Deserializer {

	public Object decodeObject(byte data[])throws IOException;
	
}
