// ToAsync.java, created Thu Nov 11 12:41:56 1999 by kkz
// Copyright (C) 1999 Karen K. Zee <kkzee@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.EventDriven;

import harpoon.Analysis.AllCallers;
import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.Quads.QuadLiveness;
import harpoon.Analysis.ContBuilder.ContBuilder;
import harpoon.Analysis.EnvBuilder.EnvBuilder;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Loader;
import harpoon.ClassFile.Linker;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadNoSSA;
import harpoon.Temp.Temp;
import harpoon.Util.HClassUtil;
import harpoon.Util.Util;
import harpoon.Util.WorkSet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 * <code>ToAsync</code>
 * 
 * @author Karen K. Zee <kkzee@alum.mit.edu>
 * @version $Id: ToAsync.java,v 1.1.2.13 2000-01-27 06:28:38 bdemsky Exp $
 */
public class ToAsync {
    protected final CachingCodeFactory ucf;
    protected final HCode hc;
    protected final ClassHierarchy ch;
    protected final Linker linker;

    /** Creates a <code>ToAsync</code>. */
    public ToAsync(CachingCodeFactory ucf, HCode hc, ClassHierarchy ch, Linker linker) {
	this.linker=linker;
        this.ucf = ucf;
	this.hc = hc;
	this.ch = ch;
    }
    
    public HMethod transform() {
	System.out.println("Entering ToAsync.transform()");
	AllCallers ac = new AllCallers(this.ch, this.ucf);
	BlockingMethods bm = new BlockingMethods(linker);
	Set blockingcalls = ac.getCallers(bm);
	
	HashMap old2new=new HashMap();
	HMethod nhm=AsyncCode.makeAsync(old2new, hc.getMethod(),
					ucf,linker);

	WorkSet async_todo=new WorkSet();
	async_todo.push(hc);

	//Add run methods in...make them blocking
//  	HClass hcthrd=linker.forName("java.lang.Runnable");
//  	Set children=ch.children(hcthrd);
//  	Iterator childit=children.iterator();

//  	while(childit.hasNext()) {
//  	    try {
//  		HClass child=(HClass)childit.next();

//  		if (!child.isInterface()) {
//  		    HMethod hmrun=child.getDeclaredMethod("run",
//  						  new HClass[0]);
//  		    if (old2new.containsKey(hmrun))
//  			continue;
//  		    async_todo.add(ucf.convert(hmrun));
//  		    HMethod temp=AsyncCode.makeAsync(old2new, hmrun,
//  						     ucf,linker);
//  		}
//  	    } catch (NoSuchMethodError e) {
//  	    }
//  	}

	WorkSet other=new WorkSet();
	WorkSet done_other=new WorkSet();
	   
	
	
	while (!async_todo.isEmpty()|!other.isEmpty()) {
	    HCode selone=null;
	    boolean status=false;
	    if (async_todo.isEmpty()) {
		HMethod hm=(HMethod) other.pop();
		done_other.push(hm);
		selone=ucf.convert(hm);
		if (selone==null)
		    continue;
		status=true;
	    } else
		selone=(HCode) async_todo.pop();
	    final QuadLiveness ql = new QuadLiveness(selone);
	    System.out.println("ToAsync is running AsyncCode on "+selone);
	    AsyncCode.buildCode(selone, old2new, async_todo,
				ql,blockingcalls,ucf,  bm, hc.getMethod(), 
				linker,ch,other, done_other,status);
	}
	return nhm;
    }

    static class BlockingMethods implements AllCallers.MethodSet {
	final Linker linker;
	public BlockingMethods(Linker linker) {
	    this.linker=linker;
	}

	/** Returns true if the <code>HMethod</code> blocks, false
	 *  otherwise. Checks against a list of known blocking methods.
	 */
	public boolean select (final HMethod m) {
	    if (swop(m) != null) {
		System.out.println(m.toString());
		return true;
	    } else
		return false;
	}

	/** Returns the corresponding asynchronous method for a given
	 *  blocking method if one exists.
	 */
	final private Map cache = new HashMap();
	public HMethod swop (final HMethod m) {
	    final HClass is = linker.forName("java.io.InputStream");
	    final HClass ss = linker.forName("java.net.ServerSocket");
	    final HClass b = HClass.Byte;
	    final HClass HCthrd = linker.forName("java.lang.Thread");

	    HMethod retval = (HMethod)cache.get(m);
	    if (retval == null) {
		if (m.equals(HCthrd.getMethod("join",
						new HClass[0]))) {
			retval=HCthrd.getMethod("join_Async",
					    new HClass[0]);

		    cache.put(m, retval);
		} else if (is.equals(m.getDeclaringClass())) {
		    if (m.getName().equals("read")) {
			final HMethod bm1 = 
			    is.getDeclaredMethod("read", new HClass[0]);
			final HMethod bm2 = is.getDeclaredMethod("read", 
                            new HClass[] {HClassUtil.arrayClass(b, 1)});
			final HMethod bm3 = is.getDeclaredMethod("read", 
			    new HClass[] {HClassUtil.arrayClass(b, 1),
					  HClass.Int, HClass.Int});
			if (bm1.equals(m)) {
			    retval = is.getMethod("readAsync", 
						  new HClass[0]);
			    cache.put(m, retval);
			} else if (bm2.equals(m)) {
			    retval = is.getMethod("readAsync", 
			        new HClass[] {HClassUtil.arrayClass(b, 1)});
			    cache.put(m, retval);
			} else if (bm3.equals(m)) {
			    retval = is.getMethod("readAsync", 
				new HClass[] {HClassUtil.arrayClass(b, 1),
					      HClass.Int, HClass.Int});
			    cache.put(m, retval);
			}
		    }
		} else if (ss.equals(m.getDeclaringClass())) {
		    final HMethod bm4 = 
			ss.getDeclaredMethod("accept", new HClass[0]);
		    if (bm4.equals(m)) {
			retval = ss.getDeclaredMethod("acceptAsync", 
						      new HClass[0]);
			cache.put(m, retval);
		    }
		}
	    }
	    return retval;
	} // swop

    } // BlockingMethods

} // ToAsync


