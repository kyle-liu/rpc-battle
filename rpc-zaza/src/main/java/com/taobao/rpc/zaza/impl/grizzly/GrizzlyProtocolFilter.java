package com.taobao.rpc.zaza.impl.grizzly;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrizzlyProtocolFilter extends BaseFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrizzlyProtocolFilter.class);

    public NextAction handleRead(FilterChainContext ctx) throws IOException {
        final Object message = ctx.getMessage();
        if (message instanceof IncompleteBufferHolder) {
            return ctx.getStopAction(((IncompleteBufferHolder) message).buffer);
        }

        final Buffer buffer = (Buffer) message;
        try {
            final List<Object> list = new ArrayList<Object>();
            Object object;
            while ((object = GrizzlyZazaProtocol.decode(buffer)) != null) {
                list.add(object);
            }

            if (list.isEmpty()) {
                return ctx.getStopAction(buffer);
            } else {
                final Buffer remainder = buffer.hasRemaining() ? buffer.split(buffer.position()) : null;
                buffer.dispose();

                ctx.setMessage(list);
                return ctx.getInvokeAction(new IncompleteBufferHolder(remainder));
            }
        } catch (Exception e) {
            LOGGER.error("decode message error", e);
            throw new IOException(e);
        }
    }

    // encode object
    public NextAction handleWrite(FilterChainContext ctx) throws IOException {
        try {
            final Buffer buffer = GrizzlyZazaProtocol.encode(ctx.getMessage(), ctx);
            buffer.trim();
            ctx.setMessage(buffer);
            return ctx.getInvokeAction();

        } catch (Exception e) {
            throw new IOException("encode message to byte error", e);
        }
    }

    private static class IncompleteBufferHolder {

        public IncompleteBufferHolder(Buffer buffer) {
            this.buffer = buffer;
        }

        private Buffer buffer;
    }
}
