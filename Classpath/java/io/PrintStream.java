/* PrintStream.java -- OutputStream for printing output
   Copyright (C) 1998,2003 Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.
 
GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */


package java.io;

/* Written using "Java Class Libraries", 2nd edition, ISBN 0-201-31002-3
 * "The Java Language Specification", ISBN 0-201-63451-1
 * Status:  Believed complete and correct to 1.3
 */

/**
 * This class prints Java primitive values and object to a stream as
 * text.  None of the methods in this class throw an exception.  However,
 * errors can be detected by calling the <code>checkError()</code> method.
 * Additionally, this stream can be designated as "autoflush" when 
 * created so that any writes are automatically flushed to the underlying
 * output sink when the current line is terminated.
 * <p>
 * <b>Note that this class is deprecated</b>.  It exists for backward  
 * compatibility only.  New code should be written to use 
 * <code>PrintWriter</code> instead.  
 * <p>
 * This class converts char's into byte's using the system default encoding.
 *
 * @deprecated
 *
 * @author Aaron M. Renn <arenn@urbanophile.com>
 * @author Tom Tromey <tromey@cygnus.com>
 */
public class PrintStream extends FilterOutputStream
{

  /*
   * Ok, why is this class deprecated?  It could easily have been extended
   * to support character encodings.  In fact, PrintWriter is basically a
   * superset of this except for the write() methods.  So let's do something
   * tricky here and just redirect calls in this class to a hidden PrintWriter
   * instance.  All the functionality goes there since that is the 'real'
   * class.  The big win of doing this way is that the default character 
   * encoding is done automagicially by the PrintWriter tree!
   */

  /**
   * This boolean indicates whether or not an error has ever occured
   * on this stream.
   */
  private boolean error_occurred;

  /**
   * This is <code>true</code> if auto-flush is enabled, 
   * <code>false</code> otherwise
   */
  private boolean auto_flush;

  /**
   * The PrintWriter instance this object writes to
   */
  private PrintWriter pw;

  /**
   * Lets us know if the stream is closed
   */
  private boolean closed;

  /**
   * This method intializes a new <code>PrintStream</code> object to write
   * to the specified output sink.  Note that this class is deprecated in
   * favor of <code>PrintWriter</code>.
   *
   * @param out The <code>OutputStream</code> to write to.
   *
   * @deprecated
   */
  public PrintStream (OutputStream out)
  {
    this(out, false);
  }

  /**
   * This method intializes a new <code>PrintStream</code> object to write
   * to the specified output sink.  This constructor also allows "auto-flush"
   * functionality to be specified where the stream will be flushed after
   * every line is terminated or newline character is written.
   * <p>
   * Note that this class is deprecated in favor of <code>PrintWriter</code>.
   *
   * @param out The <code>OutputStream</code> to write to.
   * @param auto_flush <code>true</code> to flush the stream after every 
   * line, <code>false</code> otherwise
   *
   * @deprecated
   */
  public PrintStream (OutputStream out, boolean auto_flush)
  {
    super(out);

    pw = new PrintWriter(out, auto_flush);
    this.auto_flush = auto_flush;
  }

  /**
   * This method intializes a new <code>PrintStream</code> object to write
   * to the specified output sink.  This constructor also allows "auto-flush"
   * functionality to be specified where the stream will be flushed after
   * every line is terminated or newline character is written.
   * <p>
   * Note that this class is deprecated in favor of <code>PrintWriter</code>.
   *
   * @param out The <code>OutputStream</code> to write to.
   * @param autoFlush <code>true</code> to flush the stream after every 
   * line, <code>false</code> otherwise
   * @param encoding The name of the character encoding to use for this
   * object.
   *
   * @deprecated
   */
  public PrintStream (OutputStream out, boolean autoFlush, String encoding)
    throws UnsupportedEncodingException
  {
    super (out);

    pw = new PrintWriter (new OutputStreamWriter (out, encoding), autoFlush);
    this.auto_flush = autoFlush;
  }

  /**
   * This method checks to see if an error has occurred on this stream.  Note
   * that once an error has occurred, this method will continue to report
   * <code>true</code> forever for this stream.  Before checking for an
   * error condition, this method flushes the stream.
   *
   * @return <code>true</code> if an error has occurred, 
   * <code>false</code> otherwise
   */
  public boolean checkError ()
  {
    if (!closed)
      pw.flush ();

    if (pw.checkError () | error_occurred)
      return true;
    else
      return false;
  }

  /**
   * This method can be called by subclasses to indicate that an error
   * has occurred and should be reported by <code>checkError</code>.
   */
  protected void setError ()
  {
    error_occurred = true;
  }

  /**
   * This method closes this stream and all underlying streams.
   */
  public synchronized void close ()
  {
    pw.close ();
    closed = true;
  }

  /**
   * This method flushes any buffered bytes to the underlying stream and
   * then flushes that stream as well.
   */
  public void flush ()
  {
    pw.flush();
  }

  /**
   * This methods prints a boolean value to the stream.  <code>true</code>
   * values are printed as "true" and <code>false</code> values are printed
   * as "false".
   *
   * @param b The <code>boolean</code> value to print
   */
  public void print (boolean bool)
  {
    pw.print (bool);
  }

  /**
   * This method prints an integer to the stream.  The value printed is
   * determined using the <code>String.valueOf()</code> method.
   *
   * @param inum The <code>int</code> value to be printed
   */
  public void print (int inum)
  {
    pw.print (inum);
  }

  /**
   * This method prints a long to the stream.  The value printed is
   * determined using the <code>String.valueOf()</code> method.
   *
   * @param lnum The <code>long</code> value to be printed
   */
  public void print (long lnum)
  {
    pw.print (lnum);
  }

  /**
   * This method prints a float to the stream.  The value printed is
   * determined using the <code>String.valueOf()</code> method.
   *
   * @param fnum The <code>float</code> value to be printed
   */
  public void print (float fnum)
  {
    pw.print (fnum);
  }

  /**
   * This method prints a double to the stream.  The value printed is
   * determined using the <code>String.valueOf()</code> method.
   *
   * @param dnum The <code>double</code> value to be printed
   */
  public void print (double dnum)
  {
    pw.print (dnum);
  }

  /**
   * This method prints an <code>Object</code> to the stream.  The actual
   * value printed is determined by calling the <code>String.valueOf()</code>
   * method.
   *
   * @param obj The <code>Object</code> to print.
   */
  public void print (Object obj)
  {
    // Don't call pw directly.  Convert to String so we scan for newline
    // characters on auto-flush;
    print(String.valueOf(obj));
  }

  /**
   * This method prints a <code>String</code> to the stream.  The actual
   * value printed depends on the system default encoding.
   *
   * @param str The <code>String</code> to print.
   */
  public void print (String str)
  {
    pw.print (str);

    if (auto_flush)
      if ((str.indexOf('\r') != -1) || (str.indexOf('\n') != -1))
        flush();
  }

  /**
   * This method prints a char to the stream.  The actual value printed is
   * determined by the character encoding in use.
   *
   * @param ch The <code>char</code> value to be printed
   */
  public void print (char ch)
  {
    pw.print (ch);

    if (auto_flush)
      if ((ch == '\r')
          || (ch == '\n'))
        flush ();    
  }

  /**
   * This method prints an array of characters to the stream.  The actual
   * value printed depends on the system default encoding.
   *
   * @param s The array of characters to print.
   */
  public void print (char[] charArray)
  {
    pw.print(charArray);

    if (auto_flush)
      for (int i = 0; i < charArray.length; i++)
        if ((charArray[i] == '\r') || (charArray[i] == '\n'))
          {
            flush();
            break;
          }
  }

  /**
   * This method prints a line separator sequence to the stream.  The value
   * printed is determined by the system property <xmp>line.separator</xmp>
   * and is not necessarily the Unix '\n' newline character.
   */
  public void println ()
  {
    pw.println ();
  }

  /**
   * This methods prints a boolean value to the stream.  <code>true</code>
   * values are printed as "true" and <code>false</code> values are printed
   * as "false".
   * <p>
   * This method prints a line termination sequence after printing the value.
   *
   * @param b The <code>boolean</code> value to print
   */
  public void println (boolean bool)
  {
    pw.println (bool);
  }

  /**
   * This method prints an integer to the stream.  The value printed is
   * determined using the <code>String.valueOf()</code> method.
   * <p>
   * This method prints a line termination sequence after printing the value.
   *
   * @param inum The <code>int</code> value to be printed
   */
  public void println (int inum)
  {
    pw.println (inum);
  }

  /**
   * This method prints a long to the stream.  The value printed is
   * determined using the <code>String.valueOf()</code> method.
   * <p>
   * This method prints a line termination sequence after printing the value.
   *
   * @param lnum The <code>long</code> value to be printed
   */
  public void println (long lnum)
  {
    pw.println (lnum);
  }

  /**
   * This method prints a float to the stream.  The value printed is
   * determined using the <code>String.valueOf()</code> method.
   * <p>
   * This method prints a line termination sequence after printing the value.
   *
   * @param fnum The <code>float</code> value to be printed
   */
  public void println (float fnum)
  {
    pw.println (fnum);
  }

  /**
   * This method prints a double to the stream.  The value printed is
   * determined using the <code>String.valueOf()</code> method.
   * <p>
   * This method prints a line termination sequence after printing the value.
   *
   * @param dnum The <code>double</code> value to be printed
   */
  public void println (double dnum)
  {
    pw.println (dnum);
  }

  /**
   * This method prints an <code>Object</code> to the stream.  The actual
   * value printed is determined by calling the <code>String.valueOf()</code>
   * method.
   * <p>
   * This method prints a line termination sequence after printing the value.
   *
   * @param obj The <code>Object</code> to print.
   */
  public void println (Object obj)
  {
    pw.println (obj);
  }

  /**
   * This method prints a <code>String</code> to the stream.  The actual
   * value printed depends on the system default encoding.
   * <p>
   * This method prints a line termination sequence after printing the value.
   *
   * @param str The <code>String</code> to print.
   */
  public void println (String str)
  {
    pw.println (str);
  }

  /**
   * This method prints a char to the stream.  The actual value printed is
   * determined by the character encoding in use.
   * <p>
   * This method prints a line termination sequence after printing the value.
   *
   * @param ch The <code>char</code> value to be printed
   */
  public void println (char ch)
  {
    pw.println (ch);
  }

  /**
   * This method prints an array of characters to the stream.  The actual
   * value printed depends on the system default encoding.
   * <p>
   * This method prints a line termination sequence after printing the value.
   *
   * @param s The array of characters to print.
   */
  public void println (char[] charArray)
  {
    pw.println (charArray);
  }

  /**
   * This method writes a byte of data to the stream.  If auto-flush is
   * enabled, printing a newline character will cause the stream to be
   * flushed after the character is written.
   * 
   * @param b The byte to be written
   */
  public synchronized void write (int oneByte)
  {
    // Sigh, we actually have to implement this method. Flush first so that
    // things get written in the right order.
    flush ();

    try
      {
        out.write (oneByte);

        if (auto_flush)
          if ((oneByte == '\n') || (oneByte == '\n'))
            flush ();
      }
    catch (IOException e)
      {
        error_occurred = true;
      }
  }

  /**
   * This method writes <code>len</code> bytes from the specified array
   * starting at index <code>offset</code> into the array.
   *
   * @param buffer The array of bytes to write
   * @param offset The index into the array to start writing from
   * @param len The number of bytes to write
   */
  public synchronized void write (byte[] buffer, int offset, int len)
  {
    // We actually have to implement this method too. Flush first so that
    // things get written in the right order.
    flush();

    try
      {
        out.write(buffer, offset, len);

        if (auto_flush)
          for (int i = offset; i < len; i++)
            if ((buffer[i] == '\r') || (buffer[i] == '\n'))
              {
                flush();
                break;
              }
      }
    catch (IOException e)
      {
        error_occurred = true;
      }
  }

} // class PrintStream

