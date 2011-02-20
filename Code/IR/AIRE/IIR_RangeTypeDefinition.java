// IIR_RangeTypeDefinition.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_RangeTypeDefinition</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_RangeTypeDefinition.java,v 1.4 1998-10-11 02:37:22 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_RangeTypeDefinition extends IIR_ScalarTypeDefinition
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_RANGE_TYPE_DEFINITION).
     * @return <code>IR_Kind.IR_RANGE_TYPE_DEFINITION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_RANGE_TYPE_DEFINITION; }
    //CONSTRUCTOR:
    public IIR_RangeTypeDefinition() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

