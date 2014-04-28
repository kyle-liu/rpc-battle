package com.taobao.rpc.fish.common.command.codec.impl;

import com.taobao.rpc.fish.common.command.codec.Deserializer;
import com.taobao.rpc.fish.common.command.codec.Serializer;

public class CodecFacory {

	static Deserializer deserializer=new KryoDeserializer();
	static Serializer serializer=new KryoSerializer();
	static ThreadLocal<Serializer> serLocal=new ThreadLocal<Serializer>(){
		protected Serializer initialValue() {
			return new KryoSerializer();
		}
	};
	static ThreadLocal<Deserializer> desLocal=new ThreadLocal<Deserializer>(){
		protected Deserializer initialValue() {
			return new KryoDeserializer();
		}
	};
	static Deserializer hessandser=new Hessian1Deserializer();
	static Serializer hessanser=new Hessian1Serializer();
	public static Deserializer getDeserializer(){
		return desLocal.get();
		//return hessandser;
		//return deserializer;
	}
	public static Serializer getSerializer(){
		return serLocal.get();
		//return hessanser;
		//return serializer;
	}
}
