package com.taobao.rpc.zaza.model;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;

import com.taobao.rpc.zaza.util.ZazaConfigUtil;

public class ZazaThreadPoolModel {
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(ZazaThreadPoolModel.class);

    public final InnerWorker[] clientWorkers;
    public final static ZazaThreadPoolModel intance = new ZazaThreadPoolModel();

    private ZazaThreadPoolModel() {
        // Find a power of 2 >= cpuCores
        int coreCount = Runtime.getRuntime().availableProcessors();
        if (ZazaConfigUtil.getMaxServerLogicThread() == 16) {
            coreCount = coreCount * 2;
        }
        int workerCount = 1;
        while (workerCount < coreCount) {
            workerCount <<= 1;
        }
        clientWorkers = new InnerWorker[workerCount];
        for (int mod = 0; mod < workerCount; ++mod) {
            clientWorkers[mod] = new InnerWorker("LogicThread" + mod);
            clientWorkers[mod].start();
        }

        if (ZazaConfigUtil.getMaxServerLogicThread() == 16) {
            Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(new Runnable() {

                @Override
                public void run() {
                    for (InnerWorker innerWorker : clientWorkers) {
                        logger.error(innerWorker.getName() + ":" + innerWorker.getSizeOfTask());
                    }

                }
            }, 5000, 15000, TimeUnit.MILLISECONDS);
        }
    }

    public static class InnerWorker extends Thread {
        private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();

        InnerWorker(String name) {
            setDaemon(false);
            setName(name);
        }

        public void putTask(Runnable task) {
            queue.offer(task);
        }

        public int getSizeOfTask() {
            return queue.size();
        }

        @Override
        public void run() {
            try {
                while (true) {
                    queue.take().run();
                }
            } catch (Throwable e) {
                logger.error("[client-worker-error] " + e.toString(), e);
            }
        }
    }
}
