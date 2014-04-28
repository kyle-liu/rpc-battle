package com.taobao.rpc.fish.common.command.codec.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.taobao.rpc.fish.common.command.codec.Serializer;

public class KryoSerializer implements Serializer{

	private Kryo kryo = new Kryo();  
	/*{
		kryo.setReferences(true);
		kryo.setRegistrationRequired(false);
	}*/
	@Override
	public byte[] encodeObject(Object obj) throws IOException {
	    
	    /*kryo.setReferences(false);  
	    kryo.setRegistrationRequired(false);  */
	    //kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());  
		try{
			//kryo.register(obj.getClass());
			//Kryo kryo = new Kryo();
		ByteArrayOutputStream array=new ByteArrayOutputStream(1024);
	    Output output=new Output(array);  
	    kryo.writeClassAndObject(output,obj);  
	    output.flush();	  
		return array.toByteArray();
		}finally{
			  
		}
	}
}
