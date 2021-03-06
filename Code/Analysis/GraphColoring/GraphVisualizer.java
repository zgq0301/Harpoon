// GraphVisualizer.java, created Tue Apr 10 23:19:52 2001 by rhlee
// Copyright (C) 2001 Robert Lee <rhlee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.GraphColoring;


/**
 * 
 * static methods for generating dot visualizations for 
 * the Graph abstraction
 *
 * @author  Robert Lee <rhlee@mit.edu>
 * @version $Id: GraphVisualizer.java,v 1.2 2002-02-25 20:57:17 cananian Exp $
 */

import java.io.PrintWriter;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;



public class GraphVisualizer
{



    private static void writeHeader(PrintWriter pw)
    {
	writeln(pw, "digraph G {");
	writeln(pw, "node[shape=box]");
    }
    

    private static void writeFooter(PrintWriter pw)
    {
	writeln(pw, "}");
    }
    
    public static void getDotInput(PrintWriter pw, Graph graph)
    {
	Collection edges;
	Collection pair;
	Iterator iter;
	Iterator innerIter;
	Object obj1, obj2;
	
	
	writeHeader(pw);

	edges = graph.edges();

	iter = edges.iterator();

	while(iter.hasNext())
	{
	    innerIter = ((Collection)(iter.next())).iterator();

	    obj1 = innerIter.next();
	    obj2 = innerIter.next();

	    write(pw, obj1 +  " -> " + obj2 + ";");
	    
	}

	writeFooter(pw);

    }
    

    private static void writeln(PrintWriter pw, String str)
    {
	write(pw, str + "\n");
    }
    
    private static void write(PrintWriter pw, String str)
    {
	pw.write(str);
	pw.flush();
    }
    
}
