
/**
 * xiaodu-rpc
 */
package com.taobao.rpc_battle.rpc_udp;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.taobao.rpc.benchmark.dataobject.FullAddress;
import com.taobao.rpc.benchmark.dataobject.Person;
import com.taobao.rpc.benchmark.dataobject.PersonInfo;
import com.taobao.rpc.benchmark.dataobject.PersonStatus;
import com.taobao.rpc.benchmark.dataobject.Phone;

/**
 * @author xiaodu
 *
 * 上午9:33:56
 */
public class KryoUtil {

	
	public static Kryo createKryo(){
		Kryo kryo = new Kryo();
		 kryo.register(PersonInfo.class);
		 kryo.register(ArrayList.class);
		 kryo.register(List.class);
		 kryo.register(Phone.class);
		 kryo.register(FullAddress.class);
		 
		 kryo.register(byte[].class);
		 kryo.register(Object[].class);
		 kryo.register(Object.class);
		 kryo.register(Person.class);
		 kryo.register(PersonStatus.class);
		 
		 kryo.register(String.class);
		 kryo.register(Boolean.class);
		 kryo.register(boolean.class);
		 kryo.register(int.class);
		 kryo.register(Integer.class);
		 kryo.register(Long.class);
		 
		 kryo.register(UdpReqPacket.class);
		 kryo.register(UdpRespPacket.class);
		 
		 kryo.setRegistrationRequired(true);
		 
		 return kryo;
	}
	private static ThreadLocal<Kryo> map = new ThreadLocal<Kryo>();
	public static byte[] Object2Bytes(Kryo kryo,Object obj){
		
		kryo= map.get();
		if(kryo == null){
			kryo =  createKryo();
			map.set(kryo);
		}
		
		
			Output out = new Output(new ByteArrayOutputStream() );
			kryo.writeObject(out , obj);
			return out.getBuffer();
	}
	
	
	public  static <T> T bytes2Object(Kryo kryo,byte[] obj,int len,Class<T> t){
		
		kryo= map.get();
		if(kryo == null){
			kryo =  createKryo();
			map.set(kryo);
		}
		
		
		return kryo.readObject(new Input(obj, 0,len), t);
	}
	
	

}
