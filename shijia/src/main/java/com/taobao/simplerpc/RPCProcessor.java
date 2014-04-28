/**
 * $Id: RPCProcessor.java 839 2012-12-22 04:55:58Z shijia.wxr $
 */
package com.taobao.simplerpc;

import java.nio.ByteBuffer;


/**
 * Server��Client�Ķ��¼�����
 * 
 * @author vintage.wang@gmail.com  shijia.wxr@taobao.com
 */
public interface RPCProcessor {
    public byte[] process(final int upId, final ByteBuffer upstream);
}
