// NoHeapRealtimeThread.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** A <code>NoHeapRealtimeThread</code> is a specialized form of
 *  <code>RealtimeThread</code>. Because an instance of
 *  <code>NoHeapRealtimeThread</code> may immediately preempt any
 *  implemented garbage collector logic contained in its <code>run()</code>
 *  is never allowed to allocated or reference any object allocated
 *  in the heap nor is it even allowed to manipulate any reference
 *  to any object in the heap. For example, if <code>a</code> and
 *  <code>b</code> are objects in immortal memory, <code>b.p</code>
 *  is reference to an object on the heap, and <code>a.p</code> is
 *  type complatible with <code>b.p</code>, then a
 *  <code>NoHeapRealtimeThread</code> is <i>not</i> allowed to
 *  execute anything like the following:
 *  <p>
 *  <code>a.p = b.p; b.p = null;</code>
 *  <p>
 *  Thus, it is always safe for a <code>NoHeapRealtimeThread</code>
 *  to interrupt the garbage collector at any time, without waiting
 *  for the end of the garbage collection cycle or a defined preemption
 *  point. Due to these restrictions, a <code>NoHeapRealtimeThread</code>
 *  object must be placed in a memory area such that thread logic
 *  may unexceptionally access instance variables and such that
 *  Java methods on <code>java.lang.Thread</code> (e.g., enumerate
 *  and join) complete normally except where the execution would
 *  cause access violations. The constructors of
 *  <code>NoHeapRealtimeThread</code> require a reference to
 *  <code>ScopedMemory</code> or <code>ImmortalMemory</code>.
 *  <p>
 *  When the thread is started, all execution occurs in the scope
 *  of the given memory area. Thus, all memory allocation performed
 *  with the "new" operator is taken from this given area.
 *  <p>
 *  Parameters for constructors may be <code>null</code>. In such
 *  cases the default value will be the default value set for the
 *  particular type by the associated instance of <code>Scheduler</code>.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public class NoHeapRealtimeThread extends RealtimeThread {

    /** Create a <code>NoHeapRealtimeThread</code>.
     *
     *  @param sp A <code>SchedulingParameters</code> object that will be
     *            associated with <code>this</code>. A null value means
     *            this will not have an associated
     *            <code>SchedulingParameters</code> object.
     *  @param ma A <code>MemoryArea</code> object. Must be a
     *            <code>ScopedMemory</code> or <code>ImmortalMemory</code>
     *            type. A null value causes an <code>IllegalArgumentException<code>
     *            to be thrown.
     *  @throws java.lang.IllegalArgumentException If the memory area
     *                                             parameter is null.
     */
    public NoHeapRealtimeThread(SchedulingParameters sp, MemoryArea ma)
	throws IllegalArgumentException {
	schedulingParameters = sp;
	memoryArea = ma;
	noHeap = true;
    }

    /** Create a <code>NoHeapRealtimeThread</code>.
     *
     *  @param sp A <code>SchedulingParameters</code> object that will be
     *            associated with <code>this</code>. A null value means
     *            this will not have an associated
     *            <code>SchedulingParameters</code> object.
     *  @param rp A <code>ReleaseParameters</code> object that will be
     *            associated with <code>this</code>. A null value means
     *            this will not have an associated <code>ReleaseParameters</code>
     *            object.
     *  @param ma A <code>MemoryArea</code> object. Must be a
     *            <code>ScopedMemory</code> or <code>ImmortalMemory</code>
     *            type. A null value causes an <code>IllegalArgumentException<code>
     *            to be thrown.
     *  @throws java.lang.IllegalArgumentException If the memory area
     *                                             parameter is null.
     */
    public NoHeapRealtimeThread(SchedulingParameters sp, ReleaseParameters rp,
			       MemoryArea ma) throws IllegalArgumentException {
	this(sp, ma);
	releaseParameters = rp;
    }

    /** Create a <code>NoHeapRealtimeThread</code>.
     *
     *  @param sp A <code>SchedulingParameters</code> object that will be
     *            associated with <code>this</code>. A null value means
     *            this will not have an associated
     *            <code>SchedulingParameters</code> object.
     *  @param rp A <code>ReleaseParameters</code> object that will be
     *            associated with <code>this</code>. A null value means
     *            this will not have an associated <code>ReleaseParameters</code>
     *            object.
     *  @param mp A <code>MemoryParameters</code> object that will be
     *            associated with <code>this</code>. A null value means
     *            this will not have a <code>MemoryParameters</code> object.
     *  @param ma A <code>MemoryArea</code> object. Must be a
     *            <code>ScopedMemory</code> or <code>ImmortalMemory</code>
     *            type. A null value causes an <code>IllegalArgumentException<code>
     *            to be thrown.
     *  @param group A <code>ProcessingGroupParameters</code> object that will
     *               be associated with <code>this</code> A null value means this
     *               will not have an associated <code>ProcessingGroupParameters</code>
     *               object.
     *  @param logic A <code>Runnable</code> whose <code>run()</code> method will
     *               be executed for <code>this</code>.
     *  @throws java.lang.IllegalArgumentException If the memory area
     *                                             parameter is null.
     */
    public NoHeapRealtimeThread(SchedulingParameters sp, ReleaseParameters rp,
				MemoryParameters mp, MemoryArea ma,
				ProcessingGroupParameters group, Runnable logic)
	throws IllegalArgumentException {
	super(sp, rp, mp, ma, group, logic);
    }

    /** Construct a <code>NoHeapRealtimeThread</code> which will execute in the
     *  given <code>MemoryArea</code>
     */
    public NoHeapRealtimeThread(MemoryArea area) 
	throws IllegalArgumentException 
    {
	super(area);
	setup(area);
    }
    
    /** Construct a <code>NoHeapRealtimeThread</code> which will execute 
     *  <code>logic</code> in the given <code>MemoryArea</code> 
     */
    public NoHeapRealtimeThread(MemoryArea area, Runnable logic) 
	throws IllegalArgumentException 
    {
	super(area, logic);
	setup(area);
    }
    
    /** Setup some state for the constructors */
    private void setup(MemoryArea area) throws IllegalArgumentException {
	if ((area == null) || area.heap) {
	    throw new IllegalArgumentException("invalid MemoryArea");
	} else {
	    mem = area;
	}
	noHeap = true;
    }
    
    /** Construct a new <code>NoHeapRealtimeThread</code> that will inherit
     *  the properties described in <code>MemoryParameters</code> and will
     *  run <code>logic</code>.
     */
    public NoHeapRealtimeThread(MemoryParameters mp, Runnable logic) {
	this(mp.getMemoryArea(), logic);
    }
    
    /** Check to see if a write is possible to the given object. 
     *  Warning: this method can only be used when we're not really running
     *  <code>NoHeapRealtimeThreads</code> for real, because you can't access
     *  the object at all in a real <code>NoHeapRealtimeThread</code>. 
     */
    
    public NoHeapRealtimeThread(SchedulingParameters scheduling,
				ReleaseParameters release) {
	super(scheduling, release);
    }

    /** Checks if the <code>NoHeapRealtimeThread</code> is startable and starts
     *  it if it is. Checks that the parameters associated with this
     *  <code>NoHeapRealtimeThread</code> object are not allocated in heap. Also,
     *  checks if <code>this</code> object is allocated in heap. If any of them
     *  are allocated, <code>start()</code> throws a <code>MemoryAccessError</code>.
     *
     *  @throws MemoryAccessError If any of the parameters of <code>this</code>
     *                            is allocated on heap.
     */
    public void start() {
	if (getMemoryArea().heap)
	    throw new MemoryAccessError("NoHeapRealtimeThread cannot be allocated in heap.");
	SchedulingParameters scheduling = getSchedulingParameters();
	if ((scheduling != null) && (MemoryArea.getMemoryArea(scheduling).heap))
	    throw new MemoryAccessError("SchedulingParameters of a NoHeapRealtimeThread cannot be allocated in heap.");
	ReleaseParameters release = getReleaseParameters();
	if ((release != null) && (MemoryArea.getMemoryArea(release).heap))
	    throw new MemoryAccessError("ReleaseParameters of a NoHeapRealtimeThread cannot be allocated in heap.");
	MemoryParameters memParams = getMemoryParameters();
	if ((memParams != null) && (MemoryArea.getMemoryArea(memParams).heap))
	    throw new MemoryAccessError("MemoryParameters of a NoHeapRealtimeThread cannot be allocated in heap.");
	ProcessingGroupParameters group = getProcessingGroupParameters();
	if ((group != null) && (MemoryArea.getMemoryArea(group).heap))
	    throw new MemoryAccessError("ProcessingGroupParameters of a NoHeapRealtimeThread cannot be allocated in heap.");

	super.start();
    }

    public void checkNoHeapWrite(Object obj) {
	if ((obj != null) && (obj.memoryArea != null) && obj.memoryArea.heap) {
	    throw new IllegalAssignmentError("Cannot assign " +
					     obj.memoryArea.toString() +
					     " from " + toString());
	}
    }
    
    /** Check to see if a read is possible from the given object.
     *  Warning: this method can only be used when we're not really running
     *  <code>NoHeapRealtimeThreads</code> for real, because you can't access
     *  the object at all in a real <code>NoHeapRealtimeThread</code>.
     */
    public void checkNoHeapRead(Object obj) {
	if ((obj != null) && (obj.memoryArea != null) && obj.memoryArea.heap) {
	    throw new MemoryAccessError("Cannot read " + 
					obj.memoryArea.toString() +
					" from " + toString());
	}
    }
    
    /** Return a String describing this thread. */
    public String toString() {
	return "NoHeapRealtimeThread";
    }
    
    /** A print method that's safe to call from a NoHeapRealtimeThread. */
    public native static void print(String s);
    public native static void print(double d);
    public native static void print(int n);
    public native static void print(long l);
}
