/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class java_net_PlainSocketImpl */

#ifndef _Included_java_net_PlainSocketImpl
#define _Included_java_net_PlainSocketImpl
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     java_net_PlainSocketImpl
 * Method:    socketCreate
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_java_net_PlainSocketImpl_socketCreate
  (JNIEnv *, jobject, jboolean);

/*
 * Class:     java_net_PlainSocketImpl
 * Method:    socketConnect
 * Signature: (Ljava/net/InetAddress;I)V
 */
JNIEXPORT void JNICALL Java_java_net_PlainSocketImpl_socketConnect
  (JNIEnv *, jobject, jobject, jint);

/*
 * Class:     java_net_PlainSocketImpl
 * Method:    socketBind
 * Signature: (Ljava/net/InetAddress;I)V
 */
JNIEXPORT void JNICALL Java_java_net_PlainSocketImpl_socketBind
  (JNIEnv *, jobject, jobject, jint);

/*
 * Class:     java_net_PlainSocketImpl
 * Method:    socketListen
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_java_net_PlainSocketImpl_socketListen
  (JNIEnv *, jobject, jint);

/*
 * Class:     java_net_PlainSocketImpl
 * Method:    socketAccept
 * Signature: (Ljava/net/SocketImpl;)V
 */
JNIEXPORT void JNICALL Java_java_net_PlainSocketImpl_socketAccept
  (JNIEnv *, jobject, jobject);

/*
 * Class:     java_net_PlainSocketImpl
 * Method:    socketAvailable
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_java_net_PlainSocketImpl_socketAvailable
  (JNIEnv *, jobject);

/*
 * Class:     java_net_PlainSocketImpl
 * Method:    socketClose
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_net_PlainSocketImpl_socketClose
  (JNIEnv *, jobject);

/*
 * Class:     java_net_PlainSocketImpl
 * Method:    initProto
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_net_PlainSocketImpl_initProto
  (JNIEnv *, jclass);

/*
 * Class:     java_net_PlainSocketImpl
 * Method:    socketSetOption
 * Signature: (IZLjava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_java_net_PlainSocketImpl_socketSetOption
  (JNIEnv *, jobject, jint, jboolean, jobject);

/*
 * Class:     java_net_PlainSocketImpl
 * Method:    socketGetOption
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_java_net_PlainSocketImpl_socketGetOption
  (JNIEnv *, jobject, jint);

#ifdef __cplusplus
}
#endif
#endif
