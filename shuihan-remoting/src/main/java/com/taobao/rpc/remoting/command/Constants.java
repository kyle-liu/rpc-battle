package com.taobao.rpc.remoting.command;



public class Constants {
    public static final byte REQUEST_MAGIC = (byte) 0x80;
    public static final byte RESPONSE_MAGIC = (byte) 0x81;
    public static final short RESERVED = (short) 0x0000;

    /**
     * ����ͷ����
     */
    public static final int REQUEST_HEADER_LENGTH = 12;
    /**
     * ��Ӧͷ����
     */
    public static final int RESPONSE_HEADER_LENGTH = 16;
}
