package com.taobao.rpc.remoting.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.taobao.gecko.core.command.RequestCommand;
import com.taobao.gecko.service.RemotingClient;
import com.taobao.gecko.service.RemotingFactory;
import com.taobao.gecko.service.config.ClientConfig;
import com.taobao.gecko.service.exception.NotifyRemotingException;
import com.taobao.rpc.remoting.command.BlockingGetResponseListener;
import com.taobao.rpc.remoting.command.RpcResponseCommand;
import com.taobao.rpc.remoting.factory.RpcWireFormatType;


public class RpcClient {

    private RemotingClient client;
    private ClientConfig clientConfig;
    private long connectionTimeout = 3000;
    private final Random random = new Random();

    private List<String> urls = new ArrayList<String>();


    public RpcClient() {

    }


    public void start() {
        try {
            this.initClient();
        }
        catch (NotifyRemotingException e) {
            e.printStackTrace();
        }
    }


    public void stop() {
        try {
            this.client.stop();
        }
        catch (NotifyRemotingException e) {
            e.printStackTrace();
        }
    }


    private void initClient() throws NotifyRemotingException {
        clientConfig = new ClientConfig();
        clientConfig.setConnectTimeout(this.connectionTimeout);
        clientConfig.setWireFormatType(new RpcWireFormatType());
        client = RemotingFactory.connect(clientConfig);
    }


    public RpcResponseCommand send(RequestCommand command) {
        BlockingGetResponseListener rspCallback = new BlockingGetResponseListener();
        try {
            this.client.sendToGroup(this.getUrl(), command, rspCallback,3000,TimeUnit.MILLISECONDS);
            return (RpcResponseCommand) rspCallback.waitForResponse();
        }
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (TimeoutException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (NotifyRemotingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    
    
    private String getUrl() {
        int pos = random.nextInt(this.urls.size());
        return urls.get(pos);
    }


    public void connect(String ip, int port, int connnectCount) {
        String url = "tcp://" + ip + ":" + port;
        urls.add(url);
        try {
            client.connect(url, connnectCount);
            client.awaitReadyInterrupt(url);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
