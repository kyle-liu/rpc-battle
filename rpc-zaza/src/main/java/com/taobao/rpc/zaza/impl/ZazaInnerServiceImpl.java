package com.taobao.rpc.zaza.impl;

import java.util.Map;

import com.taobao.rpc.zaza.interfaces.ZazaInnerService;
import com.taobao.rpc.zaza.model.ZazaMethodDataModel;

public class ZazaInnerServiceImpl implements ZazaInnerService {

    @Override
    public Map<String, Byte> fetchMethodCodes() {
        return ZazaMethodDataModel.instance.getMthodName2CodeMapAsServerSide();
    }

}
