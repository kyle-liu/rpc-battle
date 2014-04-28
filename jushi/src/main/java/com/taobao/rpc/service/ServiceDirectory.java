package com.taobao.rpc.service;

import java.util.List;

/** @author <a href="mailto:jushi@taobao.com">jushi<a> */
public interface ServiceDirectory {
    List<ServiceIndex> listBy(String exporter);
}
