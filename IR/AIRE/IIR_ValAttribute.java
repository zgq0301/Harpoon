// IIR_ValAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ValAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ValAttribute.java,v 1.1 1998-10-10 07:53:46 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ValAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_VAL_ATTRIBUTE
    //CONSTRUCTOR:
    public IIR_ValAttribute() { }

    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

