// ArrayInfo.java, created Sun Apr  2 18:18:01 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.TypeInference;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HMethod;

import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.AGET;


/**
 * <code>ArrayInfo</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: ArrayInfo.java,v 1.1.2.1 2000-04-03 02:29:15 salcianu Exp $
 */
public class ArrayInfo {
    
    /** Creates a <code>ArrayInfo</code>. */
    public ArrayInfo() {
    }

    /** Returns the set of <code>AGET</code> instructions from hcode
	that access arrays of non primitive objects. */
    public Set getInterestingAGETs(HMethod hm, HCode hcode){

	final Set set = new HashSet();

	final QuadVisitor qvisitor = new QuadVisitor(){
		public void visit(AGET q){
		    set.add(new ExactTemp(q, q.objectref()));
		}
		public void visit(Quad q){
		}
	    };

	for(Iterator it = hcode.getElementsI(); it.hasNext(); )
	    ((Quad) it.next()).accept(qvisitor);
	
	TypeInference ti = new TypeInference(hm, hcode, set);

	Set retval = new HashSet();

	for(Iterator it = set.iterator(); it.hasNext(); ){
	    ExactTemp et = (ExactTemp) it.next();
	    if(ti.isArrayOfNonPrimitives(et))
		retval.add(et.q);
	}

	return retval;
    }
    
}
