package com.taobao.rpc.zaza.impl.grizzly;

import java.io.IOException;
import java.util.List;

import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;

import com.taobao.rpc.zaza.ZazaResponse;
import com.taobao.rpc.zaza.interfaces.ZazaClient;

public class GrizzlyClientHandler extends BaseFilter {

    private ZazaClient client;

    public void setClient(ZazaClient client) {
        this.client = client;
    }

    public NextAction handleRead(FilterChainContext ctx) throws IOException {
        final Object message = ctx.getMessage();

        try {
            if (message instanceof List) {
                @SuppressWarnings("unchecked")
                List<ZazaResponse> responses = (List<ZazaResponse>) message;
                client.putResponses(responses);
            } else if (message instanceof ZazaResponse) {
                ZazaResponse response = (ZazaResponse) message;
                client.putResponse(response);
            }
        } catch (Exception e) {
        }

        return ctx.getStopAction();
    }
}
