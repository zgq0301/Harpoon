/* PlainSocketImpl.c - Native methods for PlainSocketImpl class
   Copyright (C) 1998, 2002 Free Software Foundation, Inc.

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

#include <config.h>
#include <errno.h>

#ifdef HAVE_SYS_IOCTL_H
#include <sys/ioctl.h>
#endif

#ifdef HAVE_ASM_IOCTLS_H
#include <asm/ioctls.h>
#endif

#include <string.h>
 
#include <jni.h>
#include <jcl.h>

#include "java_net_PlainSocketImpl.h"

#include "javanet.h"

/*
 * Note that the functions in this module simply redirect to another
 * internal function.  Why?  Because many of these functions are shared
 * with PlainDatagramSocketImpl.  The unshared ones were done the same
 * way for consistency.
 */

/*************************************************************************/

/*
 * Creates a new stream or datagram socket
 */
JNIEXPORT void JNICALL
Java_java_net_PlainSocketImpl_create(JNIEnv *env, jobject this, jboolean stream)
{
  _javanet_create(env, this, stream);
}

/*************************************************************************/

/*
 * Close the socket.  Any underlying streams will be closed by this
 * action as well.
 */
JNIEXPORT void JNICALL
Java_java_net_PlainSocketImpl_close(JNIEnv *env, jobject this)
{
  _javanet_close(env, this, 1);
}

/*************************************************************************/

/*
 * Connects to the specified destination.
 */
JNIEXPORT void JNICALL
Java_java_net_PlainSocketImpl_connect(JNIEnv *env, jobject this, 
                                      jobject addr, jint port)
{
  _javanet_connect(env, this, addr, port);
}

/*************************************************************************/

/*
 * This method binds the specified address to the specified local port.
 * Note that we have to set the local address and local port public instance 
 * variables. 
 */
JNIEXPORT void JNICALL
Java_java_net_PlainSocketImpl_bind(JNIEnv *env, jobject this, jobject addr,
                                   jint port)
{
  _javanet_bind(env, this, addr, port, 1);
}

/*************************************************************************/

/*
 * Starts listening on a socket with the specified number of pending 
 * connections allowed.
 */
JNIEXPORT void JNICALL
Java_java_net_PlainSocketImpl_listen(JNIEnv *env, jobject this, jint queuelen)
{
  _javanet_listen(env, this, queuelen);
}

/*************************************************************************/

/*
 * Accepts a new connection and assigns it to the passed in SocketImpl
 * object. Note that we assume this is a PlainSocketImpl just like us.
 */
JNIEXPORT void JNICALL
Java_java_net_PlainSocketImpl_accept(JNIEnv *env, jobject this, jobject impl)
{
  _javanet_accept(env, this, impl);
}

/*************************************************************************/

JNIEXPORT jint JNICALL
Java_java_net_PlainSocketImpl_available(JNIEnv *env, jobject this)
{
  int fd;
  int count = 0;
  jclass cls;
  jfieldID fid;
  
  cls = (*env)->GetObjectClass(env, this);
  if (cls == 0)
    {
      JCL_ThrowException(env, IO_EXCEPTION, "internal error");
      return 0;
    }
  
  fid = (*env)->GetFieldID(env, cls, "native_fd", "I"); 
  if (fid == 0)
    {
      JCL_ThrowException(env, IO_EXCEPTION, "internal error");
      return 0;
    }

  fd = (*env)->GetIntField(env, this, fid);
  
  if (ioctl(fd, FIONREAD, &count) == -1)
    {
      JCL_ThrowException(env, IO_EXCEPTION, strerror(errno));
      return 0;
    }
  else
    return count;
}

/*************************************************************************/

/*
 * This method sets the specified option for a socket
 */
JNIEXPORT void JNICALL
Java_java_net_PlainSocketImpl_setOption(JNIEnv *env, jobject this, 
                                        jint option_id, jobject val)
{
  _javanet_set_option(env, this, option_id, val);
}

/*************************************************************************/

/*
 * This method sets the specified option for a socket
 */
JNIEXPORT jobject JNICALL
Java_java_net_PlainSocketImpl_getOption(JNIEnv *env, jobject this, 
                                        jint option_id)
{
  return(_javanet_get_option(env, this, option_id));
}

/*************************************************************************/

/*
 * Reads a buffer from a remote host
 */
JNIEXPORT jint JNICALL
Java_java_net_PlainSocketImpl_read(JNIEnv *env, jobject this, jarray buf,
                                   jint offset, jint len)
{
  return(_javanet_recvfrom(env, this, buf, offset, len, 0, 0));
}

/*************************************************************************/

/*
 * Writes a buffer to the remote host
 */
JNIEXPORT void JNICALL
Java_java_net_PlainSocketImpl_write(JNIEnv *env, jobject this, jarray buf,
                                    jint offset, jint len)
{
  _javanet_sendto(env, this, buf, offset, len, 0, 0);
}

