/* DO NOT EDIT THIS FILE - it is machine generated */

#ifndef __java_net_PlainDatagramSocketImpl__
#define __java_net_PlainDatagramSocketImpl__

#include <jni.h>

#ifdef __cplusplus
extern "C"
{
#endif

extern void Java_java_net_PlainDatagramSocketImpl_create (JNIEnv *env, jobject);
extern void Java_java_net_PlainDatagramSocketImpl_close (JNIEnv *env, jobject);
extern void Java_java_net_PlainDatagramSocketImpl_bind (JNIEnv *env, jobject, jint, jobject);
extern void Java_java_net_PlainDatagramSocketImpl_sendto (JNIEnv *env, jobject, jobject, jint, jbyteArray, jint);
extern void Java_java_net_PlainDatagramSocketImpl_receive (JNIEnv *env, jobject, jobject);
extern void Java_java_net_PlainDatagramSocketImpl_join (JNIEnv *env, jobject, jobject);
extern void Java_java_net_PlainDatagramSocketImpl_leave (JNIEnv *env, jobject, jobject);
extern jobject Java_java_net_PlainDatagramSocketImpl_getOption (JNIEnv *env, jobject, jint);
extern void Java_java_net_PlainDatagramSocketImpl_setOption (JNIEnv *env, jobject, jint, jobject);

#ifdef __cplusplus
}
#endif

#endif /* __java_net_PlainDatagramSocketImpl__ */
