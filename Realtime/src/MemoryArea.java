// MemoryArea.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/** <code>MemoryArea</code> is the abstract base class of all classes dealing
 *  with the representations of allocatable memory areas, including the
 *  immortal memory area, physical memory and scoped memory areas.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public abstract class MemoryArea {

    /** The size of this MemoryArea. */
    protected long size;

    /** The size of the consumed memory */
    // Size is somewhat inaccurate
    protected long memoryConsumed;

    /** Indicates whether this is a ScopedMemory. */
    boolean scoped;

    /** Indicates whether this is a HeapMemory. */
    boolean heap;

    /** Indicates whether this is a NullMemoryArea. */
    protected boolean nullMem;

    /** Every MemoryArea has a unique ID. */
    protected int id;

    /** The run() method of this object is called whenever enter() is called. */
    protected Runnable logic;

    /** This is used to create the unique ID. */
    private static int num = 0;

    /** Indicates whether this memoryArea refers to a constant or not. 
     *  This is set by the compiler.
     */
    boolean constant;

    /** The shadow of <code>this</code>. */
    MemoryArea shadow;
  
    abstract protected void initNative(long sizeInBytes);

    /** Create an instance of <code>MemoryArea</code>.
     *
     *  @param sizeInBytes The size of <code>MemoryArea</code> to allocate, in bytes.
     */
    protected MemoryArea(long sizeInBytes) {
	size = sizeInBytes;
	preSetup();
	initNative(sizeInBytes);
	postSetup();
    }

    protected void preSetup() {
	scoped = false; 
	heap = false;
	id = num++;
	constant = false;
	nullMem = false;
    }
    
    protected void postSetup() {
        (shadow = shadow()).shadow = shadow;
	registerFinal();
    }

    // Do we really need this abstract method?
    // protected abstract void initNative(long sizeInBytes);

    /** Create an instance of <code>MemoryArea</code>.
     *
     *  @param sizeInBytes The size of <code>MemoryArea</code> to allocate, in bytes.
     *  @param logic The <code>run()</code> method of this object will be called
     *               whenever <code>enter()</code> is called.
     */
    protected MemoryArea(long sizeInBytes, Runnable logic) {
	this(sizeInBytes);
	this.logic = logic;
    }

    /** Create an instance of <code>MemoryArea</code>.
     *
     *  @param size A <code>SizeEstimator</code> object which indicates the amount
     *              of memory required by this <code>MemoryArea</code>.
     */
    protected MemoryArea(SizeEstimator size) {
	this(size.getEstimate());
    }

    /** Create an instance of <code>MemoryArea</code>.
     *
     *  @param size A <code>SizeEstimator</code> object which indicates the amount
     *              of memory required by this <code>MemoryArea</code>.
     *  @param logic The <code>run()</code> method of this object will be called
     *               whenever <code>enter()</code> is called.
     */
    protected MemoryArea(SizeEstimator size,
			 Runnable logic) {
	this(size);
	this.logic = logic;
    }

    /** Associate this memory area to the current real-time thread for the
     *  duration of the execution of the <code>run()</code> method of the
     *  <code>java.lang.Runnable</code> passed at construction time. During
     *  this bound period of execution, all objects are allocated from the
     *  memory area until another one takes effect, or the <code>enter()</code>
     *  method is exited. A runtime exception is thrown if this method is
     *  called from thread other than a <code>RealtimeThread</code> or
     *  <code>NoHeapRealtimeThrea</code>.
     *
     *  @throws java.lang.IllegalArgumentException Thrown if no <code>Runnable</code>
     *                                             was passed in the constructor.
     */
    public void enter() {
	enter(this.logic);
    }

    /** Associate this memory area to the current real-time thread for the
     *  duration of the execution of the <code>run()</code> method of the
     *  <code>java.lang.Runnable</code> passed at construction time. During
     *  this bound period of execution, all objects are allocated from the
     *  memory area until another one takes effect, or the <code>enter()</code>
     *  method is exited. A runtime exception is thrown if this method is
     *  called from thread other than a <code>RealtimeThread</code> or
     *  <code>NoHeapRealtimeThrea</code>.
     *
     *  @param logic The <code>Runnable</code> object whose <code>run()</code>
     *               method should be invoked.
     */
    public void enter(Runnable logic) {
	RealtimeThread.checkInit();
	RealtimeThread current = RealtimeThread.currentRealtimeThread();
	MemoryArea oldMem = current.memoryArea();
	current.enter(shadow, this);
	try {
	    logic.run();
	} catch (Throwable e) {
	    try {
		e.memoryArea = getMemoryArea(e);
		oldMem.checkAccess(e);
	    } catch (Exception checkException) {
		current.exitMem();
		throw new ThrowBoundaryError("An exception occurred that was " +
					     "allocated in an inner scope that " +
					     "just exited.");
	    }
	    current.exitMem();
//	    throw new ThrowBoundaryError(e.toString());
	    throw new ThrowBoundaryError();
	}
	current.exitMem();
    }

    /** Execute the <code>run()</code> method from the <code>logic</code> parameter
     *  using this memory area as the current allocation context. If the memory
     *  area is a scoped memory type, this method behaves as if it had moved the
     *  allocation context up the scope stack to the occurrence of the memory area.
     *  If the memory area is heap or immortal memory, this method behaves as if
     *  the <code>run()</code> method were running in that memory type with an
     *  empty scope stack.
     *
     *  @param logic The runnable object whose <code>run()</code> method should be
     *               executed.
     *  @throws IllegalStateException A non-realtime thread attempted to enter the
     *                                memory area.
     *  @throws InaccessibleAreaException The memory area is not in the thread's
     *                                    scope stack.
     */
    public void executeInArea(Runnable logic)
	throws InaccessibleAreaException {
	enter(logic);  // In our system, this is a subset of enter for all practical purposes...
    }

    /** Gets the <code>MemoryArea</code> in which the given object is located.
     *
     *  @return The current instance of <code>MemoryArea</code> of the object.
     */
    public static MemoryArea getMemoryArea(Object object) {
	if (object == null) {
	    return ImmortalMemory.instance();
	}
	MemoryArea mem = object.memoryArea;
  	if (mem == null) { // Native methods return objects 
  	    // allocated out of the current scope.
	    return RealtimeThread.currentRealtimeThread().memoryArea();
	} 
	if (mem.constant) { 
	    // Constants are allocated out of ImmortalMemory
	    // Also, static objects before RTJ is setup...
	    return ImmortalMemory.instance();
	}
	return mem;
    }
    
    /** An exact count, in bytes, of the all of the memory currently used by the
     *  system for the allocated objects.
     *
     *  @return The amount of memory consumed.
     */
    public long memoryConsumed() {
	return memoryConsumed;
    }
    
    /** An approximation to the total amount of memory currently available for
     *  future allocated objects, measured in bytes.
     *
     *  @return The amount of remaining memory in bytes.
     */
    public long memoryRemaining() {
	return size() - memoryConsumed();
    }
    
    protected native Object newArray(RealtimeThread rt, 
				     Class type, 
				     int number);
    protected native Object newArray(RealtimeThread rt, 
				     Class type, int[] dimensions);

    /** Allocate an array of the given type in this memory area.
     *
     *  @param type The class of the elements of the new array.
     *  @param number The number of elements in the new array.
     *  @throws java.lang.IllegalAccessException The class or initializer is
     *                                           inaccessible.
     *  @throws java.lang.InstantiationException The array cannot be instantiated.
     *  @throws OutOfMemoryError Space in the memory area is exhaused.
     */
    public Object newArray(final Class type, final int number) 
	throws IllegalAccessException, InstantiationException,
	       OutOfMemoryError {
	RealtimeThread.checkInit();
	RealtimeThread rt = RealtimeThread.currentRealtimeThread();
	if (number<0) throw new NegativeArraySizeException();

	rt.memoryArea().checkNewInstance(shadow);
	Object o;
	(o = newArray(rt, type, number)).memoryArea = shadow;
	return o;
    }

    static Class[] nullClassArr = null;
    static Object[] nullObjArr = null;

    /** Allocate an object in this memory area.
     *
     *  @param type The class of which to create a new instance.
     *  @throws java.lang.IllegalAccessException The class or initializer is
     *                                           inaccessible.
     *  @throws java.lang.InstantiationException The specified class object
     *                                           could not be instantiated.
     *                                           Possible causes are: it is an
     *                                           interface; it is abstract; it
     *                                           is an array, or an exception
     *                                           was thrown by the constructor.
     *  @throws OutOfMemoryError Space in the memory area is exhaused.
     */

    public Object newInstance(Class type)
	throws IllegalAccessException, InstantiationException, OutOfMemoryError {
	if (nullClassArr == null) {
	    nullClassArr = new Class[0];
	    nullObjArr = new Object[0];
	    nullClassArr.memoryArea = HeapMemory.instance();
	    nullObjArr.memoryArea = HeapMemory.instance();
	}
	if (nullClassArr.memoryArea.heap&&(!RealtimeThread.RTJ_init_in_progress)) {
	    final ImmortalMemory im = ImmortalMemory.instance();
	    nullClassArr = (Class[])im.newArray(Class.class, 0);
	    nullObjArr = (Object[])im.newArray(Object.class, 0);
	}
	return newInstance(type, nullClassArr, nullObjArr);
    }

    /** Allocate an object in this memory area. */
    public Object newInstance(final Class type,
			      final Class[] parameterTypes,
			      final Object[] parameters) 
	throws IllegalAccessException, InstantiationException, OutOfMemoryError {
	Constructor c;
	try {
	    // Type.getConstructor doesn't allocate any that needs deallocation 
	    c = type.getConstructor(parameterTypes); 
	} catch (Exception e) {
	    throw new InstantiationException(e.toString());
	}
	
	return newInstance(c, parameters);
    }
    
    /** Allocate an object in this memory area.
     *
     *  @throws java.lang.IllegalAccessException The class or initializer is
     *                                           inaccessible.
     *  @throws java.lang.InstantiationException The specified class object
     *                                           could not be instantianted.
     *                                           Possible causes are: it is an
     *                                           interface, it is abstract, it
     *                                           is an array, or an exception
     *                                           was thrown by the constructor.
     *  @throws OutOfMemoryError Space in the memory area is exhaused.
     */
    public Object newInstance(Constructor c, Object[] args)
	throws IllegalAccessException, InstantiationException,
	       OutOfMemoryError {
	try {
	    RealtimeThread.checkInit();
	    RealtimeThread rt = RealtimeThread.currentRealtimeThread();
	    rt.memoryArea().checkNewInstance(shadow);
	    Object o;
	    try {
		o = newInstance(rt, c, args);
	    } catch (InvocationTargetException e) {
		throw new InstantiationException(e.getMessage());
	    }
	    o.memoryArea = shadow;
	    return o;
	} catch (IllegalAssignmentError e) {
	    throw new IllegalAccessException(e.toString());
	}
    }

    /** Query the size of the memory area. The returned value is the
     *  current size. Current size may be larger than initial size for
     *  those areas that are allowed to grow.
     */
    public long size() {
	return size;
    }
    

    // METHODS NOT IN SPECS

    protected native Object newInstance(RealtimeThread rt, 
					Constructor constructor, 
					Object[] parameters)
	throws InvocationTargetException;


    /** Explicitly unsafe way to get by without polluting the previous scope with a Runnable */
    static void startMem(MemoryArea mem) {
	RealtimeThread rt = RealtimeThread.currentRealtimeThread();
	rt.checkDepth++;
	rt.enter(mem.shadow, mem);
    }

    static void stopMem() {
	RealtimeThread rt = RealtimeThread.currentRealtimeThread();
	if ((--rt.checkDepth)<0) {
	    NoHeapRealtimeThread.print("Error: stopMem>startMem\n");
	    System.exit(-1);
	}
	rt.exitMem();
    }


    protected MemoryArea(long minimum, long maximum) {
	size = maximum;
	preSetup();
	/* Sub-class will do postSetup */
    }

    protected native void enterMemBlock(RealtimeThread rt, MemAreaStack mas);
    protected native void exitMemBlock(RealtimeThread rt, MemAreaStack mas);
    protected native void throwIllegalAssignmentError(Object obj, 
						      MemoryArea ma)
	throws IllegalAssignmentError;

    /** */

    protected native MemoryArea shadow();

    /** */
  
    protected native void registerFinal();
  
    /** */

    public void bless(java.lang.Object obj) { 
	obj.memoryArea = shadow;
    }
    
    /** Create a new array, allocated out of this MemoryArea. 
     */
    
    public Object newArray(final Class type,
			   final int[] dimensions) 
	throws IllegalAccessException, OutOfMemoryError
    {
	RealtimeThread.checkInit();
	RealtimeThread rt = RealtimeThread.currentRealtimeThread();
	for (int i = 0; i<dimensions.length; i++) {
	    if (dimensions[i]<0) {
		throw new NegativeArraySizeException();
	    }
	}
	rt.memoryArea().checkNewInstance(shadow);
	Object o;
	(o = newArray(rt, type, dimensions)).memoryArea = shadow;
	return o;
    }

    /** Check to see if this object can be accessed from this MemoryArea
     */

    public void checkAccess(Object obj) {
	if ((obj != null) && (obj.memoryArea != null) && 
	    obj.memoryArea.scoped) {
	    throwIllegalAssignmentError(obj, obj.memoryArea);
	}
    }
    
    /** Check access restrictions for a newInstance
     */

    public void checkNewInstance(MemoryArea mem) {
	if (mem.scoped) {
	    throw new IllegalAssignmentError("Illegal assignment during new instance!");
	}
    }

    /** Get the outerScope of this MemoryArea, for non-ScopedMemory's,
     *  this defaults to null.
     */

    public MemoryArea getOuterScope() {
	return null;
    }
    
    /** Output a helpful string describing this MemoryArea.
     */

    public String toString() {
	return String.valueOf(id);
    }
}
