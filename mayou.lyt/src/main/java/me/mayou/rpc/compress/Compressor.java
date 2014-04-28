package me.mayou.rpc.compress;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import me.mayou.rpc.serialize.Serializer;

import org.iq80.snappy.Snappy;

import com.taobao.rpc.benchmark.dataobject.FullAddress;
import com.taobao.rpc.benchmark.dataobject.Person;
import com.taobao.rpc.benchmark.dataobject.PersonInfo;
import com.taobao.rpc.benchmark.dataobject.PersonStatus;
import com.taobao.rpc.benchmark.dataobject.Phone;

public class Compressor {

	private static ThreadLocal<byte[]> dataThreadLocal;
	
	static {
		dataThreadLocal = new ThreadLocal<byte[]>() {
			protected byte[] initialValue() {
				return new byte[Snappy.maxCompressedLength(5 * 1024)];
			}
		};
	}

	public static byte[] compress(byte[] data) {
		if (data == null || data.length == 0) {
			return null;
		}
		byte[] compressedData = dataThreadLocal.get();
		if (compressedData.length < Snappy.maxCompressedLength(data.length)) {
			compressedData = new byte[Snappy.maxCompressedLength(data.length)];
		} else {
			Arrays.fill(compressedData, (byte)0);
		}
		int compressSize = Snappy.compress(data, 0, data.length,
				compressedData, 0);
		return Arrays.copyOf(compressedData, compressSize);
	}

	public static byte[] decompress(byte[] compressedData) {
		if (compressedData == null || compressedData.length == 0) {
			return null;
		}
		return Snappy.uncompress(compressedData, 0, compressedData.length);
	}
	
	public static void main(String[] args){
        Person person = new Person();
        person.setPersonId("id1");
        person.setLoginName("name1");
        person.setStatus(PersonStatus.ENABLED);

        byte[] attachment = new byte[4000]; // 4K
        Random random = new Random();
        random.nextBytes(attachment);
        person.setAttachment(attachment);

        ArrayList<Phone> phones = new ArrayList<Phone>();
        Phone phone1 = new Phone("86", "0571", "11223344", "001");
        Phone phone2 = new Phone("86", "0571", "11223344", "002");
        phones.add(phone1);
        phones.add(phone2);

        PersonInfo info = new PersonInfo();
        info.setPhones(phones);
        Phone fax = new Phone("86", "0571", "11223344", null);
        info.setFax(fax);
        FullAddress addr = new FullAddress("CN", "zj", "1234", "Road1", "333444");
        info.setFullAddress(addr);
        info.setMobileNo("1122334455");
        info.setMale(true);
        info.setDepartment("b2b");
        info.setHomepageUrl("www.abc.com");
        info.setJobTitle("dev");
        info.setName("name2");

        person.setInfo(info);
        
        byte[] data = Serializer.serialize(person);
        System.out.println(data.length);
        System.out.println(Compressor.compress(data).length);
	}

}
