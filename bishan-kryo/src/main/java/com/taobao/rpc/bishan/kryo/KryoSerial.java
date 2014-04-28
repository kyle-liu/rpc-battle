package com.taobao.rpc.bishan.kryo;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.taobao.rpc.benchmark.dataobject.FullAddress;
import com.taobao.rpc.benchmark.dataobject.Person;
import com.taobao.rpc.benchmark.dataobject.PersonInfo;
import com.taobao.rpc.benchmark.dataobject.PersonStatus;
import com.taobao.rpc.benchmark.dataobject.Phone;
import com.taobao.rpc.bishan.net.codec.SerialInterface;
import com.taobao.rpc.bishan.net.msg.RequstPackage;
import com.taobao.rpc.bishan.net.msg.ResponsePackage;

/**
 * Kryo实现的序列化 not thread -safe
 * 
 * @author bishan.ct
 * 
 */
public class KryoSerial implements SerialInterface {

	private Kryo kryo = null;

	public KryoSerial() {
		kryo = new Kryo();
		init();
	}

	private final int BUFFER_SIZE = 6 * 1024;
	private final byte[] buffer = new byte[BUFFER_SIZE];
	private final Output output = new Output(buffer, -1);

	public byte[] encode(Object obj) {
		output.setBuffer(buffer, -1);
		kryo.writeObject(output, obj);

		return output.toBytes();
	}

	@Override
	public Object decode(byte[] bts, Class className) {
		Object obj = kryo.readObject(new Input(bts), className);
		return obj;
	}

	@Override
	public void init() {
		kryo.register(PersonStatus.class);
		kryo.register(FullAddress.class);
		kryo.register(Phone.class);
		kryo.register(PersonInfo.class);
		kryo.register(Person.class);
		// kryo.register(ArrayList.class);
		kryo.register(RequstPackage.class);
		kryo.register(ResponsePackage.class);
		kryo.register(ArrayList.class);
		kryo.setRegistrationRequired(false);
	}

	public static void main(String[] args) throws FileNotFoundException {
		ArrayList<Phone> phones = new ArrayList<Phone>();
		Phone phone1 = new Phone("86", "0571", "11223344", "001");
		Phone phone2 = new Phone("86", "0571", "11223344", "002");
		phones.add(phone1);
		phones.add(phone2);

		for (int i = 0; i < 10; i++) {
			KryoSerial kryo = new KryoSerial();
			byte[] bts = kryo.encode(phones);
			Output output = new Output(new FileOutputStream("aaaa"));
			output.write(bts);
			output.flush();

			System.out.println(bts.length);
			ArrayList<Phone> kk = (ArrayList<Phone>) kryo.decode(bts,ArrayList.class);
			System.out.println(kk.get(0).getCountry());
		}

	}
}
