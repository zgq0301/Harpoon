/* MemoryArea.h, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include <jni.h>

#ifndef _Included_MemoryArea
#define _Included_MemoryArea
#include "RTJmalloc.h"
#include "jni-private.h"
#include <string.h>
#ifdef __cplusplus
extern "C" {
#endif

void MemoryArea_init(JNIEnv* env);

/*
 * Class:     javax_realtime_MemoryArea
 * Method:    enterMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;Ljavax/realtime/MemAreaStack;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_MemoryArea_enterMemBlock
(JNIEnv *, jobject, jobject, jobject);

/*
 * Class:     javax_realtime_MemoryArea
 * Method:    exitMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;Ljavax/realtime/MemAreaStack;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_MemoryArea_exitMemBlock
(JNIEnv *, jobject, jobject, jobject);

/*
 * Class:     javax_realtime_MemoryArea
 * Method:    newArray
 * Signature: (Ljavax/realtime/RealtimeThread;Ljava/lang/Class;ILjava/lang/Object;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_javax_realtime_MemoryArea_newArray__Ljavax_realtime_RealtimeThread_2Ljava_lang_Class_2ILjava_lang_Object_2
(JNIEnv *, jobject, jobject, jclass, jint, jobject);

/*
 * Class:     javax_realtime_MemoryArea
 * Method:    newArray
 * Signature: (Ljavax/realtime/RealtimeThread;Ljava/lang/Class;[ILjava/lang/Object;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_javax_realtime_MemoryArea_newArray__Ljavax_realtime_RealtimeThread_2Ljava_lang_Class_2_3ILjava_lang_Object_2
(JNIEnv *, jobject, jobject, jclass, jintArray, jobject);

/*
 * Class:     javax_realtime_MemoryArea
 * Method:    newInstance
 * Signature: (Ljavax/realtime/RealtimeThread;Ljava/lang/reflect/Constructor;[Ljava/lang/Object)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_javax_realtime_MemoryArea_newInstance
  (JNIEnv *, jobject, jobject, jobject, jobjectArray);

/*
 * Class:     javax_realtime_MemoryArea
 * Method:    throwIllegalAssignmentError
 * Signature: (Ljava/lang/Object;Ljavax/realtime/MemoryArea;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_MemoryArea_throwIllegalAssignmentError
  (JNIEnv *, jobject, jobject, jobject);

/*
 * Class:     javax_realtime_MemoryArea
 * Method:    shadow
 * Signature: ()Ljavax/realtime/MemoryArea;
 */
JNIEXPORT jobject JNICALL Java_javax_realtime_MemoryArea_shadow
  (JNIEnv *, jobject);

/*
 * Class:     javax_realtime_MemoryArea
 * Method:    registerFinal
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_javax_realtime_MemoryArea_registerFinal
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif
