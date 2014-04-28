package shenfeng.simplerpc;

import java.util.concurrent.atomic.AtomicInteger;

class Signal {

	private static AtomicInteger seq = new AtomicInteger(0);

	private final short id;

	private byte[] out;

	private byte[] in;

	public Signal(short id) {
		this.id = id;
	}

	Signal() {
		this((short) seq.addAndGet(1));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Signal other = (Signal) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public short getId() {
		return id;
	}

	public synchronized byte[] getIn() {
		try {
			while (in == null) {
				wait();
			}
			return in;
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			in = null;
		}
	}

	public synchronized byte[] getOut() {
		try {
			return out;
		} finally {
			out = null;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	public synchronized void setIn(byte[] in) {
		this.in = in;
		notify();
	}

	public synchronized void setOut(byte[] out) {
		this.out = out;
	}

}