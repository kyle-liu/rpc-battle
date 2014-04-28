package com.taobao.rpc.zaza.model;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

import com.taobao.rpc.benchmark.dataobject.FullAddress;
import com.taobao.rpc.benchmark.dataobject.Person;
import com.taobao.rpc.benchmark.dataobject.PersonInfo;
import com.taobao.rpc.benchmark.dataobject.PersonStatus;
import com.taobao.rpc.benchmark.dataobject.Phone;
import com.taobao.rpc.zaza.ZazaRequest;
import com.taobao.rpc.zaza.serialization.KryoDeSerializer;
import com.taobao.rpc.zaza.util.PackageUtil;
import com.taobao.rpc.zaza.util.ZazaKryoUtils;
import com.taobao.rpc.zaza.util.ZazaUtil;

public class ZazaMethodDataModel {
    private List<ZazaMethod> cacher = new ArrayList<ZazaMethod>();
    public static final ZazaMethodDataModel instance = new ZazaMethodDataModel();
    private Map<String, Byte> methodName2CodeMapAsServerSide = null;
    private Map<String, Byte> methodName2CodeMapAsClientSide = null;
    private Map<String, Byte> className2CodeMap = new HashMap<String, Byte>(256);
    private Map<Byte, String> code2ClassNameMap = new HashMap<Byte, String>(256);

    public static final Person person;
    static {
        person = new Person();
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
    }

    private ZazaMethodDataModel() {

    }

    public void initMethodCodesOfClientSide(Map<String, Byte> methodName2CodeMap) {
        this.methodName2CodeMapAsClientSide = methodName2CodeMap;
    }

    public void initClassCodes(String... packagelist) {
        // The max code is 127 and then return to -128 and the amount is 256
        byte code = 0;
        if (!className2CodeMap.containsKey(String.class.getName())) {
            className2CodeMap.put(String.class.getName(), code);
            code2ClassNameMap.put(code++, String.class.getName());
        }

        if (!className2CodeMap.containsKey(ArrayList.class.getName())) {
            className2CodeMap.put(ArrayList.class.getName(), code);
            code2ClassNameMap.put(code++, ArrayList.class.getName());
        }

        if (!className2CodeMap.containsKey(HashMap.class.getName())) {
            className2CodeMap.put(HashMap.class.getName(), code);
            code2ClassNameMap.put(code++, HashMap.class.getName());
        }
        for (String packageName : packagelist) {
            List<String> classes = PackageUtil.getClassName(packageName, false);
            Collections.sort(classes);
            for (String className : classes) {
                try {
                    Class<?> classType = Class.forName(className);
                    if (Serializable.class.isAssignableFrom(classType) && !className2CodeMap.containsKey(className)) {
                        className2CodeMap.put(className, code);
                        code2ClassNameMap.put(code++, className);
                        ZazaKryoUtils.registerClass(classType);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Byte getCodeOfMethodAsClientSide(String methodDesc) {
        return methodName2CodeMapAsClientSide.get(methodDesc);
    }

    public Byte getCodeOfClass(String className) {
        return className2CodeMap.get(className);
    }

    public Map<String, Byte> getMthodName2CodeMapAsServerSide() {
        return new HashMap<String, Byte>(methodName2CodeMapAsServerSide);
    }

    public void insert(Class<?> providerClass, Object instance) {
        if (providerClass == null) {
            return;
        }

        if (methodName2CodeMapAsServerSide == null) {
            methodName2CodeMapAsServerSide = new HashMap<String, Byte>(256);
        }

        for (Method method : providerClass.getMethods()) {
            String nameOfCode = ZazaUtil.generateNameOfMethod(providerClass, method);
            // The max code is 127 and then return to -128 and the amount is 256
            Byte code = methodName2CodeMapAsServerSide.get(nameOfCode);
            if (code == null) {
                code = (byte) cacher.size();
                methodName2CodeMapAsServerSide.put(nameOfCode, code);
                FastMethod fastMethod = FastClass.create(providerClass).getMethod(method);
                ZazaMethod zazaMethod = new ZazaMethod(fastMethod, code, instance);
                cacher.add(zazaMethod);
            }
        }
    }

    public Object invoke(ZazaRequest request) {
        ZazaMethod zazaMethod = cacher.get(request.getMethodCode());
        try {
            Object[] paraObjects = null;
            if (request.getRequestObjects() != null) {
                paraObjects = new Object[request.getRequestObjects().length];
                byte[][] paraTypes = request.getRequestTypes();
                int i = 0;
                for (byte[] bytes : request.getRequestObjects()) {
                    String paraType = null;
                    if (paraTypes[i].length == 1) {
                        paraType = code2ClassNameMap.get(paraTypes[i][0]);
                    } else {
                        paraType = new String(paraTypes[i]);
                    }
                    paraObjects[i] = KryoDeSerializer.decode(paraType, bytes);
                    i++;
                }
            }
            return zazaMethod.getMethod().invoke(zazaMethod.getInstance(), paraObjects);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("parameter is unmatched");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("deseraialiazion error");
        }
    }
}
