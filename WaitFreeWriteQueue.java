package javax.realtime;

public class WaitFreeWriteQueue {
    /** The wait-free queue facilitate communication and synchronization
     *  between instances of <code>RealtimeThread</code> and
     *  <code>java.lang.Thread</code>. The problem is that synchronized
     *  access objects shared between real-time threads and threads
     *  might cause the real-time threads to incur delays due to
     *  execution of the garbage collector.
     */

    protected boolean empty = true;
    protected boolean full = false;
    protected int size = 0;

    public WaitFreeWriteQueue(Thread writer, Thread reader,
			      int maximum, MemoryArea memory)
	throws IllegalArgumentException, InstantiationException,
	       ClassNotFoundException, IllegalAccessException {
	// TODO
    }

    public void clear() {
	empty = true;
	full = false;
	// TODO
    }

    public boolean force(Object object) throws MemoryScopeException {
	// TODO

	return false;
    }

    public boolean isEmpty() {
	return empty;
    }

    public boolean isFull() {
	return full;
    }

    public Object read() {
	// TODO

	return null;
    }

    public int size() {
	return size;
    }

    public boolean write(Object object) throws MemoryScopeException {
	// TODO

	return false;
    }
}
