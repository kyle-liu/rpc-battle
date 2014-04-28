package com.taobao.rpc.zaza.util;

import java.util.ArrayList;
import java.util.List;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;

public class ZazaKryoUtils {
    private static final List<Class<?>> classList = new ArrayList<Class<?>>();
    private static final List<Serializer<?>> serializerList = new ArrayList<Serializer<?>>();
    private static final ThreadLocal<Kryo> kryos = new ThreadLocal<Kryo>() {
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            int size = classList.size();
            for (int i = 0; i < size; i++) {
                Serializer<?> serializer = serializerList.get(i);
                if (serializer == null) {
                    kryo.register(classList.get(i));
                } else {
                    kryo.register(classList.get(i), serializer);
                }
            }
            kryo.setRegistrationRequired(false);
            kryo.setReferences(false);
            return kryo;
        }
    };

    /**
	 * 
	 */
    private ZazaKryoUtils() {
    }

    /**
     * @param className
     * @param serializer
     * @param id
     */
    public static synchronized void registerClass(Class<?> className, Serializer<?> serializer) {
        classList.add(className);
        serializerList.add(serializer);
    }

    public static synchronized void registerClass(Class<?> className) {
        registerClass(className, null);
    }

    /**
     * @return
     */
    public static Kryo getKryo() {
        return kryos.get();
    }
}
