package example.hello;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import shenfeng.simplerpc.Registry;

import com.taobao.rpc.benchmark.dataobject.FullAddress;
import com.taobao.rpc.benchmark.dataobject.Person;
import com.taobao.rpc.benchmark.dataobject.PersonInfo;
import com.taobao.rpc.benchmark.dataobject.PersonStatus;
import com.taobao.rpc.benchmark.dataobject.Phone;
import com.taobao.rpc.benchmark.service.HelloService;

public class Client {

	static final String constant = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";

	public static void main(String[] args) {
		Registry registry = new Registry();
		final HelloService stub = registry.lookup(HelloService.class, "127.0.0.1", 1313);
		ExecutorService pool = Executors.newCachedThreadPool();
		for (int i = 0; i < 2; i++) {
			pool.execute(new Runnable() {
				@Override
				public void run() {
					for (int i = 0; i < 1000; i++) {
						stub.helloWorld(constant);
						stub.helloPerson(getPerson());
					}
				}
			});
		}
		pool.shutdown();
		for (int i = 0; i < 1000; i++) {
			stub.helloWorld(constant);
			stub.helloPerson(getPerson());
		}
	}

	static Person getPerson() {
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
		return person;
	}

}
