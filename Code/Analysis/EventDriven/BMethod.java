// BMethod.java, created Sat Mar 18 22:06:48 2000 by bdemsky
// Copyright (C) 2001 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.EventDriven;

import harpoon.ClassFile.HMethod;

/**
 * BMethod.java
 *
 *
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: BMethod.java,v 1.2 2002-02-25 20:57:01 cananian Exp $
 */
public interface BMethod {
    public HMethod swop (final HMethod m);
    public HMethod[] blockingMethods();
}

