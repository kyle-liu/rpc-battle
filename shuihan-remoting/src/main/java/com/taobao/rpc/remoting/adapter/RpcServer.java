package com.taobao.rpc.remoting.adapter;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.taobao.gecko.service.RemotingFactory;
import com.taobao.gecko.service.RemotingServer;
import com.taobao.gecko.service.config.ServerConfig;
import com.taobao.gecko.service.exception.NotifyRemotingException;
import com.taobao.rpc.remoting.command.RpcInvokeCommand;
import com.taobao.rpc.remoting.factory.RpcWireFormatType;
import com.taobao.rpc.remoting.processor.PrcRequestProcessor;

public class RpcServer {

	protected ServerConfig serverConfig;
	protected RemotingServer remotingServer;
	private int corePoolSize = 200;
	private int maxPoolSize = 300;
	private ThreadPoolExecutor executor;
	private PrcRequestProcessor requestProcessor;

	public RpcServer() {

	}

	public int getCorePoolSize() {
		return corePoolSize;
	}

	public void setCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
	}

	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	public void start(int port) {
		executor = new ThreadPoolExecutor(this.corePoolSize, this.maxPoolSize,
				60, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(
						10000));
		this.initServerConfig(port);
		this.initRemotingServer();
		this.startRemotingServer();
	}

	public void stop() {
		try {
			this.remotingServer.stop();
		} catch (NotifyRemotingException e) {
			e.printStackTrace();
		}
	}

	public void export(Class<?> type, Object object) {
		this.requestProcessor.registerService(type, object);
	}

	private void initRemotingServer() {
		this.remotingServer = RemotingFactory
				.newRemotingServer(this.serverConfig);
		requestProcessor = new PrcRequestProcessor();
		requestProcessor.setExecutor(this.executor);
		this.remotingServer.registerProcessor(RpcInvokeCommand.class,
				requestProcessor);
	}

	private void startRemotingServer() {
		try {
			this.remotingServer.start();
		} catch (final NotifyRemotingException e) {
			e.printStackTrace();
		}
	}

	private void initServerConfig(int port) {
		this.serverConfig = new ServerConfig();
		this.serverConfig.setWireFormatType(new RpcWireFormatType());
		this.serverConfig.setPort(port);
		this.serverConfig
				.setCallBackExecutorPoolSize(2 * getSystemThreadCount());
		this.serverConfig.setCallBackExecutorQueueSize(100000);
		this.serverConfig
				.setMaxCallBackExecutorPoolSize(4 * getSystemThreadCount());
		this.serverConfig.setMaxScheduleWrittenBytes(Runtime.getRuntime()
				.maxMemory() / 4);
	}

	public static final int getSystemThreadCount() {
		int cpus = getCpuProcessorCount();
		return cpus > 8 ? 4 + cpus * 5 / 8 : cpus + 1;
	}

	public static final int getCpuProcessorCount() {
		return Runtime.getRuntime().availableProcessors();
	}

}
