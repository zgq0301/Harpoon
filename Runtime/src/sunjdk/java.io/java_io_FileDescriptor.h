/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class java_io_FileDescriptor */

#ifndef _Included_java_io_FileDescriptor
#define _Included_java_io_FileDescriptor
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     java_io_FileDescriptor
 * Method:    valid
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_io_FileDescriptor_valid
  (JNIEnv *, jobject);

/*
 * Class:     java_io_FileDescriptor
 * Method:    sync
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_io_FileDescriptor_sync
  (JNIEnv *, jobject);

/*
 * Class:     java_io_FileDescriptor
 * Method:    initSystemFD
 * Signature: (Ljava/io/FileDescriptor;I)Ljava/io/FileDescriptor;
 */
JNIEXPORT jobject JNICALL Java_java_io_FileDescriptor_initSystemFD
  (JNIEnv *, jclass, jobject, jint);

#ifdef __cplusplus
}
#endif
#endif
