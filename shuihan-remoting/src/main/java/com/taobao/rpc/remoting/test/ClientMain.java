package com.taobao.rpc.remoting.test;

import com.taobao.rpc.api.RpcFactory;
import com.taobao.rpc.benchmark.service.HelloService;
import com.taobao.rpc.benchmark.dataobject.*;
import org.apache.commons.cli.*;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author ding.lid
 */
public class ClientMain {
	static final String constant = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
	static final Person person;
	static {
		person = new Person();
		person.setPersonId("id1");
		person.setLoginName("name1");
		person.setStatus(PersonStatus.ENABLED);

		byte[] attachment = new byte[4000]; // 4K
		Random random = new Random();
		random.nextBytes(attachment);
		person.setAttachment(attachment);

		ArrayList<Phone> phones = new ArrayList<Phone>();
		Phone phone1 = new Phone("86", "0571", "11223344", "001");
		Phone phone2 = new Phone("86", "0571", "11223344", "002");
		phones.add(phone1);
		phones.add(phone2);

		PersonInfo info = new PersonInfo();
		info.setPhones(phones);
		Phone fax = new Phone("86", "0571", "11223344", null);
		info.setFax(fax);
		FullAddress addr = new FullAddress("CN", "zj", "1234", "Road1",
				"333444");
		info.setFullAddress(addr);
		info.setMobileNo("1122334455");
		info.setMale(true);
		info.setDepartment("b2b");
		info.setHomepageUrl("www.abc.com");
		info.setJobTitle("dev");
		info.setName("name2");

		person.setInfo(info);
	}

	public static void main(String[] args) throws Exception {
		Options options = new Options();
		options.addOption("s", "server", true, "server ip");
		options.addOption("d", "delay", true, "count delay when start(ms)");
		options.addOption("t", "time", true, "run time(ms)");
		options.addOption("o", "output", true, "output file");
		options.addOption("h", "help", false, "help");

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);
		if (cmd.hasOption("h")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("Usage", options, true);
			return;
		}

		final String serverIp;
		if (!cmd.hasOption("s")) {
			serverIp = "127.0.0.1";
		} else {
			serverIp = cmd.getOptionValue("s");
			System.out.println("Use server from cmd line: " + serverIp);
		}
		final long runDelay;
		if (!cmd.hasOption("d")) {
			runDelay = 0; // default no delay
		} else {
			runDelay = Integer.parseInt(cmd.getOptionValue("d"));
			System.out.println("Use run delay from cmd line: " + runDelay
					+ "ms");
		}
		final long runTime;
		if (!cmd.hasOption("t")) {
			runTime = 3 * 1000; // default just run 3s
		} else {
			runTime = Integer.parseInt(cmd.getOptionValue("t"));
			System.out.println("Use run time from cmd line: " + runTime + "ms");
		}
		final String outputFile;
		if (!cmd.hasOption("o")) {
			outputFile = null;
		} else {
			outputFile = cmd.getOptionValue("o");
			System.out.println("Use output file from cmd line: " + outputFile);
		}

		final RpcFactory rpcFactory = Util.getRpcFactoryImpl();
		final HelloService reference = rpcFactory.getReference(
				HelloService.class, serverIp);

		final AtomicLong counter = new AtomicLong();
		final AtomicBoolean stopped = new AtomicBoolean(false);
		final AtomicBoolean correct = new AtomicBoolean(true);
		final Thread mainThread = Thread.currentThread();

		final int runThreads = rpcFactory.getClientThreads();

		ExecutorService executorService = Executors
				.newFixedThreadPool(runThreads);
		for (int i = 0; i < runThreads; ++i) {
			final int idx = i;
			Runnable invoker = new Runnable() {
				boolean tag = idx % 2 == 0;

				@Override
				public void run() {
					while (!stopped.get()) {
						if (tag) {
							String foo = reference.helloWorld(constant);
							if (!constant.equals(foo)) {
								correct.set(false);
								stopped.set(true);
								mainThread.interrupt();
							}
						} else {
							Person ret = reference.helloPerson(person);
							if (!person.equals(ret)) { // FIXME DO NOT check
														// every time!
								correct.set(false);
								stopped.set(true);
								mainThread.interrupt();
							}
						}
						counter.incrementAndGet();
					}
				}
			};

			executorService.execute(invoker);
		}

		Thread.sleep(runDelay);

		final long beginCount = counter.get();
		final long startTime = System.currentTimeMillis();
		while (true) {
			long now = System.currentTimeMillis();
			long left = runTime - (now - startTime);
			if (left <= 0)
				break;
			try {
				Thread.sleep(left);
			} catch (InterruptedException e) {
				// ignore
			}
		}
		final long invokeTime = counter.get() - beginCount;
		stopped.set(true);
		executorService.shutdown();
		final String format = "%tY-%<tm-%<td %<tH:%<tM:%<tS, Name:%s, Result:%s, TPS:%.2f, InvokeCount:%d, RunTime:%dms\n";
		final String format_wrong_result = "%tY-%<tm-%<td %<tH:%<tM:%<tS, %s, %s, , , ,\n";
		if (outputFile == null) {
			if (correct.get()) {
				System.out.printf(format, System.currentTimeMillis(),
						rpcFactory.getAuthorId(), correct, 1.0 * invokeTime
								/ runTime * 1000, invokeTime, runTime);
			} else {
				System.out.printf(format_wrong_result,
						System.currentTimeMillis(), rpcFactory.getAuthorId(),
						correct);
			}
			System.out.flush();
		} else {
			PrintWriter out = new PrintWriter(new FileWriter(outputFile, true));
			if (correct.get()) {
				out.printf(format, System.currentTimeMillis(),
						rpcFactory.getAuthorId(), correct, 1.0 * invokeTime
								/ runTime * 1000, invokeTime, runTime);
			} else {
				out.printf(format_wrong_result, System.currentTimeMillis(),
						rpcFactory.getAuthorId(), correct);
			}
			out.flush();
		}

		// System.exit(0);
	}
}
