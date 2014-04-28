package com.taobao.shuihan.rpc;

import com.google.protobuf.BlockingService;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;
import com.google.protobuf.Message;
import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import com.taobao.shuihan.rpc.protos.PersonProtos;


public class HelloBlockServiceImpl implements BlockingService {

    @Override
    public ServiceDescriptor getDescriptorForType() {
        return PersonProtos.HelloService.getDescriptor();
    }


    @Override
    public Message callBlockingMethod(MethodDescriptor method, RpcController controller, Message request)
            throws ServiceException {
        return request;
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
