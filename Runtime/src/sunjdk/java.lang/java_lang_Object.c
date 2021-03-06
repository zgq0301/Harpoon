#include <jni.h>
#include <jni-private.h>
#include "java_lang_Object.h"
#include "java_lang_System.h" /* for Java_java_lang_System_identityHashCode */

#include <assert.h>
#include "config.h" /* for WITH_TRANSACTIONS */

#include "../../java.lang/object.h" /* useful library-indep implementations */

/*
 * Class:     java_lang_Object
 * Method:    getClass
 * Signature: ()Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_lang_Object_getClass
  (JNIEnv *env, jobject obj) {
    return (*env)->GetObjectClass(env, obj);
}

/*
 * Class:     java_lang_Object
 * Method:    hashCode
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_java_lang_Object_hashCode
  (JNIEnv *env, jobject obj) {
    return Java_java_lang_System_identityHashCode(env, NULL, obj);
}

#ifdef WITH_TRANSACTIONS
  /* transactions defines own versions of clone() */
#else /* !WITH_TRANSACTIONS */
/*
 * Class:     java_lang_Object
 * Method:    clone
 * Signature: ()Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_java_lang_Object_clone
  (JNIEnv *env, jobject obj) {
    struct claz *claz = FNI_CLAZ(FNI_UNWRAP_MASKED(obj));
    uint32_t size = claz->size;
    assert(claz->component_claz==NULL/* not an array*/);
    return fni_object_cloneHelper(env, obj, size);
}

#define PRIMITIVEARRAYCLONE(name, type, sig)\
JNIEXPORT jobject JNICALL Java__3##sig##_clone\
  (JNIEnv *env, jobject obj) {\
    jsize len = (*env)->GetArrayLength(env, (jarray) obj);\
    return fni_object_cloneHelper(env, obj, sizeof(struct aarray) + sizeof(type)*len);\
}
PRIMITIVEARRAYCLONE(Boolean, jboolean, Z);
PRIMITIVEARRAYCLONE(Byte, jbyte, B);
PRIMITIVEARRAYCLONE(Char, jchar, C);
PRIMITIVEARRAYCLONE(Short, jshort, S);
PRIMITIVEARRAYCLONE(Int, jint, I);
PRIMITIVEARRAYCLONE(Long, jlong, J);
PRIMITIVEARRAYCLONE(Float, jfloat, F);
PRIMITIVEARRAYCLONE(Double, jdouble, D);
/* not really a primitive =) */
PRIMITIVEARRAYCLONE(Object, struct oobj *, Ljava_lang_Object_2);
#endif /* !WITH_TRANSACTIONS */

/*
 * Class:     java_lang_Object
 * Method:    notify
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Object_notify
  (JNIEnv *env, jobject _this) {
  fni_object_notify(env, _this);
}

/*
 * Class:     java_lang_Object
 * Method:    notifyAll
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Object_notifyAll
  (JNIEnv *env, jobject _this) {
  fni_object_notifyAll(env, _this);
}
/*
 * Class:     java_lang_Object
 * Method:    wait
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_java_lang_Object_wait
  (JNIEnv *env, jobject _this, jlong millis) {
  fni_object_wait(env, _this, millis, 0);
}

#ifdef WITH_TRANSACTIONS
/* transaction support.  no monitor is held initially. */
/*
 * Class:     java_lang_Object
 * Method:    notify
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Object_notify_00024_00024withtrans
  (JNIEnv *env, jobject _this, jobject commitrec) {
  /* called inside transaction context.  since 'too many notifies' is
   * valid semantics, we don't have to worry about undo-ing this notify
   * if the transaction doesn't commit.  Lock, notify, unlock.
   * new transaction can't modify anything until this transaction commits.
   * XXX: possible problem because listener will wake up before this
   * transaction commits, causing a lost notification? */
  FNI_MonitorEnter(env, _this);
  Java_java_lang_Object_notify(env, _this);
  FNI_MonitorExit(env, _this);
}

/*
 * Class:     java_lang_Object
 * Method:    notifyAll
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Object_notifyAll_00024_00024withtrans
  (JNIEnv *env, jobject _this, jobject commitrec) {
  /* same comments as above. */
  FNI_MonitorEnter(env, _this);
  Java_java_lang_Object_notifyAll(env, _this);
  FNI_MonitorExit(env, _this);
}
/*
 * Class:     java_lang_Object
 * Method:    wait
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_java_lang_Object_wait_00024_00024withtrans
  (JNIEnv *env, jobject _this, jobject commitrec, jlong millis) {
  /* should do a commit, then a wait (ought to be atomic w/respect to
   * notify) and then start a new (sub) transaction.  In other words,
   * really needs to return a CommitRec. */
  assert(0);
}
#endif /* WITH_TRANSACTIONS */
