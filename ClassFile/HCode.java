package harpoon.ClassFile;

/**
 * <code>HCode</code> is an abstract class that all views of a particular
 * method's executable code should extend.
 * <p>
 * An <code>HCode</code> corresponds roughly to a "list of instructions".
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HCode.java,v 1.5 1998-09-13 23:57:14 cananian Exp $
 * @see HMethod
 * @see HCodeElement
 * @see harpoon.ClassFile.Bytecode.Code
 * @see harpoon.ClassFile.Bytecode.Instr
 */
public abstract class HCode {
  /**
   * Return the <code>HMethod</code> to which this <code>HCode</code>
   * belongs.
   */
  public abstract HMethod getMethod();

  /**
   * Return the name of this code view.
   * The default bytecode view is named <code>"bytecode"</code>.
   * It is suggested that additional views be named in a similarly
   * human-friendly fashion.
   */
  public abstract String getName();

  /**
   * Return an ordered list of the component objects making up this
   * code view.  If there is a 'root' to the code view, it should
   * occupy index 0 of the <code>HCodeElement</code> array.
   * @see harpoon.ClassFile.Bytecode.Instr
   */
  public abstract HCodeElement[] getElements();

  /**
   * Return the 'root' element of this code view.
   * @return root of the code view, or <code>null</code> if this notion
   *         is not applicable.
   */
  public HCodeElement getRootElement() { return getElements()[0]; }

  /**
   * Pretty-print this code view.
   */
  public void print(java.io.PrintWriter pw) {
    pw.println("Codeview \""+getName()+"\" for "+getMethod()+":");
    HCodeElement[] hce = getElements();
    for (int i=0; i<hce.length; i++)
      pw.println("  #"+hce[i].getID()+"/"+
		 hce[i].getSourceFile()+":"+hce[i].getLineNumber()+" - " +
		 hce[i].toString());
  }
}
