// IIR_BaseAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_BaseAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_BaseAttribute.java,v 1.1 1998-10-10 07:53:32 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_BaseAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_BASE_ATTRIBUTE
    //CONSTRUCTOR:
    public IIR_BaseAttribute() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

