// Derivation.java, created Fri Jan 22 16:46:17 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Maps;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;

/**
 * <code>Derivation</code> provides a means to access the derivation
 * of a particular derived pointer.  Given a compiler temporary, it
 * will enumerate the base pointers and signs needed to allow proper
 * garbage collection of the derived pointer.<p>
 * See Diwan, Moss, and Hudson, <A
 * HREF="http://www.acm.org/pubs/citations/proceedings/pldi/143095/p273-diwan/"
 * >"Compiler Support for Garbage Collection in a Statically Typed
 * Language"</A> in PLDI'92 for background on the derivation structure
 * and its motivations.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Derivation.java,v 1.1.2.2 2000-02-01 07:43:35 cananian Exp $
 */
public interface Derivation extends harpoon.Analysis.Maps.TypeMap {

    /** Map compiler temporaries to their derivations.
     * @param t The temporary to query.
     * @param hce A definition point for <code>t</code>.
     * @return <code>null</code> if the temporary has no derivation (is
     *         a base pointer, for example), or the derivation otherwise.
     * @exception NullPointerException if <code>t</code> or <code>hc</code>
     *            is <code>null</code>.
     * @exception TypeNotKnownException if the <code>Derivation</code>
     *            has no information about <code>t</code> as defined
     *            at <code>hce</code>.
     */
    public DList derivation(HCodeElement hce, Temp t)
	throws TypeNotKnownException;

    /** Structure of the derivation information.<p>
     *  Given three <code>Temp</code>s <code>t1</code>, <code>t2</code>,
     *  and <code>t3</code>, a derived pointer <code>d</code> whose value
     *  can be described by the equation:<pre>
     *   d = K + t1 - t2 + t3
     *  </pre> for some integer K at every point during runtime can be
     *  represented as the <code>DList</code><pre>
     *   new DList(t1, true, new DList(t2, false, new DList(t3, true)))
     *  </pre>.<p>
     * <b>NOTE</b> that the temporaries named in the <code>DList</code>
     * refer to the <i>reaching definitions</i> of those temporaries at
     * the <i>definition point</i> of the variable with the derived
     * type.  In SSI/SSA forms, this does not matter, as every variable
     * has exactly one reaching definition, but in other forms <b>it
     * is the responsibility of the implementor</b> to ensure that the
     * base pointers are not overwritten while the derived value is
     * live.
     */
    public static class DList {
	/** Base pointer. */
	public final Temp base;
	/** Sign of base pointer.  <code>true</code> if derived pointer
	 *  equals offset <b>+</b> base pointer, <code>false</code> if
	 *  derived pointer equals offset <b>-</b> base pointer. */
	public final boolean sign;
	/** Pointer to a continuation of the derivation, or <Code>null</code>
	 *  if there are no more base pointers associated with this derived
	 *  pointer. */
	public final DList next;
	/** Constructor. */
	public DList(Temp base, boolean sign, DList next) {
	    this.base = base; this.sign = sign; this.next = next;
	}
      
      /* Like <code>Quad.clone()</code>, does not clone
       * <code>Temp</code>s when not supplied with a
       * <code>TempMap</code>. */
      public static DList clone(DList dlist) {
	if (dlist==null) return null;
	else return new DList(dlist.base, dlist.sign, clone(dlist.next));
      }

      /** Returns a clone of this <code>DList</code> */
      public static DList clone(DList dlist, CloningTempMap ctm) {
	if (dlist==null) return null;
	else
	  return new DList(((dlist.base==null)?null:ctm.tempMap(dlist.base)),
			   dlist.sign, 
			   clone(dlist.next, ctm));
      }

      /** Returns a new <code>DList</code> with the <code>Temp</code>s 
       *  renamed by the supplied mapping */
      public static DList rename(DList dlist, TempMap tempMap)
	{
	  if (dlist==null) return null;
	  else return new DList
		 (((dlist.base==null)?null:tempMap.tempMap(dlist.base)),
		  dlist.sign,
		  rename(dlist.next, tempMap));
	}
    }
}
