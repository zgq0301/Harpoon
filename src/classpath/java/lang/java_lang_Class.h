/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class java_lang_Class */

#ifndef _Included_java_lang_Class
#define _Included_java_lang_Class
#ifdef __cplusplus
extern "C" {
#endif
#undef java_lang_Class_serialVersionUID
#define java_lang_Class_serialVersionUID 3206093459760846163LL
/* Inaccessible static: unknownProtectionDomain */
/*
 * Class:     java_lang_Class
 * Method:    forName
 * Signature: (Ljava/lang/String;)Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_lang_Class_forName
  (JNIEnv *, jclass, jstring);

/*
 * Class:     java_lang_Class
 * Method:    isInstance
 * Signature: (Ljava/lang/Object;)Z
 */
JNIEXPORT jboolean JNICALL Java_java_lang_Class_isInstance
  (JNIEnv *, jobject, jobject);

/*
 * Class:     java_lang_Class
 * Method:    isAssignableFrom
 * Signature: (Ljava/lang/Class;)Z
 */
JNIEXPORT jboolean JNICALL Java_java_lang_Class_isAssignableFrom
  (JNIEnv *, jobject, jclass);

/*
 * Class:     java_lang_Class
 * Method:    isInterface
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_lang_Class_isInterface
  (JNIEnv *, jobject);

/*
 * Class:     java_lang_Class
 * Method:    isPrimitive
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_lang_Class_isPrimitive
  (JNIEnv *, jobject);

/*
 * Class:     java_lang_Class
 * Method:    getName
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_java_lang_Class_getName
  (JNIEnv *, jobject);

/*
 * Class:     java_lang_Class
 * Method:    getSuperclass
 * Signature: ()Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_lang_Class_getSuperclass
  (JNIEnv *, jobject);

/*
 * Class:     java_lang_Class
 * Method:    getInterfaces
 * Signature: ()[Ljava/lang/Class;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_Class_getInterfaces
  (JNIEnv *, jobject);

/*
 * Class:     java_lang_Class
 * Method:    getModifiers
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_java_lang_Class_getModifiers
  (JNIEnv *, jobject);

/*
 * Class:     java_lang_Class
 * Method:    getDeclaringClass
 * Signature: ()Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_lang_Class_getDeclaringClass
  (JNIEnv *, jobject);

/*
 * Class:     java_lang_Class
 * Method:    getClasses
 * Signature: ()[Ljava/lang/Class;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_Class_getClasses
  (JNIEnv *, jobject);

/*
 * Class:     java_lang_Class
 * Method:    getFields
 * Signature: ()[Ljava/lang/reflect/Field;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_Class_getFields
  (JNIEnv *, jobject);

/*
 * Class:     java_lang_Class
 * Method:    getMethods
 * Signature: ()[Ljava/lang/reflect/Method;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_Class_getMethods
  (JNIEnv *, jobject);

/*
 * Class:     java_lang_Class
 * Method:    getConstructors
 * Signature: ()[Ljava/lang/reflect/Constructor;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_Class_getConstructors
  (JNIEnv *, jobject);

/*
 * Class:     java_lang_Class
 * Method:    getField
 * Signature: (Ljava/lang/String;)Ljava/lang/reflect/Field;
 */
JNIEXPORT jobject JNICALL Java_java_lang_Class_getField
  (JNIEnv *, jobject, jstring);

/*
 * Class:     java_lang_Class
 * Method:    getMethod
 * Signature: (Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;
 */
JNIEXPORT jobject JNICALL Java_java_lang_Class_getMethod
  (JNIEnv *, jobject, jstring, jobjectArray);

/*
 * Class:     java_lang_Class
 * Method:    getConstructor
 * Signature: ([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;
 */
JNIEXPORT jobject JNICALL Java_java_lang_Class_getConstructor
  (JNIEnv *, jobject, jobjectArray);

/*
 * Class:     java_lang_Class
 * Method:    getDeclaredClasses
 * Signature: ()[Ljava/lang/Class;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_Class_getDeclaredClasses
  (JNIEnv *, jobject);

/*
 * Class:     java_lang_Class
 * Method:    getDeclaredFields
 * Signature: ()[Ljava/lang/reflect/Field;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_Class_getDeclaredFields
  (JNIEnv *, jobject);

/*
 * Class:     java_lang_Class
 * Method:    getDeclaredMethods
 * Signature: ()[Ljava/lang/reflect/Method;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_Class_getDeclaredMethods
  (JNIEnv *, jobject);

/*
 * Class:     java_lang_Class
 * Method:    getDeclaredConstructors
 * Signature: ()[Ljava/lang/reflect/Constructor;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_Class_getDeclaredConstructors
  (JNIEnv *, jobject);

/*
 * Class:     java_lang_Class
 * Method:    getDeclaredField
 * Signature: (Ljava/lang/String;)Ljava/lang/reflect/Field;
 */
JNIEXPORT jobject JNICALL Java_java_lang_Class_getDeclaredField
  (JNIEnv *, jobject, jstring);

/*
 * Class:     java_lang_Class
 * Method:    getDeclaredMethod
 * Signature: (Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;
 */
JNIEXPORT jobject JNICALL Java_java_lang_Class_getDeclaredMethod
  (JNIEnv *, jobject, jstring, jobjectArray);

/*
 * Class:     java_lang_Class
 * Method:    getDeclaredConstructor
 * Signature: ([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;
 */
JNIEXPORT jobject JNICALL Java_java_lang_Class_getDeclaredConstructor
  (JNIEnv *, jobject, jobjectArray);

/*
 * Class:     java_lang_Class
 * Method:    getClassLoader0
 * Signature: ()Ljava/lang/ClassLoader;
 */
JNIEXPORT jobject JNICALL Java_java_lang_Class_getClassLoader0
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif
