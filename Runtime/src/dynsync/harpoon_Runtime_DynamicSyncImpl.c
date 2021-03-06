#include <jni.h>
#include <jni-private.h>
#include "config.h"
#include "harpoon_Runtime_DynamicSyncImpl.h"
#include "dynsync.h"

/*
 * Class:     harpoon_Runtime_DynamicSyncImpl
 * Method:    isSync
 * Signature: (Ljava/lang/Object;)Z
 */
JNIEXPORT jboolean JNICALL Java_harpoon_Runtime_DynamicSyncImpl_isNoSync
  (JNIEnv *env, jclass cls, jobject obj) {
  return EXACT_isNoSync(FNI_UNWRAP(obj)) ? JNI_TRUE : JNI_FALSE;
}
