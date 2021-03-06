/*
 * @(#)FileOutputStream.java	1.35 98/09/24
 *
 * Copyright 1994-1998 by Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Sun Microsystems, Inc. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Sun.
 */

package java.io;

import harpoon.Analysis.ContBuilder.*;
/**
 * A file output stream is an output stream for writing data to a 
 * <code>File</code> or to a <code>FileDescriptor</code>. What files 
 * are available or may be created depends on the host environment.
 *
 * @author  Arthur van Hoff
 * @version 1.35, 09/24/98
 * @see     java.io.File
 * @see     java.io.FileDescriptor
 * @see     java.io.FileInputStream
 * @since   JDK1.0
 */
public
class FileOutputStream extends OutputStream
{
    /**
     * The system dependent file descriptor. The value is
     * 1 more than actual file descriptor. This means that
     * the default value 0 indicates that the file is not open.
     */
    private FileDescriptor fd;

    /**
     * Creates an output file stream to write to the file with the 
     * specified name. A new <code>FileDescriptor</code> object is 
     * created to represent this file connection.
     * <p>
     * First, if there is a security manager, its <code>checkWrite</code> 
     * method is called with <code>name</code> as its argument.
     * <p>
     * If the file exists but is a directory rather than a regular file, does
     * not exist but cannot be created, or cannot be opened for any other
     * reason then a <code>FileNotFoundException</code> is thrown.
     *
     * @param      name   the system-dependent filename
     * @exception  FileNotFoundException  if the file exists but is a directory
     *                   rather than a regular file, does not exist but cannot
     *                   be created, or cannot be opened for any other reason
     * @exception  SecurityException  if a security manager exists and its
     *               <code>checkWrite</code> method denies write access
     *               to the file.
     * @see        java.lang.SecurityManager#checkWrite(java.lang.String)
     */
    public FileOutputStream(String name) throws FileNotFoundException {
	this(name, false);
    }

    /**
     * Creates an output file stream to write to the file with the specified
     * <code>name</code>.  If the second argument is <code>true</code>, then
     * bytes will be written to the end of the file rather than the beginning.
     * A new <code>FileDescriptor</code> object is created to represent this
     * file connection.
     * <p>
     * First, if there is a security manager, its <code>checkWrite</code> 
     * method is called with <code>name</code> as its argument.
     * <p>
     * If the file exists but is a directory rather than a regular file, does
     * not exist but cannot be created, or cannot be opened for any other
     * reason then a <code>FileNotFoundException</code> is thrown.
     * 
     * @param     name        the system-dependent file name
     * @param     append      if <code>true</code>, then bytes will be written
     *                   to the end of the file rather than the beginning
     * @exception  FileNotFoundException  if the file exists but is a directory
     *                   rather than a regular file, does not exist but cannot
     *                   be created, or cannot be opened for any other reason.
     * @exception  SecurityException  if a security manager exists and its
     *               <code>checkWrite</code> method denies write access
     *               to the file.
     * @see        java.lang.SecurityManager#checkWrite(java.lang.String)
     * @since     JDK1.1
     */
    public FileOutputStream(String name, boolean append)
        throws FileNotFoundException
    {
	SecurityManager security = System.getSecurityManager();
	if (security != null) {
	    security.checkWrite(name);
	}
	fd = new FileDescriptor();
	if (append) {
	    openAppend(name);
	} else {
	    open(name);
	}
    }

    public void makeAsync()
    {
    	NativeIO.makeNonBlockJNI(fd.fd);
    }


    /**
     * Creates a file output stream to write to the file represented by 
     * the specified <code>File</code> object. A new 
     * <code>FileDescriptor</code> object is created to represent this 
     * file connection.
     * <p>
     * First, if there is a security manager, its <code>checkWrite</code> 
     * method is called with the path represented by the <code>file</code> 
     * argument as its argument.
     * <p>
     * If the file exists but is a directory rather than a regular file, does
     * not exist but cannot be created, or cannot be opened for any other
     * reason then a <code>FileNotFoundException</code> is thrown.
     *
     * @param      file               the file to be opened for writing.
     * @exception  FileNotFoundException  if the file exists but is a directory
     *                   rather than a regular file, does not exist but cannot
     *                   be created, or cannot be opened for any other reason
     * @exception  SecurityException  if a security manager exists and its
     *               <code>checkWrite</code> method denies write access
     *               to the file.
     * @see        java.io.File#getPath()
     * @see        java.lang.SecurityException
     * @see        java.lang.SecurityManager#checkWrite(java.lang.String)
     */
    public FileOutputStream(File file) throws IOException {
	this(file.getPath());
    }

    /**
     * Creates an output file stream to write to the specified file 
     * descriptor, which represents an existing connection to an actual 
     * file in the file system.
     * <p>
     * First, if there is a security manager, its <code>checkWrite</code> 
     * method is called with the file descriptor <code>fdObj</code> 
     * argument as its argument.
     *
     * @param      fdObj   the file descriptor to be opened for writing.
     * @exception  SecurityException  if a security manager exists and its
     *               <code>checkWrite</code> method denies
     *               write access to the file descriptor.
     * @see        java.lang.SecurityManager#checkWrite(java.io.FileDescriptor)
     */
    public FileOutputStream(FileDescriptor fdObj) {
	SecurityManager security = System.getSecurityManager();
	if (fdObj == null) {
	    throw new NullPointerException();
	}
	if (security != null) {
	    security.checkWrite(fdObj);
	}
	fd = fdObj;
    }

    /**
     * Opens a file, with the specified name, for writing.
     * @param name name of file to be opened
     */
    private native void open(String name) throws FileNotFoundException;

    /**
     * Opens a file, with the specified name, for appending.
     * @param name name of file to be opened
     */
    private native void openAppend(String name) throws FileNotFoundException;

    /**
     * Writes the specified byte to this file output stream. Implements 
     * the <code>write</code> method of <code>OutputStream</code>.
     *
     * @param      b   the byte to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public native void write(int b) throws IOException;
    
    public VoidContinuation writeAsyncO(int b) throws IOException
    {
    		int r= NativeIO.putCharJNI(fd.fd, b);
    		switch(r)
    		{
    			case NativeIO.ERROR: throw new IOException();
    			case NativeIO.TRYAGAIN: return new WriteAsyncC(b);
    			default: return new VoidContinuationOpt();
    		}
    }

    class WriteAsyncC extends VoidContinuation implements IOContinuation
    {
	public void exception(Throwable t) {}


	int b;
	
	public WriteAsyncC(int b) { this.b= b; Scheduler.addWrite(this); }
	public void resume()
	{
	    int r= NativeIO.putCharJNI(fd.fd, b);
	    switch(r)
		{
		case NativeIO.ERROR: next.exception( new IOException() ); return;
		case NativeIO.TRYAGAIN:  Scheduler.addWrite(this); return;
		default: next.resume();
		} 
	    
	}
		
	public FileDescriptor getFD() { return fd; }
	
    }
    	

    /**
     * Writes a sub array as a sequence of bytes.
     * @param b the data to be written
     * @param off the start offset in the data
     * @param len the number of bytes that are written
     * @exception IOException If an I/O error has occurred.
     */
    private native void writeBytes(byte b[], int off, int len) throws IOException;

    /**
     * Writes <code>b.length</code> bytes from the specified byte array 
     * to this file output stream. 
     *
     * @param      b   the data.
     * @exception  IOException  if an I/O error occurs.
     */
    public void write(byte b[]) throws IOException {
	writeBytes(b, 0, b.length);
    }
    

    public VoidContinuation writeAsyncO(byte b[]) throws IOException {
    	return writeAsyncO(b, 0, b.length);
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array 
     * starting at offset <code>off</code> to this file output stream. 
     *
     * @param      b     the data.
     * @param      off   the start offset in the data.
     * @param      len   the number of bytes to write.
     * @exception  IOException  if an I/O error occurs.
     */
    public void write(byte b[], int off, int len) throws IOException {
	writeBytes(b, off, len);
    }
    
    public VoidContinuation writeAsyncO(byte b[], int off, int len) throws IOException
    {
		if (b == null) {
	    		throw new NullPointerException();
		} else if ((off < 0) || (off > b.length) || (len < 0) ||
		   		((off + len) > b.length) || ((off + len) < 0)) {
	    				throw new IndexOutOfBoundsException();
		} else if (len == 0) return new VoidContinuationOpt();

	    	int r= NativeIO.writeJNI(fd.fd, b, off, len);
    	
		if (r<0) throw new IOException();
		if (r<len)
		{
			return new WriteAsync2C(b, off+r, len-r);
		}
		return new VoidContinuationOpt();
    }
    
    class WriteAsync2C extends VoidContinuation implements IOContinuation {
	public void exception(Throwable t) {}

	byte b[];
	int off, len;
	
	public WriteAsync2C(byte b[], int off, int len)
	{
	    this.b= b;
	    this.off= off;
	    this.len= len;
	    Scheduler.addWrite(this);
	}
	
	public void resume()
	{
	    int r= NativeIO.writeJNI(fd.fd, b, off, len);
	    
	    if (r<0) next.exception(new IOException());
	    if (r<len)
		{
		    off+= r;
		    len-= r;
		    Scheduler.addWrite(this);
		} else next.resume();
	}
	
	public FileDescriptor getFD() { return fd; }
	
    }

    /**
     * Closes this file output stream and releases any system resources 
     * associated with this stream. This file output stream may no longer 
     * be used for writing bytes. 
     *
     * @exception  IOException  if an I/O error occurs.
     */
    public native void close() throws IOException;

    /**
     * Returns the file descriptor associated with this stream.
     *
     * @return  the <code>FileDescriptor</code> object that represents 
     *          the connection to the file in the file system being used 
     *          by this <code>FileOutputStream</code> object. 
     * 
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.FileDescriptor
     */
     public final FileDescriptor getFD()  throws IOException {
	if (fd != null) return fd;
	throw new IOException();
     }
    
    /**
     * Cleans up the connection to the file, and ensures that the 
     * <code>close</code> method of this file output stream is
     * called when there are no more references to this stream. 
     *
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.FileInputStream#close()
     */
    protected void finalize() throws IOException {
 	if (fd != null) {
 	    if (fd == fd.out || fd == fd.err) {
 		flush();
 	    } else {
 		close();
 	    }
 	}
    }

    private static native void initIDs();
    
    static {
	initIDs();
    }

}








