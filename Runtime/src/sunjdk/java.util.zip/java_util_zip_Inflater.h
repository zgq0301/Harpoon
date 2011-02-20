/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class java_util_zip_Inflater */

#ifndef _Included_java_util_zip_Inflater
#define _Included_java_util_zip_Inflater
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     java_util_zip_Inflater
 * Method:    setDictionary
 * Signature: ([BII)V
 */
JNIEXPORT void JNICALL Java_java_util_zip_Inflater_setDictionary
  (JNIEnv *, jobject, jbyteArray, jint, jint);

/*
 * Class:     java_util_zip_Inflater
 * Method:    inflate
 * Signature: ([BII)I
 */
JNIEXPORT jint JNICALL Java_java_util_zip_Inflater_inflate
  (JNIEnv *, jobject, jbyteArray, jint, jint);

/*
 * Class:     java_util_zip_Inflater
 * Method:    getAdler
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_java_util_zip_Inflater_getAdler
  (JNIEnv *, jobject);

/*
 * Class:     java_util_zip_Inflater
 * Method:    getTotalIn
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_java_util_zip_Inflater_getTotalIn
  (JNIEnv *, jobject);

/*
 * Class:     java_util_zip_Inflater
 * Method:    getTotalOut
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_java_util_zip_Inflater_getTotalOut
  (JNIEnv *, jobject);

/*
 * Class:     java_util_zip_Inflater
 * Method:    reset
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_util_zip_Inflater_reset
  (JNIEnv *, jobject);

/*
 * Class:     java_util_zip_Inflater
 * Method:    end
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_util_zip_Inflater_end
  (JNIEnv *, jobject);

/*
 * Class:     java_util_zip_Inflater
 * Method:    init
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_java_util_zip_Inflater_init
  (JNIEnv *, jobject, jboolean);

#ifdef __cplusplus
}
#endif
#endif
