// IllegalEdgeException.java, created Thu Jan 14 15:34:50 1999 by pnkfelix
package harpoon.Analysis.GraphColoring;

/**
 * <code>IllegalEdgeException</code>
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: IllegalEdgeException.java,v 1.1.2.2 1999-01-19 16:07:59 pnkfelix Exp $
 */

public class IllegalEdgeException extends Exception {
    
    /** Creates a <code>IllegalEdgeException</code>. */
    public IllegalEdgeException() {
        super();
    }

    /** Creates a <code>IllegalEdgeException</code>. */
    public IllegalEdgeException(String s) {
        super(s);
    }
    
}
