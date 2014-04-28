package com.taobao.rpc.zaza.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ZazaConfigUtil {
    private static int port = 9999;
    private static int connectionTimeOut = 1000;
    private static int timeOut = 2000;
    private static int clentNum = 10;
    private static int clentThreadNum = 100;
    private static int maxServerLogicThread = 100;
    private static int clientParkNano = 100;
    private static int serverParkNano = 100;

    static {
        Properties prop = new Properties();
        try {
            String filePath = System.getProperty("user.dir") + "/conf/zaza.properties";
            System.out.println(filePath);
            prop.load(new FileInputStream(filePath));
            port = Integer.parseInt((String) prop.getProperty("port"));
            connectionTimeOut = Integer.parseInt((String) prop.getProperty("connectionTimeOut"));
            clentNum = Integer.parseInt(prop.getProperty("clentNum"));
            clentThreadNum = Integer.parseInt(prop.getProperty("clentThreadNum"));
            if (prop.getProperty("timeOut") != null) {
                timeOut = Integer.parseInt(prop.getProperty("timeOut"));
            }
            if (prop.getProperty("maxServerLogicThread") != null) {
                maxServerLogicThread = Integer.parseInt(prop.getProperty("maxServerLogicThread"));
            }
            if (prop.getProperty("clientParkNano") != null) {
                clientParkNano = Integer.parseInt(prop.getProperty("clientParkNano"));
            }
            if (prop.getProperty("serverParkNano") != null) {
                serverParkNano = Integer.parseInt(prop.getProperty("serverParkNano"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static int getPort() {
        return port;
    }

    public static int getConnectionTimeout() {
        return connectionTimeOut;
    }

    public static int getClentNum() {
        return clentNum;
    }

    public static int getClentThreadNum() {
        return clentThreadNum;
    }

    public static int getTimeOut() {
        return timeOut;
    }

    public static int getMaxServerLogicThread() {
        return maxServerLogicThread;
    }

    public static int getClientParkNano() {
        return clientParkNano;
    }

    public static int getServerParkNano() {
        return serverParkNano;
    }

}
