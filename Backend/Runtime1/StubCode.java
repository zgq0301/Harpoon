// StubCode.java, created Sat Oct 23 15:33:33 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Runtime1;

import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Maps.NameMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Properties.Derivation;
import harpoon.IR.Tree.Bop;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.ExpList;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.Tree;
import harpoon.IR.Tree.Type;
import harpoon.IR.Tree.BINOP;
import harpoon.IR.Tree.CJUMP;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.METHOD;
import harpoon.IR.Tree.MOVE;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.NATIVECALL;
import harpoon.IR.Tree.RETURN;
import harpoon.IR.Tree.SEGMENT;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.THROW;
import harpoon.Temp.Label;
import harpoon.Temp.Temp;

import java.util.ArrayList;
import java.util.List;
/**
 * <code>StubCode</code> is used to generate non-canonical tree code
 * stubs for native methods.  That is, a <code>StubCode</code> is a
 * thunk from runtime-native methods to the JNI interface which
 * C-code "native methods" conform to.<p>
 * <code>StubCode</code> will generate a thunk for any old method you
 * give to its constructor.  It is the job of the
 * <code>nativeTreeCodeFactory()</code> in <code>Runtime1.Runtime</code>
 * to weed out native from non-native methods, give the proper ones
 * to <code>StubCode</code>, and do the right thing with the stubs that
 * <code>StubCode</code> makes.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: StubCode.java,v 1.1.2.1 1999-10-25 22:18:09 cananian Exp $
 */
public class StubCode extends harpoon.IR.Tree.TreeCode {
    final TreeBuilder m_tb;
    final NameMap m_nm;
    final int EXC_OFFSET;
    final int REF_OFFSET;

    /** Creates a <code>StubCode</code>. */
    public StubCode(HMethod method, Frame frame) {
        super(method, null, frame);
	this.m_nm = frame.getRuntime().nameMap;
	this.m_tb = (TreeBuilder) frame.getRuntime().treeBuilder;
	this.EXC_OFFSET = 1 * m_tb.POINTER_SIZE;
	this.REF_OFFSET = 3 * m_tb.POINTER_SIZE;
	this.tree = buildStub(method);
    }
    // first, some generic cruft to complete the Code implementation.
    public Derivation.DList derivation(HCodeElement hce, Temp t) {
	throw new Error("derivation() is not implemented.");
    }
    public HClass typeMap(HCodeElement hce, Temp t) {
	throw new Error("typeMap() is not implemented.");
    }

    // down here is the real stub builder code.
    Tree buildStub(HMethod method) {
	List stmlist = new ArrayList();
	// segment change
	stmlist.add(new SEGMENT(tf, null, SEGMENT.CODE));
	// add method header.
	// (remember that first method parameter in tree form is the
	//  'return exception address')
	HClass[] paramTypes = method.getParameterTypes();
	Temp[] paramTemps = new Temp[paramTypes.length+1];
	TEMP[] paramTEMPs = new TEMP[paramTypes.length+1];
	paramTemps[0] = new Temp(tf.tempFactory(), "rexaddr");
	paramTEMPs[0] = new TEMP(tf, null, Type.POINTER, paramTemps[0]);
	for (int i=0; i<paramTypes.length; i++) {
	    paramTemps[i+1] = new Temp(tf.tempFactory(), "param"+i);
	    paramTEMPs[i+1] = new TEMP(tf, null, class2type(paramTypes[i]),
				       paramTemps[i+1]);
	}
	stmlist.add(new METHOD(tf, null, paramTEMPs));
	// get JNIEnv *
	// this is a cheesy one-thread implementation at the moment;
	// it should eventually call pthread_getspecific() to use thread-local
	// memory.
	Temp envT = new Temp(tf.tempFactory(), "env");
	stmlist.add(new MOVE(tf, null,
			     new TEMP(tf, null, Type.POINTER, envT),
			     new MEM(tf, null, Type.POINTER,
				     new NAME(tf, null,
					      new Label("_FNI_JNIEnv")))));
	// reset the exception field in the thread state.
	stmlist.add(new MOVE(tf, null,
			     new MEM(tf, null, Type.POINTER,
				     new BINOP(tf, null, Type.POINTER, Bop.ADD,
					       new TEMP(tf, null, Type.POINTER,
							envT),
					       new CONST(tf, null, EXC_OFFSET)
					       )),
			     new CONST(tf, null))); // set to null.
	// save the top of the local ref stack (so we can restore it later)
	Temp refT = new Temp(tf.tempFactory(), "lref");
	stmlist.add(new MOVE(tf, null,
			     new TEMP(tf, null, Type.POINTER, refT),
			     new MEM(tf, null, Type.POINTER,
				     new BINOP(tf, null, Type.POINTER, Bop.ADD,
					       new TEMP(tf, null, Type.POINTER,
							envT),
					       new CONST(tf, null, REF_OFFSET)
					       ))));
	// wrap objects in parameter list
	for (int i=0; i<paramTypes.length; i++)
	    if (!paramTypes[i].isPrimitive())
		stmlist.add(new NATIVECALL
			    (tf, null,
			     new TEMP
			     (tf, null, Type.POINTER, paramTemps[i+1]),
			     new NAME
			     (tf, null, new Label("_FNI_NewLocalRef")),
			      new ExpList
			      (new TEMP(tf, null, Type.POINTER, envT),
			       new ExpList
			       (new TEMP(tf,null,Type.POINTER,paramTemps[i+1]),
				null))));
	// wrap a class object pointer for static methods.
	Temp classT = null;
	if (method.isStatic()) {
	    classT = new Temp(tf.tempFactory(), "jclass");
	    stmlist.add(new NATIVECALL
			(tf, null,
			 new TEMP
			 (tf, null, Type.POINTER, classT),
			 new NAME
			 (tf, null, new Label("_FNI_NewLocalRef")),
			 new ExpList
			 (new TEMP(tf, null, Type.POINTER, envT),
			  new ExpList
			  (new NAME(tf, null,
				    m_nm.label(method.getDeclaringClass(),
					       "classobj")),
			   null))));
	}
	// make the native call's parameters, in reverse order
	ExpList jniParams = null;
	for (int i=paramTypes.length-1; i>=0; i--)
	    jniParams = new ExpList
		(new TEMP(tf, null,
			  class2type(paramTypes[i]), paramTemps[i+1]),
		 jniParams);
	// second parameter is either a jclass for a static method
	if (classT != null)
	    jniParams = new ExpList(new TEMP(tf, null, Type.POINTER, classT),
				    jniParams);
	// first parameter is the JNIEnv *
	jniParams = new ExpList(new TEMP(tf, null, Type.POINTER, envT),
				jniParams);
	// Do the call.
	Temp retT = null;
	if (method.getReturnType()!=HClass.Void)
	    retT = new Temp(tf.tempFactory(), "retval");
	stmlist.add(new NATIVECALL(tf, null,
				   (retT==null) ? null :
				   new TEMP(tf, null,
					    class2type(method.getReturnType()),
					    retT),
				   new NAME(tf, null,
					    new Label(jniMangle(method))),
				   jniParams));
	// now clean up afterward: check for exceptions, etc.
	Temp excT = new Temp(tf.tempFactory(), "excval");
	stmlist.add(new MOVE(tf, null,
			     new TEMP(tf, null, Type.POINTER, excT),
			     new MEM(tf, null, Type.POINTER,
				     new BINOP(tf, null, Type.POINTER, Bop.ADD,
					       new TEMP(tf, null, Type.POINTER,
							envT),
					       new CONST(tf, null, EXC_OFFSET)
					       ))));
	Label no_exceptions = new Label();
	Label yes_exceptions = new Label();
	stmlist.add(new CJUMP(tf, null,
			      new BINOP(tf, null, Type.POINTER, Bop.CMPEQ,
					new TEMP(tf, null, Type.POINTER, excT),
					new CONST(tf, null)/*null pointer*/),
			      no_exceptions, yes_exceptions));
	// no exceptions occurred.  unwrap return value and return from stub.
	stmlist.add(new LABEL(tf, null, no_exceptions, false));
	Exp retexp;
	if (retT==null)
	    retexp = new CONST(tf, null);
	else {
	    int ty = class2type(method.getReturnType());
	    if (!method.getReturnType().isPrimitive())
		stmlist.add(new NATIVECALL(tf, null,
					   new TEMP(tf, null, ty, retT),
					   new NAME(tf, null,
						    new Label("_FNI_Unwrap")),
					   new ExpList
					   (new TEMP(tf, null, ty, retT), null)
					   ));
	    retexp = new TEMP(tf, null, ty, retT);
	}
	emitFreeLocals(stmlist, envT, refT);
	stmlist.add(new RETURN(tf, null, retexp));
	// an exception occurred.  unwrap exception value and throw it.
	stmlist.add(new LABEL(tf, null, yes_exceptions, false));
	stmlist.add(new NATIVECALL(tf, null,
				   new TEMP(tf, null, Type.POINTER, excT),
				   new NAME(tf, null,
					    new Label("_FNI_Unwrap")),
				   new ExpList
				   (new TEMP(tf, null, Type.POINTER, excT),
				    null)
				   ));
	emitFreeLocals(stmlist, envT, refT);
	stmlist.add(new THROW(tf, null,
			      new TEMP(tf, null, Type.POINTER, excT),
			      new TEMP(tf, null, Type.POINTER, paramTemps[0])
			      ));
	return Stm.toStm(stmlist);
    }
    private void emitFreeLocals(List stmlist, Temp envT, Temp refT) {
    	stmlist.add(new NATIVECALL
		    (tf, null, null, // free local references
		     new NAME(tf, null,
			      new Label("_FNI_DeleteLocalRefsUpTo")),
		     new ExpList(new TEMP(tf, null, Type.POINTER, refT),
				 null)));
    }

    private static int class2type(HClass hc) {
	if (!hc.isPrimitive()) return Type.POINTER;
	if (hc==HClass.Boolean || hc==HClass.Byte || hc==HClass.Char ||
	    hc==HClass.Int || hc==HClass.Short) return Type.INT;
	if (hc==HClass.Double) return Type.DOUBLE;
	if (hc==HClass.Float) return Type.FLOAT;
	if (hc==HClass.Long) return Type.LONG;
	throw new Error("Unknown primitive type: "+hc);
    }

    private static String jniMangle(HMethod m) {
	//XXX implement me!
	return "dude";
    }
}
