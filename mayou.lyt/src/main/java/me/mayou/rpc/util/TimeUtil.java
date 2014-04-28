package me.mayou.rpc.util;

public class TimeUtil {

	private static volatile long current = System.currentTimeMillis();

	static {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(1000);
						current = System.currentTimeMillis();
					} catch (InterruptedException e) {
						// do nothing
					}
				}
			}

		});
//		thread.start();
	}
	
	public static long currentTime(){
		return current;
	}

}
