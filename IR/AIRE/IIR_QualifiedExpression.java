// IIR_QualifiedExpression.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_QualifiedExpression</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_QualifiedExpression.java,v 1.1 1998-10-10 07:53:40 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_QualifiedExpression extends IIR_Expression
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_QUALIFIED_EXPRESSION
    //CONSTRUCTOR:
    public IIR_QualifiedExpression() { }
    //METHODS:  
    public void set_type_mark(IIR_TypeDefinition type_mark)
    { _type_mark = type_mark; }
 
    public IIR_TypeDefinition get_type_mark()
    { return _type_mark; }
 
    public void set_expression(IIR expression)
    { _expression = expression; }
 
    public IIR get_expression()
    { return _expression; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_TypeDefinition _type_mark;
    IIR _expression;
} // END class

