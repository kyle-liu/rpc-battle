package com.taobao.rpc.service;

import com.google.common.base.Function;
import com.taobao.rpc.api.RpcException;
import com.taobao.rpc.codec.Codec;
import com.taobao.rpc.codec.CodecFactory;
import com.taobao.rpc.handle.BufferHandle;
import com.taobao.rpc.handle.Handler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** @author <a href="mailto:jushi@taobao.com">jushi<a> */
public class Services extends Handler implements ServiceDirectory {
    private final List<Service> services;

    public Services(List<Service> services, CodecFactory factory) {
        super(factory);
        this.services = preAppendServiceDirectoryServicesWith(services);
    }

    @Override
    public List<ServiceIndex> listBy(String exporter) {
        ArrayList<ServiceIndex> result = new ArrayList<ServiceIndex>();
        for (int i = 0; i < services.size(); i++) {
            Service service = services.get(i);
            if (service.belongs().equals(exporter))
                result.add(new ServiceIndex(i, service.name()));
        }
        return result;
    }

    public Service get(int index) {
        return services.get(index);
    }

    @Override
    protected void receive(ByteBuf frame, BufferHandle bufferHandle, final ChannelHandlerContext ctx) {
        bufferHandle.readAndwrite(frame, new Function<Codec, ChannelFuture>() {

            @Override
            public ChannelFuture apply(Codec codec) {
                Request request = codec.decode(Request.class);
                Object result = handle(request);
                Response response = new Response(request.id(), result);
                return ctx.write(codec.encode(response));
            }
        });
    }

    private List<Service> preAppendServiceDirectoryServicesWith(List<Service> services) {
        ArrayList<Service> list = new ArrayList<Service>(services.size() + 1);
        list.addAll(Service.services(ServiceDirectory.class, this));
        list.addAll(services);
        return Collections.unmodifiableList(list);
    }

    private Object handle(Request request) {
        try {
            return services.get(request.serviceIndex()).invoke(request.args());
        } catch (Exception e) {
            return new RpcException(e);
        }
    }

}
