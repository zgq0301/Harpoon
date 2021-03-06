// UseDefMap.java, created Sun Sep 13 23:11:39 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Maps;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;

/**
 * A <code>UseDefMap</code> is a mapping from temporaries to the
 * <code>HCodeElements</code> that define them.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: UseDefMap.java,v 1.6 2002-09-02 19:23:27 cananian Exp $
 */

public interface UseDefMap<HCE extends HCodeElement>  {
    /**
     * Return an array of <code>HCodeElement</code>s that use 
     * <code>Temp t</code>.
     * @param hc The <code>HCode</code> containing <code>t</code>.
     * @param t  The temporary to examine.
     * @return an array of <code>HCodeElement</code>s where
     *         <code>HCodeElement.use()</code> includes <code>t</code>.
     */
    HCE[] useMap(HCode<HCE> hc, Temp t);
    /**
     * Return an array of <code>HCodeElement</code>s that define 
     * <code>Temp t</code>.
     * @param hc The <code>HCode</code> containing <code>t</code>.
     * @param t  The temporary to examine.
     * @return an array of <code>HCodeElement</code>s where
     *         <code>HCodeElement.def()</code> includes <code>t</code>.
     */
    HCE[] defMap(HCode<HCE> hc, Temp t);
}
