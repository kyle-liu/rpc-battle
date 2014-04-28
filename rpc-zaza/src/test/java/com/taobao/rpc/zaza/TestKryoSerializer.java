package com.taobao.rpc.zaza;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;

import org.junit.Test;

import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.DefaultArraySerializers;
import com.taobao.rpc.benchmark.dataobject.FullAddress;
import com.taobao.rpc.benchmark.dataobject.Person;
import com.taobao.rpc.benchmark.dataobject.PersonInfo;
import com.taobao.rpc.benchmark.dataobject.PersonStatus;
import com.taobao.rpc.benchmark.dataobject.Phone;
import com.taobao.rpc.zaza.serialization.HessianSerializer;
import com.taobao.rpc.zaza.util.ZazaCompressionUtil;
import com.taobao.rpc.zaza.util.ZazaKryoUtils;

public class TestKryoSerializer {

    static final Person person;
    static {
        person = new Person();
        person.setPersonId("id1");
        person.setLoginName("name1");
        person.setStatus(PersonStatus.ENABLED);

        byte[] attachment = new byte[4]; // 4K
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
    }

    @Test
    public void testSerializePerson() throws Exception {
        ZazaKryoUtils.registerClass(byte[].class, new DefaultArraySerializers.ByteArraySerializer());
        ZazaKryoUtils.registerClass(Person.class);
        ZazaKryoUtils.registerClass(FullAddress.class);
        ZazaKryoUtils.registerClass(PersonInfo.class);
        ZazaKryoUtils.registerClass(Phone.class);
        ZazaKryoUtils.registerClass(PersonStatus.class);
        ZazaKryoUtils.registerClass(ArrayList.class);

        Output output = new Output(1024, -1);
        ZazaKryoUtils.getKryo().writeObject(output, person);
        byte[] bytes = output.toBytes();
        System.out.println(bytes.length);
        System.out.println(ZazaCompressionUtil.compress(bytes).length);
        output.close();
    }

    @Test
    public void testSerializePersonHessian() throws Exception {
        byte[] bytes = HessianSerializer.encode(person);
        System.out.println(bytes.length);
        System.out.println(ZazaCompressionUtil.compress(bytes).length);
    }

    @Test
    public void testSerializeString() throws FileNotFoundException {
        long starttime = System.currentTimeMillis();
        int length = 0;
        int length2 = 0;
        for (int i = 0; i < 20000; i++) {
            Output output = new Output(1024, -1);
            ZazaKryoUtils.getKryo().writeObject(output,
                    "com.taobao.rpc.benchmark.dataobjectcom.taobao.rpc.benchmark.dataobject");
            length = output.toBytes().length;
            length2 = ZazaCompressionUtil.compress(output.toBytes()).length;
            output.close();
        }
        System.out.println(System.currentTimeMillis() - starttime);
        System.out.println(length);
        System.out.println(length2);

        starttime = System.currentTimeMillis();
        for (int i = 0; i < 20000; i++) {
            Output output = new Output(1024, -1);
            length = "heehehehafdfadfadfadfadsfadsdbfdngnmgjgadfad11tryefadsfadsf".getBytes().length;
            output.close();
        }
        System.out.println(System.currentTimeMillis() - starttime);
        System.out.println(length);

    }

}
