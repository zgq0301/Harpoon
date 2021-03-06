/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-98  Gerald Brose.
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.jacorb.idl;

/**
 * @author Gerald Brose
 * @version $Id: Operation.java,v 1.1 2003-04-03 16:50:23 wbeebee Exp $
 */

import java.io.*;

interface Operation
    extends Serializable
{
    /** plain name of the operation */

    public String name();

    /*  mangled name in case of attributes (_get_, _set_)*/

    public String opName();

    public String signature();

    public void printSignature(PrintWriter ps);

    /** method code for stubs */

    public void printMethod(PrintWriter ps, String classname, boolean locality_constraint);

    /** method code for skeletons */

    public void printDelegatedMethod(PrintWriter ps);

    public void printInvocation(PrintWriter ps);


} 


















