// IIR_EnumerationTypeDefinition.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_EnumerationTypeDefinition</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_EnumerationTypeDefinition.java,v 1.3 1998-10-11 01:24:56 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_EnumerationTypeDefinition extends IIR_ScalarTypeDefinition
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_ENUMERATION_TYPE_DEFINITION).
     * @return <code>IR_Kind.IR_ENUMERATION_TYPE_DEFINITION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_ENUMERATION_TYPE_DEFINITION; }
    //CONSTRUCTOR:
    public IIR_EnumerationTypeDefinition() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

