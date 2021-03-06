package org.jacorb.util;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose.
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

import java.util.*;

/**
 * @author Gerald Brose, FU Berlin
 * @version $Id: ObjectUtil.java,v 1.1 2003-04-03 17:01:43 wbeebee Exp $
 */

public  class ObjectUtil
{
    /**
     * @returns the contents of the ressource as a string
     */

    public static final String readURL(String url) 
    {
	try
	{
	    java.net.URL u = new java.net.URL(url);
	    String line  = null;
	    java.io.BufferedReader in;	    
	    
	    in = new java.io.BufferedReader(new java.io.InputStreamReader(u.openStream()) ); 
	    line = in.readLine();
	    
	    while (line.indexOf("IOR:") != 0) 
		line = in.readLine();
	    
	    in.close();
	    return line;
	}
	catch ( Exception e )
	{ 
            org.jacorb.util.Debug.output(2, "Could not read from URL " + url );
	    org.jacorb.util.Debug.output(3,e);	    
	}
	return null;
    }

}






