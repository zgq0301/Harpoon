// InstrBuilder.java, created Fri Sep 10 23:37:52 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.MIPS;

import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrMEM;
import harpoon.IR.Assem.InstrLABEL;
import harpoon.Temp.Temp;
import harpoon.Temp.Label;
import harpoon.Util.Util;
import harpoon.Util.ArrayIterator;

import java.util.List;
import java.util.Iterator;
import java.util.Arrays;

/** <code>StrongARM.InstrBuilder</code> is an <code>Generic.InstrBuilder</code> for the
    StrongARM architecture.

    @author  Felix S. Klock II <pnkfelix@mit.edu>
    @version $Id: InstrBuilder.java,v 1.1.2.1 2000-06-26 18:37:13 witchel Exp $
 */
public class InstrBuilder extends harpoon.Backend.Generic.InstrBuilder {

    private static final int OFFSET_LIMIT = 1023;

    RegFileInfo rfInfo;

    /* helper macro. */
    private final Temp SP() { 
	return rfInfo.SP;
    }
    
    InstrBuilder(RegFileInfo rfInfo) {
	super();
	this.rfInfo = rfInfo;
    }

    // TODO: override makeStore/Load(List, int, Instr) to take
    // advantage of StrongARM's multi-register memory operations.   

    public int getSize(Temp t) {
	if (t instanceof TwoWordTemp) {
	    return 2; 
	} else {
	    return 1;
	}
    }

   public List makeLoad(Temp r, int offset, Instr template) {
      String[] strs = getLdrAssemStrs(r, offset);
      Util.assert(strs.length == 1 ||
                  strs.length == 2 );

      if (strs.length == 2) {
         InstrMEM load1 = 
            new InstrMEM(template.getFactory(), template,
                         strs[0],
                         new Temp[]{ r },
                         new Temp[]{ SP()  });
         InstrMEM load2 = 
            new InstrMEM(template.getFactory(), template,
                         strs[1],
                         new Temp[]{ r },
                         new Temp[]{ SP()  });
         load2.layout(load1, null);
         return Arrays.asList(new InstrMEM[] { load1, load2 });
      } else {
         InstrMEM load = 
            new InstrMEM(template.getFactory(), template,
                         strs[0],
                         new Temp[]{ r },
                         new Temp[]{ SP()  });
         return Arrays.asList(new InstrMEM[] { load });
      }
   }

    private String[] getLdrAssemStrs(Temp r, int offset) {
       if (r instanceof TwoWordTemp) {
          return new String[] {
             "lw `d0h, ", + (4*offset) + "(`s0)" ,
             "lw `d0l, ", + (4*(offset+1)) + "(`s0)" };
       } else {
          return new String[] { "lw `d0, " + (4*offset) + "(`s0)" };
       }
    }

    private String[] getStrAssemStrs(Temp r, int offset) {
       if (r instanceof TwoWordTemp) {
          return new String[] {
             "sw `s0h, " + (4*offset) + "(`s1)", 
             "sw `s0l, " + (4*(offset+1)) + "(`s1)" };
       } else {
          return new String[] { "sw `s0, " + (4*offset) + "(`s1)" };
       }
    }

    public List makeStore(Temp r, int offset, Instr template) {
       String[] strs = getStrAssemStrs(r, offset);
       Util.assert(strs.length == 1 || 
                   strs.length == 2);
	    
       if (strs.length == 2) {
          System.out.println("In makeStore, twoWord case");

          InstrMEM store1 = 
             new InstrMEM(template.getFactory(), template,
				 strs[0],
                          new Temp[]{ },
                          new Temp[]{ r , SP() });
          InstrMEM store2 = 
             new InstrMEM(template.getFactory(), template,
                          strs[1],
                          new Temp[]{ },
                          new Temp[]{ r , SP() });
          store2.layout(store1, null);
          Util.assert(store1.getNext() == store2, "store1.next == store2");
          Util.assert(store2.getPrev() == store1, "store2.prev == store1");
          return Arrays.asList(new InstrMEM[]{ store1, store2 });
       } else {

          InstrMEM store = 
             new InstrMEM(template.getFactory(), template,
                          strs[0],
                          new Temp[]{ },
                          new Temp[]{ r , SP() });
          return Arrays.asList(new InstrMEM[] { store });
       }
    }

    public InstrLABEL makeLabel(Instr template) {
	Label l = new Label();
	InstrLABEL il = new InstrLABEL(template.getFactory(), 
				       template,
				       l.toString() + ":", l);
	return il;
    }

    /** Returns a new <code>InstrLABEL</code> for generating new
	arbitrary code blocks to branch to.
	@param template An <code>Instr</code> to base the generated
	                <code>InstrLABEL</code>.
			<code>template</code> should be part of the
			instruction stream that the returned
			<code>InstrLABEL</code> is intended for. 
    */
    public InstrLABEL makeLabel(Label l, Instr template) {
	InstrLABEL il = new InstrLABEL(template.getFactory(), 
				       template,
				       l.toString() + ":", l);
	return il;
    }
}
