// IIR_PosAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_PosAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_PosAttribute.java,v 1.3 1998-10-11 01:25:00 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_PosAttribute extends IIR_Attribute
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_POS_ATTRIBUTE).
     * @return <code>IR_Kind.IR_POS_ATTRIBUTE</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_POS_ATTRIBUTE; }
    //CONSTRUCTOR:
    public IIR_PosAttribute() { }

    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

