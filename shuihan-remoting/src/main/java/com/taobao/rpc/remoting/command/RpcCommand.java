package com.taobao.rpc.remoting.command;

/**
 * Notify����ӿڣ�����content�ı���뷽��
 * 
 * @author boyan
 * @Date 2010-8-9
 * 
 */
public interface RpcCommand {
    void encodeContent();


    void decodeContent();
}
