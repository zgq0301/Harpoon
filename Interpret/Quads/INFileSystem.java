// INFileSystem.java, created Thu Jan 27 04:36:13 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Quads;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HClassMutator;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.NoSuchClassException;

import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;

/**
 * <code>INFileSystem</code> provides implementations for (some of) the native
 * methods in <code>java.io.FileSystem</code>.  Actually,
 * <code>java.io.FileSystem</code> is a JDK 1.2 abstract class, so this
 * class actually provides an implementation of *our* instantiation of
 * <code>java.io.FileSystem</code>, which happens to be
 * <code>harpoon.Interpret.Quads.InterpretedFileSystem</code>, a completely
 * synthetic class.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: INFileSystem.java,v 1.1.2.2 2000-01-27 11:44:31 cananian Exp $
 */
public class INFileSystem {
    static final void register(StaticState ss) {
	try { // JDK 1.2 only
	    ss.register(getFileSystem(ss));
	    ss.register(constructor(ss));
	    ss.register(getBooleanAttributes(ss));
	    ss.register(getPathSeparator(ss));
	    ss.register(getSeparator(ss));
	    ss.register(isAbsolute(ss));
	    ss.register(normalize(ss));
	} catch (NoSuchMethodError e) { // ignore
	} catch (NoSuchClassException e) { // ignore
	}
    }

    // return the synthetic FileSystem class. Make it if we haven't already.
    static final HClass getInterpretedFileSystem(StaticState ss) {
	final String IFSNAME = "harpoon.Interpret.Quads.InterpretedFileSystem";
	try {
	    return ss.linker.forName(IFSNAME);
	} catch (NoSuchClassException e) {
	    // okay, we've got to build it ourselves.
	    HClass fs = ss.linker.forName("java.io.FileSystem");
	    HClass ifs = ss.linker.createMutableClass(IFSNAME, fs);
	    HClassMutator cm = ifs.getMutator();
	    // make class non-abstract, with the proper superclass
	    cm.setSuperclass(fs);
	    cm.removeModifiers(Modifier.ABSTRACT);
	    // make all methods native & non-abstract
	    HMethod[] hm = ifs.getDeclaredMethods();
	    for (int i=0; i < hm.length; i++) {
		hm[i].getMutator().removeModifiers(Modifier.ABSTRACT);
		hm[i].getMutator().addModifiers(Modifier.NATIVE);
	    }
	    // remove inherited fields
	    HField[] hf = ifs.getDeclaredFields();
	    for (int i=0; i < hf.length; i++)
		cm.removeDeclaredField(hf[i]);
	    // make a field to help implement this as a singleton
	    HField f = cm.addDeclaredField("singleton", fs);
	    f.getMutator().setModifiers(Modifier.PUBLIC | Modifier.STATIC);
	    // done!
	    return ifs;
	}
    }
    // convenience method: make a non-interpreted File object from an
    // interpreted File object.
    private static final java.io.File resolveFile(StaticState ss,
						  ObjectRef fileobj) {
	// check safety
	if (fileobj==null) {
	    ObjectRef ex_obj = ss.makeThrowable(ss.HCnullpointerE);
	    throw new InterpretedThrowable(ex_obj, ss);
	}
	// get the string corresponding to the file path.
	String path = ss.ref2str
	    ((ObjectRef)Method.invoke
	     (ss, ss.HCfile.getMethod("getPath",new HClass[0]),
	      new Object[] { fileobj } ));
	// now play with it.
	return new java.io.File(path);
    }

    // get the native FileSystem object
    private static final NativeMethod getFileSystem(StaticState ss0) {
	final HMethod hm =
	    ss0.linker.forName("java.io.FileSystem")
	    .getMethod("getFileSystem", new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		HClass hc = getInterpretedFileSystem(ss);
		HField hf = hc.getDeclaredField("singleton");
		ObjectRef obj = (ObjectRef) ss.get(hf);
		if (obj!=null) return obj; // return already-created singleton
		// else, make singleton object.
		obj = new ObjectRef(ss, hc);
		Method.invoke(ss, hc.getConstructor(new HClass[0]),
			      new Object[] { obj } );
		// stash it away in field.
		ss.update(hf, obj);
		// return it
		return obj;
	    }
	};
    }
    // constructor for the synthetic interpreted file system class.
    private static final NativeMethod constructor(StaticState ss0) {
	final HMethod hm =
	    getInterpretedFileSystem(ss0).getConstructor(new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef _this = (ObjectRef) params[0];
		// invoke superclass constructor.  that's all.
		HMethod scM = ss.linker.forName("java.io.FileSystem")
		    .getConstructor(new HClass[0]);
		Method.invoke(ss, scM, new Object[] { _this } );
		return null; // done.
	    }
	};
    }
    // getBooleanAttributes -- return the simple boolean attributes for
    // the file or directory denoted by the given abstract pathname.
    private static final NativeMethod getBooleanAttributes(StaticState ss0) {
	final HClass ifs = getInterpretedFileSystem(ss0);
	final HMethod hm =
	    ifs.getMethod("getBooleanAttributes", new HClass[] { ss0.HCfile });
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef _this = (ObjectRef) params[0];
		ObjectRef _file = (ObjectRef) params[1];
		// resolve interpreted file object into native File
		java.io.File f = resolveFile(ss, _file);
		// get the attribute values
		int BA_EXISTS =
		    ((Integer)ss.get(ifs.getField("BA_EXISTS"))).intValue();
		int BA_REGULAR =
		    ((Integer)ss.get(ifs.getField("BA_REGULAR"))).intValue();
		int BA_DIRECTORY =
		    ((Integer)ss.get(ifs.getField("BA_DIRECTORY"))).intValue();
		int BA_HIDDEN =
		    ((Integer)ss.get(ifs.getField("BA_HIDDEN"))).intValue();
		// okay, compute return value.
		int retval = 0;
		if (f.exists()) retval |= BA_EXISTS;
		if (f.isFile()) retval |= BA_REGULAR;
		if (f.isDirectory()) retval |= BA_DIRECTORY;
		// have to use reflection for the 'hidden' attribute,
		// since File.isHidden isn't present before JDK 1.2
		try {
		    Boolean hidden = (Boolean) 
			(f.getClass().getMethod("isHidden", new Class[0])
			 .invoke(f, new Object[0]));
		    if (hidden.booleanValue()) retval |= BA_HIDDEN;
		} catch (NoSuchMethodException e) { // ignore
		} catch (Exception e) { // this shouldn't happen.
		    throw new RuntimeException(e.toString());
		}
		return new Integer(retval);
	    }
	};
    }
    // Return the local filesystem's name-separator character.
    private static final NativeMethod getSeparator(StaticState ss0) {
	final HClass ifs = getInterpretedFileSystem(ss0);
	final HMethod hm = ifs.getMethod("getSeparator", new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		return new Character(java.io.File.separatorChar);
	    }
	};
    }
    // Return the local filesystem's path-separator character.
    private static final NativeMethod getPathSeparator(StaticState ss0) {
	final HClass ifs = getInterpretedFileSystem(ss0);
	final HMethod hm = ifs.getMethod("getPathSeparator", new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		return new Character(java.io.File.pathSeparatorChar);
	    }
	};
    }
    //
    private static final NativeMethod isAbsolute(StaticState ss0) {
	final HClass ifs = getInterpretedFileSystem(ss0);
	final HMethod hm = ifs.getMethod("isAbsolute",
					 new HClass[] { ss0.HCfile } );
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef _this = (ObjectRef) params[0];
		ObjectRef _file = (ObjectRef) params[1];
		// resolve interpreted file object into native File
		java.io.File f = resolveFile(ss, _file);
		return new Boolean(f.isAbsolute());
	    }
	};
    }
    // Convert the given pathname string to normal form.
    private static final NativeMethod normalize(StaticState ss0) {
	final HClass ifs = getInterpretedFileSystem(ss0);
	final HMethod hm = ifs.getMethod("normalize",
					 new HClass[] { ss0.HCstring } );
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef _this = (ObjectRef) params[0];
		ObjectRef _path = (ObjectRef) params[1];
		String path = ss.ref2str(_path);
		String normal = new java.io.File(path).getPath();
		return path.equals(normal) ? _path : ss.makeString(normal);
	    }
	};
    }
}

