// IIR_VariableInterfaceDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_VariableInterfaceDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_VariableInterfaceDeclaration.java,v 1.3 1998-10-11 01:25:05 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_VariableInterfaceDeclaration extends IIR_InterfaceDeclaration
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_VARIABLE_INTERFACE_DECLARATION).
     * @return <code>IR_Kind.IR_VARIABLE_INTERFACE_DECLARATION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_VARIABLE_INTERFACE_DECLARATION; }
    //CONSTRUCTOR:
    public IIR_VariableInterfaceDeclaration() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

