/* DO NOT EDIT THIS FILE - it is machine generated */

#ifndef __java_lang_Class__
#define __java_lang_Class__

#include <jni.h>

#ifdef __cplusplus
extern "C"
{
#endif

JNIEXPORT jclass JNICALL Java_java_lang_Class_getComponentType (JNIEnv *env, jobject);
JNIEXPORT jobjectArray JNICALL Java_java_lang_Class_getDeclaredClasses (JNIEnv *env, jobject, jboolean);
JNIEXPORT jobjectArray JNICALL Java_java_lang_Class_getDeclaredConstructors (JNIEnv *env, jobject, jboolean);
JNIEXPORT jobjectArray JNICALL Java_java_lang_Class_getDeclaredFields (JNIEnv *env, jobject, jboolean);
JNIEXPORT jobjectArray JNICALL Java_java_lang_Class_getDeclaredMethods (JNIEnv *env, jobject, jboolean);
JNIEXPORT jclass JNICALL Java_java_lang_Class_getDeclaringClass (JNIEnv *env, jobject);
JNIEXPORT jobjectArray JNICALL Java_java_lang_Class_getInterfaces (JNIEnv *env, jobject);
JNIEXPORT jint JNICALL Java_java_lang_Class_getModifiers (JNIEnv *env, jobject);
JNIEXPORT jstring JNICALL Java_java_lang_Class_getName (JNIEnv *env, jobject);
JNIEXPORT jclass JNICALL Java_java_lang_Class_getSuperclass (JNIEnv *env, jobject);
JNIEXPORT jboolean JNICALL Java_java_lang_Class_isArray (JNIEnv *env, jobject);
JNIEXPORT jboolean JNICALL Java_java_lang_Class_isAssignableFrom (JNIEnv *env, jobject, jclass);
JNIEXPORT jboolean JNICALL Java_java_lang_Class_isInstance (JNIEnv *env, jobject, jobject);
JNIEXPORT jboolean JNICALL Java_java_lang_Class_isInterface (JNIEnv *env, jobject);
JNIEXPORT jboolean JNICALL Java_java_lang_Class_isPrimitive (JNIEnv *env, jobject);
JNIEXPORT void JNICALL Java_java_lang_Class_throwException (JNIEnv *env, jclass, jthrowable);
#undef java_lang_Class_serialVersionUID
#define java_lang_Class_serialVersionUID 3206093459760846163LL

#ifdef __cplusplus
}
#endif

#endif /* __java_lang_Class__ */
