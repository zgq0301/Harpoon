// InstrBuilder.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Sparc;

import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrLABEL;
import harpoon.Temp.Temp;

import java.util.List;

/**
 * <code>Sparc.InstrBuilder</code> is another implementation of
 * <code>Generic.InstrBuilder</code> - for the Sparc architecture.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: InstrBuilder.java,v 1.1.2.2 1999-11-02 22:09:01 andyb Exp $
 */
public class InstrBuilder extends harpoon.Backend.Generic.InstrBuilder {
    private final RegFileInfo regFileInfo;
    private final TempBuilder tempBuilder;

    InstrBuilder(RegFileInfo regFileInfo, TempBuilder tempBuilder) {
        super();
        this.regFileInfo = regFileInfo;
        this.tempBuilder = tempBuilder;
    }

    /* AAA - todo */
    public List makeLoad(Temp r, int offset, Instr template) {
        return null;
    }

    /* AAA - todo */
    public List makeStore(Temp r, int offset, Instr template) {
        return null;
    }    

    /* AAA - todo */
    public InstrLABEL makeLabel(Instr template) {
        return null;
    }
}
