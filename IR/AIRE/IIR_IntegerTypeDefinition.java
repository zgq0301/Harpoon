// IIR_IntegerTypeDefinition.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_IntegerTypeDefinition</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_IntegerTypeDefinition.java,v 1.3 1998-10-11 01:24:58 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_IntegerTypeDefinition extends IIR_ScalarTypeDefinition
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_INTEGER_TYPE_DEFINITION).
     * @return <code>IR_Kind.IR_INTEGER_TYPE_DEFINITION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_INTEGER_TYPE_DEFINITION; }
    //CONSTRUCTOR:
    public IIR_IntegerTypeDefinition() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

