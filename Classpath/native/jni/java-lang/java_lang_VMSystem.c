/* System.c -- native code for java.lang.System
   Copyright (C) 1998, 1999, 2000, 2002 Free Software Foundation, Inc.

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

#include "java_lang_VMSystem.h"
#include <sys/time.h>
#include <stdlib.h>

/*
 * Class:     java_lang_VMSystem
 * Method:    setIn0
 * Signature: (Ljava/io/InputStream;)V
 */
JNIEXPORT void JNICALL
Java_java_lang_VMSystem_setIn (JNIEnv * env, jclass thisClass, jobject obj)
{
  jclass cls;
  jfieldID field;

  cls = JCL_FindClass(env, "java/lang/System");
  if (!cls)
    return;

  field = (*env)->GetStaticFieldID(env, cls, "in",
				   "Ljava/io/InputStream;");
  if (!field)
    return;
  (*env)->SetStaticObjectField(env, cls, field, obj);
}

/*
 * Class:     java_lang_VMSystem
 * Method:    setOut0
 * Signature: (Ljava/io/PrintStream;)V
 */
JNIEXPORT void JNICALL
Java_java_lang_VMSystem_setOut (JNIEnv * env, jclass thisClass, jobject obj)
{
  jclass cls;
  jfieldID field;

  cls = JCL_FindClass(env, "java/lang/System");
  if (!cls)
    return;

  field = (*env)->GetStaticFieldID(env, cls, "out",
				   "Ljava/io/PrintStream;");
  if (!field)
    return;
  (*env)->SetStaticObjectField(env, cls, field, obj);
}

/*
 * Class:     java_lang_VMSystem
 * Method:    setErr0
 * Signature: (Ljava/io/PrintStream;)V
 */
JNIEXPORT void JNICALL
Java_java_lang_VMSystem_setErr (JNIEnv * env, jclass thisClass, jobject obj)
{
  jclass cls;
  jfieldID field;

  cls = JCL_FindClass(env, "java/lang/System");
  if (!cls)
    return;

  field = (*env)->GetStaticFieldID(env, cls, "err",
				   "Ljava/io/PrintStream;");
  if (!field)
    return;
  (*env)->SetStaticObjectField(env, cls, field, obj);
}

/*
 * Class:     java_lang_VMSystem
 * Method:    currentTimeMillis
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL
Java_java_lang_VMSystem_currentTimeMillis (JNIEnv * env, jclass thisClass)
{
  /* Note: this implementation copied directly from Japhar's, by Chris Toshok. */
  jlong result;
  struct timeval tp;

  if (gettimeofday(&tp, NULL) == -1)
    (*env)->FatalError(env, "gettimeofday call failed.");

  result = (jlong)tp.tv_sec;
  result *= 1000;
  result += (tp.tv_usec / 1000);

  return result;
}

JNIEXPORT jboolean JNICALL 
Java_java_lang_VMSystem_isWordsBigEndian (JNIEnv *env, jclass clazz)
{
  /* Are we little or big endian?  From Harbison&Steele.  */
  union
  {
    long l;
    char c[sizeof (long)];
  } u;

  u.l = 1;
  return (u.c[sizeof (long) - 1] == 1);
}
