//package com.taobao.rpc.bishan.kryo;
//
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//
//import com.esotericsoftware.kryo.Kryo;
//import com.esotericsoftware.kryo.io.Input;
//import com.esotericsoftware.kryo.io.Output;
//import com.taobao.rpc.benchmark.dataobject.Person;
//import com.taobao.rpc.bishan.ClientMain;
//import com.taobao.rpc.bishan.net.msg.RequstPackage;
//import com.taobao.rpc.bishan.net.reactor.BsFactory;
//
//public class TestKryo {
//
//	public static void main(String[] args) throws FileNotFoundException{
//		Kryo kryo = new Kryo();
//		kryo.setReferences(true);
//
//		KryoSerial ks=(KryoSerial)BsFactory.kryoSerial.get();
//		//kryo.register(Person.class,44);
//		//kryo.register(PersonInfo.class,44);
//		// ...
//		Output output = new Output(new FileOutputStream("file.bin5"));
//		ks.kryo.writeObject(output ,ClientMain.genPerson());
//		output.close();
////		Person someObject = new Person();
////		
////		Phone ph=new Phone();
////		ph.setNumber("asas");
////		
////		
////		PersonInfo pi=new PersonInfo();
////		pi.setHomepageUrl("aaa");
////		pi.setMale(false);
////		pi.setMytets(111);
////		pi.setKkk(10.3);
////		pi.setLll(2000);
////		//someObject.setInfo(pi);
////		
////		
////		someObject.setLoginName("hello");
////		
////		//kryo.writeObject(output, someObject);
////		kryo.writeClass(output, pi.getClass());	//���ע��ͻ�дȫ�޶���
////		kryo.writeObject(output, pi);
////	//	kryo.writeObject(output, ph);
////		//kryo.writeObject(output, cpi);
////		
////		output.close();
//		// ��ȡ
//		Input input = new Input(new FileInputStream("file.bin1"));
//		
//		RequstPackage someObject2=ks.kryo.readObject(input, RequstPackage.class);
//	//	CopyOfPersonInfo rrr = kryo.readObject(input, CopyOfPersonInfo.class);
////		Phone rrrr = kryo.readObject(input, Phone.class);
////		System.out.println(rrr.getMytets()+""+rrr.getKkk()+""+rrr.getLll());
////		System.out.println(someObject2.getLoginName());
////		System.out.println(rrrr.getNumber());
//		input.close();
//		
//		//�ٴ�д��
//		Output output2 = new Output(new FileOutputStream("file.bin3"));
//		Person someObject3 = new Person();
//		someObject3.setLoginName("hello55");
//		//someObject3.setInfo(pi);
//		
//		kryo.writeObject(output2, someObject3);
//		output2.close();
//		
//		//
//		Output output3 = new Output(new FileOutputStream("file.bin4"));
//		//kryo.writeObject(output3, pi);
//		Person someObject4 = new Person();
//		someObject4.setLoginName("xxxx");
//		kryo.writeObject(output3, someObject4);
//		output3.close();
//	}
//	
//}
