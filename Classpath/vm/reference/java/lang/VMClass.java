/* VMClass.java -- VM Specific Class methods
   Copyright (C) 2003 Free Software Foundation

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

package java.lang;

import gnu.classpath.RawData;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/*
 * This class is a reference version, mainly for compiling a class library
 * jar.  It is likely that VM implementers replace this with their own
 * version that can communicate effectively with the VM.
 */

/**
 *
 * @author Etienne Gagnon <etienne.gagnon@uqam.ca>
 * @author Archie Cobbs <archie@dellroad.org>
 * @author C. Brian Jones <cbj@gnu.org>
 */
public final class VMClass 
{
  /*
   * Class initialization mostly-in-Java copied from SableVM.
   * The steps below follow the JVM spec, 2nd edition, sec. 2.17.5.
   */
  private int initializing_thread;
  private boolean erroneous_state;

  private native boolean isInitialized();
  private native void setInitialized();
  private native void step7();
  private native void step8();

  /**
   * Pointer to VM internal class structure.
   */

  private final RawData vmData;  

  private VMClass(RawData vmData) 
  { 
    this.vmData = vmData;
  }

  private void initialize(int thread) throws InterruptedException
  {
    Error error;

    /* 1 */
    synchronized (this)
    {
      /* 2 */
      while (initializing_thread != 0 && initializing_thread != thread)
        wait();

      /* 3 */
      if (initializing_thread == thread)
        return;

      /* 4 */
      if (isInitialized())
        return;

      /* 5 */
      if (erroneous_state)
        throw new NoClassDefFoundError();

      /* 6 */
      initializing_thread = thread;
    }

    /* 7 */
    try {
      step7();
    }
    catch(Error e) {
      synchronized(this) {
        erroneous_state = true;
        initializing_thread = 0;
        notifyAll();
        throw e;
      }
    }

    /* 8 */
    try {
      step8();

      /* 9 */
      synchronized(this) {
        setInitialized();
        initializing_thread = 0;
        notifyAll();
        return;
      }
    }

    /* 10 */
    catch(Exception e) {
      try {
        error = new ExceptionInInitializerError(e);
      } catch (OutOfMemoryError e2) {
        error = e2;
      }
    } catch(Error e) {
      error = e;
    }

    /* 11 */
    synchronized(this) {
      erroneous_state = true;
      initializing_thread = 0;
      notifyAll();
      throw error;
    }
  }

  /**
   * Discover whether an Object is an instance of this Class.  Think of it
   * as almost like <code>o instanceof (this class)</code>.
   *
   * @param o the Object to check
   * @return whether o is an instance of this class
   * @since 1.1
   */
  native boolean isInstance(Object o);

  /**
   * Discover whether an instance of the Class parameter would be an
   * instance of this Class as well.  Think of doing
   * <code>isInstance(c.newInstance())</code> or even
   * <code>c.newInstance() instanceof (this class)</code>. While this
   * checks widening conversions for objects, it must be exact for primitive
   * types.
   *
   * @param c the class to check
   * @return whether an instance of c would be an instance of this class
   *         as well
   * @throws NullPointerException if c is null
   * @since 1.1
   */
  native boolean isAssignableFrom(Class c);

  /**
   * Check whether this class is an interface or not.  Array types are not
   * interfaces.
   *
   * @return whether this class is an interface or not
   */
  native boolean isInterface();

  /**
   * Return whether this class is a primitive type.  A primitive type class
   * is a class representing a kind of "placeholder" for the various
   * primitive types, or void.  You can access the various primitive type
   * classes through java.lang.Boolean.TYPE, java.lang.Integer.TYPE, etc.,
   * or through boolean.class, int.class, etc.
   *
   * @return whether this class is a primitive type
   * @see Boolean#TYPE
   * @see Byte#TYPE
   * @see Character#TYPE
   * @see Short#TYPE
   * @see Integer#TYPE
   * @see Long#TYPE
   * @see Float#TYPE
   * @see Double#TYPE
   * @see Void#TYPE
   * @since 1.1
   */
  native boolean isPrimitive();

  /**
   * Get the name of this class, separated by dots for package separators.
   * Primitive types and arrays are encoded as:
   * <pre>
   * boolean             Z
   * byte                B
   * char                C
   * short               S
   * int                 I
   * long                J
   * float               F
   * double              D
   * void                V
   * array type          [<em>element type</em>
   * class or interface, alone: &lt;dotted name&gt;
   * class or interface, as element type: L&lt;dotted name&gt;;
   *
   * @return the name of this class
   */
  native String getName();

  /**
   * Get the direct superclass of this class.  If this is an interface,
   * Object, a primitive type, or void, it will return null. If this is an
   * array type, it will return Object.
   *
   * @return the direct superclass of this class
   */
  native Class getSuperclass();

  /**
   * Get the interfaces this class <EM>directly</EM> implements, in the
   * order that they were declared. This returns an empty array, not null,
   * for Object, primitives, void, and classes or interfaces with no direct
   * superinterface. Array types return Cloneable and Serializable.
   *
   * @return the interfaces this class directly implements
   */
  native Class[] getInterfaces();

  /**
   * If this is an array, get the Class representing the type of array.
   * Examples: "[[Ljava.lang.String;" would return "[Ljava.lang.String;", and
   * calling getComponentType on that would give "java.lang.String".  If
   * this is not an array, returns null.
   *
   * @return the array type of this class, or null
   * @see Array
   * @since 1.1
   */
  native Class getComponentType();

  /**
   * Get the modifiers of this class.  These can be decoded using Modifier,
   * and is limited to one of public, protected, or private, and any of
   * final, static, abstract, or interface. An array class has the same
   * public, protected, or private modifier as its component type, and is
   * marked final but not an interface. Primitive types and void are marked
   * public and final, but not an interface.
   *
   * @return the modifiers of this class
   * @see Modifer
   * @since 1.1
   */
  native int getModifiers();

  /**
   * If this is a nested or inner class, return the class that declared it.
   * If not, return null.
   *
   * @return the declaring class of this class
   * @since 1.1
   */
  native Class getDeclaringClass();

  /**
   * Like <code>getDeclaredClasses()</code> but without the security checks.
   *
   * @param pulicOnly Only public classes should be returned
   */
  native Class[] getDeclaredClasses(boolean publicOnly);

  /**
   * Like <code>getDeclaredFields()</code> but without the security checks.
   *
   * @param pulicOnly Only public fields should be returned
   */
  native Field[] getDeclaredFields(boolean publicOnly);

  /**
   * Like <code>getDeclaredMethods()</code> but without the security checks.
   *
   * @param pulicOnly Only public methods should be returned
   */
  native Method[] getDeclaredMethods(boolean publicOnly);

  /**
   * Like <code>getDeclaredConstructors()</code> but without
   * the security checks.
   *
   * @param pulicOnly Only public constructors should be returned
   */
  native Constructor[] getDeclaredConstructors(boolean publicOnly);

  /**
   * Return the class loader of this class.
   *
   * @return the class loader
   */
  native ClassLoader getClassLoader();

  /**
   * VM implementors are free to make this method a noop if 
   * the default implementation is acceptable.
   *
   * @param name the name of the class to find
   * @return the Class object representing the class or null for noop
   * @throws ClassNotFoundException if the class was not found by the
   *         classloader
   * @throws LinkageError if linking the class fails
   * @throws ExceptionInInitializerError if the class loads, but an exception
   *         occurs during initialization
   */
  static native Class forName(String name) throws ClassNotFoundException;

  /**
   * Return whether this class is an array type.
   *
   * @return 1 if this class is an array type, 0 otherwise, -1 if unsupported
   * operation
   */
  native int isArray();

  /**
   * This method should trigger class initialization (if the
   * class hasn't already been initialized)
   * 
   * @throws ExceptionInInitializerError if an exception
   *         occurs during initialization
   */
  native void initialize();

  /**
   * Load an array class.
   *
   * @return the Class object representing the class
   * @throws ClassNotFoundException if the class was not found by the
   *         classloader
   */
  static native Class loadArrayClass(String name, ClassLoader classloader)
	throws ClassNotFoundException;

  /**
   * Throw a checked exception without declaring it.
   */
  static native void throwException(Throwable t);

} // class VMClass
