// IIR_GroupTemplateDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_GroupTemplateDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_GroupTemplateDeclaration.java,v 1.4 1998-10-11 01:24:57 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_GroupTemplateDeclaration extends IIR_Declaration
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_GROUP_TEMPLATE_DECLARATION).
     * @return <code>IR_Kind.IR_GROUP_TEMPLATE_DECLARATION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_GROUP_TEMPLATE_DECLARATION; }
    //CONSTRUCTOR:
    public IIR_GroupTemplateDeclaration() { }
    //METHODS:  
    //MEMBERS:  
    public IIR_EntityClassEntryList entity_class_entry_list;

// PROTECTED:
} // END class

