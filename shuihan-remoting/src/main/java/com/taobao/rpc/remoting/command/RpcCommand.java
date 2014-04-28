package com.taobao.rpc.remoting.command;

/**
 * Notify命令接口，定义content的编解码方法
 * 
 * @author boyan
 * @Date 2010-8-9
 * 
 */
public interface RpcCommand {
    void encodeContent();


    void decodeContent();
}
