import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Random;

import org.junit.Test;

import shenfeng.simplerpc.TransportConstants;

import com.taobao.rpc.benchmark.dataobject.FullAddress;
import com.taobao.rpc.benchmark.dataobject.Person;
import com.taobao.rpc.benchmark.dataobject.PersonInfo;
import com.taobao.rpc.benchmark.dataobject.PersonStatus;
import com.taobao.rpc.benchmark.dataobject.Phone;

public class JavaSerializerTest {

	private final Person person = createPerson();

	@Test
	public void testPerson() {
		final Long methodCode = 12345L;
		ByteArrayOutputStream target = new ByteArrayOutputStream();
		write(methodCode, person, target);
		byte[] serializerBytes = target.toByteArray();
		assertEquals(5241, serializerBytes.length);
		assertTarget(methodCode, person, Person.class, serializerBytes);
	}

	@Test
	public void testString() {
		final Long methodCode = 12345L;
		final String constant = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";

		ByteArrayOutputStream target = new ByteArrayOutputStream();
		write(methodCode, constant, target);
		byte[] serializerBytes = target.toByteArray();
		assertEquals(80, serializerBytes.length);
		assertTarget(methodCode, constant, String.class, serializerBytes);
	}

	private void assertTarget(final long methodCode, final Object constant, final Class<?> constantClass,
			byte[] serializerBytes) {
		ObjectInputStream in;
		try {
			in = new ObjectInputStream(new ByteArrayInputStream(serializerBytes));
			assertTrue(TransportConstants.Call == in.readByte());
			assertEquals(methodCode, in.readLong());
			assertEquals(constant, in.readObject());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Person createPerson() {
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

	private void write(final long methodCode, final Object constant, ByteArrayOutputStream target) {
		ObjectOutputStream out;
		try {
			out = new ObjectOutputStream(target);
			out.writeByte(TransportConstants.Call);
			out.writeLong(methodCode);
			out.writeObject(constant);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
