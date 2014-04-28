package com.taobao.rpc.fish.common.command.codec;

import java.io.IOException;

public interface Serializer {

	public byte[] encodeObject(Object obj)throws IOException;
}
