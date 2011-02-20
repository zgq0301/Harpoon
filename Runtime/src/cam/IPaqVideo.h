/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class IPaqVideo */

#ifndef _Included_IPaqVideo
#define _Included_IPaqVideo
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     IPaqVideo
 * Method:    setup
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_ipaq_IPaqVideo_setup
  (JNIEnv *, jobject);

/*
 * Class:     IPaqVideo
 * Method:    capture
 * Signature: ([B[B[B)V
 */
JNIEXPORT void JNICALL Java_ipaq_IPaqVideo_capture___3B_3B_3B
  (JNIEnv *, jobject, jbyteArray, jbyteArray, jbyteArray);

/*
 * Class:     IPaqVideo
 * Method:    capture
 * Signature: ([B)V
 */
JNIEXPORT void JNICALL Java_ipaq_IPaqVideo_capture___3B
  (JNIEnv *, jobject, jbyteArray);

/*
 * Class:     IPaqVideo
 * Method:    unsafeSetProperties
 * Signature: (BBBBZZII)V
 */
JNIEXPORT void JNICALL Java_ipaq_IPaqVideo_unsafeSetProperties
  (JNIEnv *, jobject, jbyte, jbyte, jbyte, jbyte, jboolean, jboolean, jint, jint);

#ifdef __cplusplus
}
#endif
#endif
