package com.taobao.rpc.fish.common.command.codec.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.taobao.rpc.fish.common.command.codec.Deserializer;
import com.taobao.rpc.fish.common.command.codec.Serializer;

public class KryoDeserializer implements Deserializer{
	private Kryo kryo = new Kryo();
	/*{
		kryo.setReferences(true);
		kryo.setRegistrationRequired(false);
		
	}*/
	@Override
	public  Object decodeObject(byte[] data){
		if(data==null)return null;
		//Kryo kryo = new Kryo();
		Input in=new Input(data);
		return kryo.readClassAndObject(in);
		
	}  

}
