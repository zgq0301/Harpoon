// FixupFunc.java, created Tue Sep 15 15:10:05 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Analysis.UseDef;
import harpoon.Analysis.DomTree;
import harpoon.Analysis.DomFrontier;
import harpoon.Analysis.Place;
import harpoon.Temp.Temp;
import harpoon.Temp.TempList;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;
/**
 * <code>FixupFunc</code> places appropriate Phi and Lambda functions
 * in the Quads.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: FixupFunc.java,v 1.7 1998-09-16 15:57:53 cananian Exp $
 * @see Translate
 */

public class FixupFunc  {
    static void fixup(Code c) {
	DomTree  dt = new DomTree(false);// dominator tree
	DomTree pdt = new DomTree(true); // post-dominator tree
	place(c, dt, pdt);
	rename(c, dt);
    }
    private static void place(Code c, DomTree dt, DomTree pdt) {
	UseDef ud = new UseDef();
	Place Pphi = new Place(ud, new DomFrontier(dt));
	Place Plam = new Place(ud, new DomFrontier(pdt));
	
	Quad[] ql = (Quad[]) c.getElements();
	for (int i=0; i< ql.length; i++) {
	    Temp[] neededPhi = Pphi.neededFunc(c, ql[i]);
	    Temp[] neededLam = Plam.neededFunc(c, ql[i]);
	    // algorithm wants to place phis on FOOTER.
	    if (neededPhi.length > 0 && (!(ql[i] instanceof FOOTER))) {
		// place phi functions.
		PHI q = (PHI) ql[i]; // better be a phi!
		q.dst = neededPhi;
		q.src = new Temp[q.dst.length][q.prev.length];
		for (int j=0; j < q.src.length; j++)
		    for (int k=0; k < q.src[j].length; k++)
			q.src[j][k] = q.dst[j];
	    }
	    if (neededLam.length > 0) {
		// place lambda functions.
		LAMBDA q = (LAMBDA) ql[i]; // better be a lambda!
		q.src = neededLam;
		q.dst = new Temp[q.src.length][q.next.length];
		for (int j=0; j < q.dst.length; j++)
		    for (int k=0; k < q.dst[j].length; k++)
			q.dst[j][k] = q.src[j];
	    }
	}
    }
    private static void rename(final Code c, final DomTree dt) {
	final FFCount Count = new FFCount();
	final FFStack Stack = new FFStack();
	
	class Util {
	    void search(Quad S) {
		TempList L = null;
		TempList M = null;
		// if S is a lambda function
		if (S instanceof LAMBDA) {
		    LAMBDA Sl = (LAMBDA) S;
		    // let L be the set of lambda function sources.
		    for (int i=0; i < Sl.src.length; i++)
			L = new TempList(Sl.src[i], L);
		}
		
		// if S is not a phi functions
		if ( ! (S instanceof PHI) ) {
		    Temp[] ul = S.use();
		    // for each use of some variable x in S
		    for (int i=0; i < ul.length; i++) { // (includes lambdas)
			Temp x = ul[i];
			Temp xi= Stack.top(x);
			S.renameUses(tempMap(x, xi));
		    }
		}
		// if S is not a lambda function
		if ( ! (S instanceof LAMBDA) ) {
		    Temp[] dl = S.def();
		    // for each definition of some variable a in S
		    for (int i=0; i < dl.length; i++) { // (includes phis)
			Temp a = dl[i];
			int I = Count.inc(a);
			Temp ai = Stack.push(a, I);
			S.renameDefs(tempMap(a, ai));
			M = new TempList(a, M); // keep track of this a.
		    }
		}
		// for each flowgraph successor Y of S
		for (int i=0; i < S.next.length; i++) {
		    Quad Y = (Quad) S.next[i].to();
		    // if S is a lambda function...
		    if (S instanceof LAMBDA) {
			LAMBDA Sl = (LAMBDA) S;
			// let j = WhichSucc(S, Y)
			int j = S.next[i].which_succ();
			// for every lambda function in S
			for (int k=0; k < Sl.src.length; k++) {
			    // suppose a is the jth destination of the lambda
			    Temp a = Sl.dst[k][j];
			    int I = Count.inc(a);
			    // replace the jth dest of the lambda with ai
			    Temp ai = Stack.push(a, I);
			    Sl.dst[k][j] = ai;
			}
		    }
		    // if Y is a phi function...
		    if (Y instanceof PHI) {
			PHI Yp = (PHI) Y;
			// let j = WhichPred(Y, S)
			int j = S.next[i].which_pred();
			// for every phi function in Y
			for (int k=0; k < Yp.dst.length; k++) {
			    // suppose a is the jth operand of the phi
			    Temp a = Yp.src[k][j];
			    Temp ai = Stack.top(a);
			    // replace the jth operand with ai
			    Yp.src[k][j] = ai;
			}
		    }
		    // clean up the stack for lambda functions.
		    for (TempList al = L; al != null; al = al.tail)
			Stack.pop(al.head);
		}
		// for each dominator tree child X of S
		HCodeElement[] Xl = dt.children(c, S);
		for (int i=0; i < Xl.length; i++) {
		    Quad X = (Quad) Xl[i];
		    int j = whichSucc(S, X);
		    // if X is the jth successor of S and S is a lambda func...
		    if (j != -1 && S instanceof LAMBDA) {
			// for every a in L
			int k = ((LAMBDA)S).dst.length;
			for (TempList al = L; al!=null; al=al.tail) {
			    Temp a = al.head;
			    // push the jth dest. of the lambda on Stack[a]
			    Stack.push(a, ((LAMBDA)S).dst[--k][j]);
			}
		    }
	    
		    // recurse
		    search(X);

		    // pop what we added
		    if (j != -1 && S instanceof LAMBDA)
			for (TempList al = L; al != null; al=al.tail)
			    Stack.pop(al.head);
		}
		// for each definition of some variable a in the original S
		for (TempList al = M; al != null; al=al.tail) {
		    // pop Stack[a]
		    Stack.pop(al.head);
		}
		// done.
	    } // end function search.
	} // end class Util

	// INVOKE!!
	new Util().search((Quad)c.getRootElement());
    } // end function rename.

    // Utility classes....
    static class FFCount {
	Hashtable h = new Hashtable();
	int inc(Temp a) { 
	    Integer I = (Integer) h.get(a);
	    int i = 1 + ((I==null)?0:I.intValue());
	    h.put(a, new Integer(i));
	    return i;
	}
    }
    static class FFStackValue {
	Vector v = new Vector();
	Stack  s = new Stack();
	FFStackValue(Temp t) { 
	    // element 0 is the old name of the value (for use-before-def)
	    v.addElement(t); s.push(t); 
	}
    }
    static class FFStack {
	Temp top(Temp t) {
	    return (Temp) get(t).s.peek();
	}
	Temp pop(Temp t) {
	    return (Temp) get(t).s.pop();
	}
	Temp push(Temp t, Temp ti) {
	    return (Temp) get(t).s.push(ti);
	}
	Temp push(Temp t, int i) {
	    return push(t, index(t, i));
	}
	Temp index(Temp t, int i) {
	    FFStackValue sv = get(t);
	    while ( sv.v.size() <= i )
		sv.v.addElement(new Temp(t));
	    return (Temp) sv.v.elementAt(i);
	}
	private Hashtable h = new Hashtable();
	private FFStackValue get(Temp t) {
	    FFStackValue sv = (FFStackValue) h.get(t);
	    if (sv == null) { sv = new FFStackValue(t); h.put(t, sv); }
	    return sv;
	}
    }

    // Utility functions.
    static int whichPred(Quad Q, Quad P) {
	for (int i=0; i < Q.prev.length; i++)
	    if (Q.prev(i) == P)
		return i;
	return -1; // no predecessor found.
    }
    static int whichSucc(Quad Q, Quad S) {
	for (int i=0; i < Q.next.length; i++)
	    if (Q.next(i) == S)
		return i;
	return -1; // no predecessor found.
    }

    static TempMap tempMap(final Temp Told, final Temp Tnew) {
	return new TempMap() {
	    public Temp tempMap(Temp t) {
		if (t == Told) return Tnew;
		return t;
	    }
	};
    }
}
