package harpoon.IR.Tree;

import harpoon.Analysis.Maps.TypeMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Properties.Derivation;
import harpoon.IR.Properties.Derivation.DList;
import harpoon.Temp.Temp;
import harpoon.Util.Tuple;
import harpoon.Util.Util;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * The <code>ToCanonicalTree</code> class translates tree code to 
 * canonical tree code (no ESEQ).  
 * 
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: ToCanonicalTree.java,v 1.1.2.1 1999-03-29 05:08:40 duncan Exp $
 */
public class ToCanonicalTree implements Derivation, TypeMap {
    private Tree m_tree;
    private Derivation m_derivation;
    private TypeMap m_typeMap;

    /** Class constructor. */
    public ToCanonicalTree(final TreeFactory tf, TreeCode code) { 
    	final Hashtable dT = new Hashtable();

	m_tree = translate(tf, code, dT);
	m_derivation = new Derivation() {
	    public DList derivation(HCodeElement hce, Temp t) {
		if ((hce==null)||(t==null)) return null;
		else {
		    Object deriv = dT.get(new Tuple(new Object[] { hce, t }));
		    if (deriv instanceof Error)
			throw (Error)((Error)deriv).fillInStackTrace();
		    else
			return (DList)deriv;
		}
	    }
	};
	m_typeMap = new TypeMap() {
	    public HClass typeMap(HCode hc, Temp t) {
		Util.assert(t.tempFactory()==tf.tempFactory());
		if (t==null) return null;
		else {
		    Object type = dT.get(t);   // Ignores hc parameter
		    if (type instanceof Error) 
			throw (Error)((Error)type).fillInStackTrace();
		    else                       
			return (HClass)type;
		}
	    }
	};
    }
    
    /** Returns the updated derivation information for the 
     *  specified <code>Temp</code>.  The <code>HCodeElement</code>
     *  parameter must be a <code>Tree</code> object in which the 
     *  <code>Temp</code> is found.
     */
    public DList derivation(HCodeElement hce, Temp t) {
	return m_derivation.derivation(hce, t);
    }

    /** Returns the root of the generated tree code */
    public Tree getTree() {
	return m_tree;
    }

    /** Returns the updated type information for the specified
     *  <code>Temp</code>.  The <code>HCode</code> paramter is
     *  ignored. */
    public HClass typeMap(HCode hc, Temp t) {
	// Ignores HCode parameter
	return m_typeMap.typeMap(hc, t);
    }

    // translate to canonical form
    private Tree translate(TreeFactory tf, TreeCode code, Hashtable dT) {
	TreeMap tm = new TreeMap();
	CanonicalizingVisitor cv = new CanonicalizingVisitor(tf, tm, code, dT);
	((Stm)code.getRootElement()).visit(cv);
	return tm.get((Stm)code.getRootElement());
    }

    // Visitor class to translate to canonical form
    class CanonicalizingVisitor extends TreeVisitor {
	private Derivation  derivation;
	private TreeCode    code;
	private StmExpList  nopNull;	
	private TreeFactory tf; 
	private TreeMap     treeMap;
	private TypeMap     typeMap;
	private Hashtable   dT;

	public CanonicalizingVisitor(TreeFactory tf, TreeMap tm, 
				     TreeCode code, Hashtable dT) {
	    this.code       = code;
	    this.derivation = code;
	    this.treeMap    = tm;
	    this.dT         = dT;
	    this.tf         = tf;
	    this.typeMap    = code;
	    
	    this.nopNull = new StmExpList
		(new EXP
		 (tf, code.getRootElement(), 
		  new CONST(tf, code.getRootElement(), 0)),
		 null);
	}

	public void visit(Tree t) { 
	    throw new Error("No defaults here");
	}

	public void visit(MOVE s) {
	    Tree t = treeMap.get(s);
	    s.src.visit(this);

	    if (s.dst instanceof ESEQ) {
		ESEQ eseq = (ESEQ)s.dst;
		eseq.exp.visit(this);
		eseq.stm.visit(this);
		SEQ tmp = new SEQ
		  (s.getFactory(), s, 
		   s, 
		   new MOVE
		   (s.getFactory(), s, 
		    treeMap.get(eseq.exp), 
		    treeMap.get(s.src)));
		tmp.visit(this);
		treeMap.map(s, tmp);
	    }
	    else {
		treeMap.map(s, reorderStm(s));
	    }
	}
      
	public void visit(SEQ s) {
	    s.left.visit(this);
	    s.right.visit(this);
	    treeMap.map(s, seq(treeMap.get(s.left), treeMap.get(s.right)));
	}

	public void visit(Stm s) { 
	    treeMap.map(s, reorderStm(s));
	}

	public void visit(Exp e) { 
	    treeMap.map(e, reorderExp(e));
	}

	public void visit(ESEQ e) { 
	    e.stm.visit(this);
	    e.exp.visit(this);
	    treeMap.map(e, new ESEQ(e.getFactory(), e, 
			    seq(treeMap.get(e.stm), ((ESEQ)treeMap.get(e.exp)).stm),
			    ((ESEQ)treeMap.get(e.exp)).exp));
	}

	/* public void visit(TEMP e) { 
	   updateDT(e, (TEMP)treeMap.get(e));
	   }*/

	private Stm reorderStm(Stm s) {
	    StmExpList x = reorder(s.kids());
	    return seq(x.stm, s.build(x.exps));
	}
	
	private ESEQ reorderExp (Exp e) {
	    StmExpList x = reorder(e.kids());
	    return new ESEQ(tf, e, x.stm, e.build(x.exps));
	}
	
        private StmExpList reorder(ExpList exps) {
	    if (exps==null) return nopNull;
	    else {
		Exp a = exps.head;
		a.visit(this);
		ESEQ aa = (ESEQ)treeMap.get(a);
		StmExpList bb = reorder(exps.tail);
		if (commute(bb.stm, aa.exp))
		    return new StmExpList(seq(aa.stm,bb.stm), 
					  new ExpList(aa.exp,bb.exps));
		else { // FIX: must update DT for new Temp
		    Temp t = new Temp(tf.tempFactory());
		    return new StmExpList
			(seq
			 (aa.stm, 
			  seq
			  (new MOVE
			   (tf, aa, 
			    new TEMP(tf, aa, Type.POINTER, t),
			    aa.exp),
			   bb.stm)),
			 new ExpList(new TEMP(tf, aa, Type.POINTER, t), 
				     bb.exps));
		}
	    }
	}
	
	
	protected void updateDT(TEMP tOld, TEMP tNew) {
	    if (this.derivation.derivation(tOld, tOld.temp) != null) {
		dT.put(new Tuple(new Object[] { tNew, tNew.temp }),
		       DList.clone(this.derivation.derivation(tOld, tOld.temp)));
		dT.put(tNew.temp,new Error("*** Derived pointers have no type"));
	    }
	    else {
		if (this.typeMap.typeMap(this.code, tOld.temp) != null) {
		    dT.put(tNew.temp, this.typeMap.typeMap(this.code, tOld.temp));
		}
	    }
	}
    }

        
    /********************************************************************
     *                                                                  *
     *                      Utility Methods                             *
     *                                                                  *
     *******************************************************************/

    static boolean commute(Stm a, Exp b) {
	return isNop(a) || (b instanceof NAME) || (b instanceof CONST);
    }

    static boolean isNop(Stm s) {
	return (s instanceof EXP) && (((EXP)s).exp instanceof CONST);
    }

    static Stm seq(Stm a, Stm b) {
	if (isNop(a)) return b;
	else if (isNop(b)) return a;
	else return new SEQ(a.getFactory(), a, a, b);
    }
}


class StmExpList {
    Stm stm;
    ExpList exps;
    StmExpList(Stm s, ExpList e) {stm=s; exps=e;}
}

class TreeMap {
    private Hashtable h = new Hashtable();   
    void map(Tree t1, Tree t2) { h.put(t1, t2); }
    Exp get(Exp e) { return (Exp)h.get(e); }
    Stm get(Stm s) { return (Stm)h.get(s); }
}







