package com.taobao.rpc.zaza.serialization;

import java.util.concurrent.ConcurrentHashMap;

import com.google.protobuf.Message;

public class PBDeSerializer {

    private static ConcurrentHashMap<String, Message> messages = new ConcurrentHashMap<String, Message>();

    public static void addMessage(String className, Message message) {
        messages.putIfAbsent(className, message);
    }

    public static Object decode(String className, byte[] bytes) throws Exception {
        Message message = messages.get(className);
        return message.newBuilderForType().mergeFrom(bytes).build();
    }

}
