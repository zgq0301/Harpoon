// IIR_FileInterfaceDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_FileInterfaceDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_FileInterfaceDeclaration.java,v 1.3 1998-10-11 01:24:57 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_FileInterfaceDeclaration extends IIR_InterfaceDeclaration
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_FILE_INTERFACE_DECLARATION).
     * @return <code>IR_Kind.IR_FILE_INTERFACE_DECLARATION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_FILE_INTERFACE_DECLARATION; }
    //CONSTRUCTOR:
    public IIR_FileInterfaceDeclaration() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

