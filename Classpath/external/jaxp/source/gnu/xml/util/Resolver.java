/*
 * $Id: Resolver.java,v 1.1 2003-05-06 16:55:06 wbeebee Exp $
 * Copyright (C) 1999-2001 David Brownell
 * 
 * This file is part of GNU JAXP, a library.
 *
 * GNU JAXP is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * GNU JAXP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * As a special exception, if you link this library with other files to
 * produce an executable, this library does not by itself cause the
 * resulting executable to be covered by the GNU General Public License.
 * This exception does not however invalidate any other reasons why the
 * executable file might be covered by the GNU General Public License. 
 */

package gnu.xml.util;

import java.io.*;

import java.util.Dictionary;
import java.util.Hashtable;

import org.xml.sax.*;


// $Id: Resolver.java,v 1.1 2003-05-06 16:55:06 wbeebee Exp $

/**
 * Utility implementation of a SAX resolver, which can be used to improve
 * network utilization of SAX based XML components.  It does this by
 * supporting local caches of external entities.
 * SAX parsers <em>should</em> use such local caches when possible.
 *
 * @see XCat
 *
 * @version $Date: 2003-05-06 16:55:06 $
 */
public class Resolver implements EntityResolver, Cloneable
{
    /**
     * Updates a dictionary used to map PUBLIC identifiers to file names,
     * so that it uses the mappings in a specified directory.
     *
     * @param mappings Array of string pairs, where the first member
     *	of each pair is a PUBLIC identifier and the second is the
     *	name of a file, relative to the specified directory.
     * @param directory File holding the specified files.
     */
    public static void addDirectoryMapping (
	Dictionary	table,
	String		mappings [][],
	File		directory
    ) throws IOException
    {
	for (int i = 0; i < mappings.length; i++) {
	    File	file = new File (directory, mappings [i][1]);
	    String	temp;

	    if (!file.exists ())	// ?? log a warning ??
		continue;

	    temp = fileToURL (file);
	    table.put (mappings [i][0], temp);
	}
    }

	// FIXME: these *URL routines don't quite belong here, except
	// that they're all in the same spirit of making it easy to
	// use local filesystem URIs with XML parsers.

    /**
     * Provides the URL for a named file, without relying on the JDK 1.2
     * {@link java.io.File#toURL File.toURL}() utility method.
     *
     * @param filename the file name to convert.  Relative file names
     *	are resolved the way the JVM resolves them (current to the
     *	process-global current working directory).
     *
     * @exception IOException if the file does not exist
     */
    public static String fileNameToURL (String filename)
    throws IOException
    {
	return fileToURL (new File (filename));
    }

    /**
     * Provides the URL for a file, without relying on the JDK 1.2
     * {@link java.io.File#toURL File.toURL}() utility method.
     *
     * @param f the file to convert.  Relative file names
     *	are resolved the way the JVM resolves them (current to the
     *	process-global current working directory).
     *
     * @exception IOException if the file does not exist
     */
    public static String fileToURL (File f)
    throws IOException
    {
	String	temp;

	// NOTE:  the javax.xml.parsers.DocumentBuilder and
	// javax.xml.transform.stream.StreamSource versions
	// of this don't have this test.  Some JVM versions
	// don't report this error sanely through URL code. 
	if (!f.exists ())
	    throw new IOException ("no such file: " + f.getName ());

	    // FIXME: getAbsolutePath() seems buggy; I'm seeing components
	    // like "/foo/../" which are clearly not "absolute"
	    // and should have been resolved with the filesystem.

	    // Substituting "/" would be wrong, "foo" may have been
	    // symlinked ... the URL code will make that change
	    // later, so that things can get _really_ broken!

	temp = f.getAbsolutePath ();

	if (File.separatorChar != '/')
	    temp = temp.replace (File.separatorChar, '/');
	if (!temp.startsWith ("/"))
	    temp = "/" + temp;
	if (!temp.endsWith ("/") && f.isDirectory ())
	    temp = temp + "/";
	return "file:" + temp;
    }


    /**
     * Returns a URL string.  Note that if a malformed URL is provided, or
     * the parameter names a nonexistent file, the resulting URL may be
     * malformed.
     *
     * @param fileOrURL If this is the name of a file which exists,
     *	then its URL is returned.  Otherwise the argument is returned.
     */
    public static String getURL (String fileOrURL)
    {
	try {
	    return fileNameToURL (fileOrURL);
	} catch (Exception e) {
	    return fileOrURL;
	}
    }



    // note:  cloneable, this is just copied; unguarded against mods
    private Dictionary		pubidMapping;

    /**
     * Constructs a resolver which understands how to map PUBLIC identifiers
     * to other URIs, typically for local copies of standard DTD components.
     * 
     * @param dictionary maps PUBLIC identifiers to URIs.  This is not
     *	copied; subsequent modifications will be reported through the
     *	resolution operations.
     */
    public Resolver (Dictionary dict)
	{ pubidMapping = dict; }

    
    // FIXME: want notion of a "system default" resolver, presumably
    // loaded with all sorts of useful stuff.  At the same time need
    // a notion of resolver chaining (failure --> next) so that subsystems
    // can set up things that won't interfere with other ones.

    /**
     * This parses most MIME content type strings that have <em>charset=...</em>
     * encoding declarations to and returns the specified encoding.  This
     * conforms to RFC 3023, and is useful when constructing InputSource
     * objects from URLConnection objects or other objects using MIME
     * content typing.
     *
     * @param contentType the MIME content type that will be parsed; must
     *	not be null.
     * @return the appropriate encoding, or null if the content type is
     *	not text and there's no <code>charset=...</code> attribute
     */
    static public String getEncoding (String contentType)
    {
	// currently a dumb parsing algorithm that works "mostly" and handles
	//	..anything...charset=ABC
	//	..anything...charset=ABC;otherAttr=DEF
	//	..anything...charset=ABC (comment);otherAttr=DEF
	//	..anything...charset= "ABC" (comment);otherAttr=DEF

	int	temp;
	String	encoding;
	String	defValue = null;

	if (contentType.startsWith ("text/"))
	    defValue = contentType.startsWith ("text/html")
		    ? "ISO-8859-1" : "US-ASCII";

	// Assumes 'charset' is only an attribute name, not part
	// of a value, comment, or other attribute name
	// ALSO assumes no escaped values like "\;" or "\)"
	if ((temp = contentType.indexOf ("charset")) != -1) {
	    // strip out everything up to '=' ...
	    temp = contentType.indexOf ('=', temp);
	    if (temp == -1)
		return defValue;
	    encoding = contentType.substring (temp + 1);
	    // ... and any subsequent attributes
	    if ((temp = encoding.indexOf (';')) != -1)
		encoding = encoding.substring (0, temp);
	    // ... and any comments after value
	    if ((temp = encoding.indexOf ('(')) != -1)
		encoding = encoding.substring (0, temp);
	    // ... then whitespace, and any (double) quotes
	    encoding = encoding.trim ();
	    if (encoding.charAt (0) == '"')
		encoding = encoding.substring (1, encoding.length () - 1);
	} else
	    encoding = defValue;
	return encoding;
    }


    /**
     * Uses a local dictionary of public identifiers to resolve URIs,
     * normally with the goal of minimizing network traffic or latencies.
     */
    public InputSource resolveEntity (String publicId, String systemId)
    throws IOException, SAXException
    {
	InputSource	retval = null;
	String		uri;

	if (publicId != null
		&& ((uri = (String) pubidMapping.get (publicId)) != null)) {
	    retval = new InputSource (uri);
	    retval.setPublicId (publicId);
	}

	// Could do URN resolution here

	// URL resolution always done by parser

	// FIXME: chain to "next" resolver

	return retval;
    }
}
