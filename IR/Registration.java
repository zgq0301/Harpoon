// Registration.java, created Sat Sep 12 18:49:07 1998 by cananian
package harpoon.IR;

/**
 * <code>Registration</code> takes care of registering 'well-known'
 * intermediate representations.  The class that implements the
 * <code>main()</code> method of your program should either subclass
 * <code>Registration</code> or create a new registration object
 * when it initializes to ensure that the IRs are registered.<p>
 * Registration occurs in a <code>static</code> block of the class,
 * so it will happen as soon as the class initializer is called;
 * typically when the <code>Registration</code> class is loaded.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Registration.java,v 1.1 1998-09-13 23:57:16 cananian Exp $
 */

public class Registration  {
    static {
	harpoon.IR.QuadSSA.Code.register();
    }
}
