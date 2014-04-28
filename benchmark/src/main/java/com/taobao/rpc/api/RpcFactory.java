package com.taobao.rpc.api;

/**
 * The factory of rpc implementation.
 *
 * @author ding.lid
 */
public interface RpcFactory {
    int DEFAULT_PORT = 12121;

    /**
     * Export a local object to remote service.
     * <p />
     * Don't <i>block</i> this method after exported so as to export several service conveniently.
     *
     * @param type Service type class
     * @param serviceObject Local Service Object
     * @param <T> Service type
     * @throws RpcException
     */
    <T> void export(Class<T> type, T serviceObject);

    /**
     * Get service reference
     *
     * @param type Service type class
     * @param ip remote host ip
     * @param <T> Service type
     * @throws RpcException
     * @return service reference
     */
    <T> T getReference(Class<T> type, String ip);

    /**
     * get the client thread count to run benchmark.
     */
    int getClientThreads();

    /**
     * get your id, please use your mail prefix and put your alias name in brackets.
     * <br>
     * eg, <code>shutong.dy(叔同)</code>
     */
    String getAuthorId();
}
