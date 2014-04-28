package com.taobao.rpc.msgpack;

import com.taobao.rpc.api.RpcException;
import com.taobao.rpc.api.RpcFactory;
import com.taobao.rpc.benchmark.dataobject.*;
import org.msgpack.MessagePack;
import org.msgpack.rpc.Client;
import org.msgpack.rpc.Server;
import org.msgpack.rpc.loop.EventLoop;

import java.net.UnknownHostException;

/**
 * An simple implementation of {@link RpcFactory}.
 * <p/>
 * Just wrap implementation to {@code MsgPack}.
 * <p/>
 * Date: 2013/1/4
 *
 * @author shutong.dy
 */
public class MsgPackRpcFactory implements RpcFactory {
    @Override
    public <T> void export(Class<T> type, T serviceObject) {
        MessagePack mp = new MessagePack();
        mp.register(PersonStatus.class);
        mp.register(FullAddress.class);
        mp.register(Phone.class);
        mp.register(PersonInfo.class);
        mp.register(Person.class);
        try {
            EventLoop loop = EventLoop.start(mp);

            Server svr = new Server(loop);
            svr.serve(serviceObject);
            svr.listen(1985);

            loop.join();
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    @Override
    public <T> T getReference(Class<T> type, String ip) {
        MessagePack mp = new MessagePack();
        mp.register(PersonStatus.class);
        mp.register(FullAddress.class);
        mp.register(Phone.class);
        mp.register(PersonInfo.class);
        mp.register(Person.class);

        Client cli = null;

        try {
            cli = new Client(ip, 1985, EventLoop.start(mp));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return type.cast(cli.proxy(type));
    }

    @Override
    public int getClientThreads() {
        return 20;
    }

    @Override
    public String getAuthorId() {
        return "shutong-msgpack";
    }
}
