// IIR_ArchitectureDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_ArchitectureDeclaration</code> class represents
 * one of potentially several implementations of an entity.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ArchitectureDeclaration.java,v 1.1 1998-10-10 07:53:32 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ArchitectureDeclaration extends IIR_LibraryUnit
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_ARCHITECTURE_DECLARATION

    /** The constructor method initializes an architecture declaration using
     *  an unspecified source location, an unspecified architecture
     *  declarator, and unspecified entry name, no architecture declarations,
     *  no architecture statements, and no attributes. */
    public IIR_ArchitectureDeclaration() { }

    //METHODS:  
    public void set_entity( IIR_EntityDeclaration entity)
    { _entity = entity; }
 
    public IIR_EntityDeclaration get_entity()
    { return _entity; }
 
    //MEMBERS:  
    public IIR_DeclarationList architectecture_declarative_part;
    public IIR_StatementList architecture_statement_part;

// PROTECTED:
    IIR_EntityDeclaration _entity;
} // END class

