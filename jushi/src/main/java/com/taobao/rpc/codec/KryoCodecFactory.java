package com.taobao.rpc.codec;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.base.Function;
import com.taobao.rpc.service.Request;
import com.taobao.rpc.service.Response;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import org.objenesis.strategy.StdInstantiatorStrategy;

import static com.taobao.rpc.codec.LengthFieldBaseFrameCodecUtil.preappendLength;

/** @author <a href="mailto:jushi@taobao.com">jushi<a> */
public class KryoCodecFactory implements CodecFactory {
    @Override
    public Codec create() {
        final Kryo kryo = kryo();
        final ByteBuf buf = Unpooled.buffer(8192, 65536);
        return new Codec() {
            @Override
            public <T> ByteBuf encode(final T object) {
                return preappendLength(buf.clear(), new Function<ByteBuf, Void>() {
                    @Override
                    public Void apply(ByteBuf out) {
                        Output output = new Output(new ByteBufOutputStream(out));
                        kryo.writeObject(output, object);
                        output.flush();
                        return null;
                    }
                });
            }

            @Override
            public <T> T decode(Class<T> type) {
                Input input = new Input(new ByteBufInputStream(buf));
                return kryo.readObject(input, type);
            }

            @Override
            public void prepare(ByteBuf in) {
                if (buf != null) buf.clear().writeBytes(in);
            }

        };
    }

    private static Kryo kryo() {
        final Kryo kryo = new Kryo();
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
        kryo.register(Request.class);
        kryo.register(Response.class);
        return kryo;
    }
}
