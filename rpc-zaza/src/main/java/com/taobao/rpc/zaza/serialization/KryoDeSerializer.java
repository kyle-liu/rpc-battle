package com.taobao.rpc.zaza.serialization;

import com.esotericsoftware.kryo.io.Input;
import com.taobao.rpc.zaza.util.ZazaKryoUtils;

public class KryoDeSerializer {
    public static Object decode(String className, byte[] bytes) throws Exception {
        return ZazaKryoUtils.getKryo().readObject(new Input(bytes), Class.forName(className));
    }
}
