import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.zip.Deflater;

import org.junit.Test;
import org.xerial.snappy.Snappy;

import shenfeng.simplerpc.TransportConstants;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.taobao.rpc.benchmark.dataobject.FullAddress;
import com.taobao.rpc.benchmark.dataobject.Person;
import com.taobao.rpc.benchmark.dataobject.PersonInfo;
import com.taobao.rpc.benchmark.dataobject.PersonStatus;
import com.taobao.rpc.benchmark.dataobject.Phone;

public class KryoTest {

	Kryo kryo = new Kryo();

	private final Person person = createPerson();

	@Test
	public void testPerson() throws IOException {
		final Long methodCode = 12345L;
		ByteArrayOutputStream target = new ByteArrayOutputStream();
		write(methodCode, person, target);
		byte[] serializerBytes = target.toByteArray();
		assertEquals(4289, serializerBytes.length);
		assertTarget(methodCode, person, Person.class, serializerBytes);

		byte[] mutile = new byte[serializerBytes.length * 100];
		for (int i = 0; i < 100; i++) {
			System.arraycopy(serializerBytes, 0, mutile, i * serializerBytes.length, serializerBytes.length);
		}
		assertEquals(75895, Snappy.compress(mutile).length);

		Deflater compresser = new Deflater();
		compresser.setInput(mutile);
		compresser.finish();
		int finalSize = compresser.deflate(new byte[serializerBytes.length * 3]);
		assertTrue(7370 < finalSize && finalSize < 7390);
	}

	@Test
	public void testString() throws IOException {
		final Long methodCode = 12345L;
		final String constant = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";

		ByteArrayOutputStream target = new ByteArrayOutputStream();
		write(methodCode, constant, target);
		byte[] serializerBytes = target.toByteArray();
		assertEquals(67, serializerBytes.length);
		assertTarget(methodCode, constant, String.class, serializerBytes);

		byte[] mutile = new byte[serializerBytes.length * 100];
		for (int i = 0; i < 100; i++) {
			System.arraycopy(serializerBytes, 0, mutile, i * serializerBytes.length, serializerBytes.length);
		}
		assertEquals(383, Snappy.compress(mutile).length);

		Deflater compresser = new Deflater();
		compresser.setInput(mutile);
		compresser.finish();
		assertEquals(127, compresser.deflate(new byte[serializerBytes.length * 3]));
	}

	private void assertTarget(final Long methodCode, final Object constant, final Class<?> constantClass,
			byte[] serializerBytes) {
		Input in = new Input(new ByteArrayInputStream(serializerBytes));
		assertTrue(TransportConstants.Call == kryo.readObject(in, Byte.class));
		assertEquals(methodCode, kryo.readObject(in, Long.class));
		assertEquals(constant, kryo.readObject(in, constantClass));
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

	private void write(final Long methodCode, final Object constant, ByteArrayOutputStream target) {
		Output out = new Output(target);
		kryo.writeObject(out, TransportConstants.Call);
		kryo.writeObject(out, methodCode);
		kryo.writeObject(out, constant);
		out.flush();
	}

}
