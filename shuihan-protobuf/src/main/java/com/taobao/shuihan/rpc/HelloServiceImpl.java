package com.taobao.shuihan.rpc;

import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;
import com.google.protobuf.Message;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.google.protobuf.Service;
import com.taobao.shuihan.rpc.protos.PersonProtos;


public class HelloServiceImpl implements Service {

    @Override
    public ServiceDescriptor getDescriptorForType() {
        return PersonProtos.HelloService.getDescriptor();
    }


    @Override
    public void callMethod(MethodDescriptor method, RpcController controller, Message request, RpcCallback<Message> done) {
        done.run(request);
    }


    @Override
    public Message getRequestPrototype(MethodDescriptor method) {
        if (method.getName().indexOf("helloPerson") > 0) {
            return PersonProtos.Person.newBuilder().build();
        }
        else {
            return PersonProtos.callStr.newBuilder().build();
        }
    }


    @Override
    public Message getResponsePrototype(MethodDescriptor method) {
        if (method.getName().indexOf("helloPerson") > 0) {
            return PersonProtos.Person.newBuilder().build();
        }
        else {
            return PersonProtos.callStr.newBuilder().build();
        }
    }

}
