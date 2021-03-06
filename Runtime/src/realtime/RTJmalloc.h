/* RTJmalloc.h, created by wbeebee 
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu> 
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#ifndef __RTJmalloc_h__
#define __RTJmalloc_h__

#include "MemBlock.h"
#include "jni-private.h"
#include "RTJconfig.h"
#include "MemoryArea.h"

#ifdef WITH_REALTIME_THREADS
#include "threads.h"
#endif

void RTJ_preinit();
void RTJ_init();
int RTJ_init_in_progress;

#ifdef RTJ_DEBUG_REF
#define RTJ_malloc(size) RTJ_malloc_ref(size, __LINE__, __FILE__);
inline void* RTJ_malloc_ref(size_t size, const int line, const char *file);
#else
inline void* RTJ_malloc(size_t size);
#endif

inline struct MemBlock* MemBlock_currentMemBlock();
inline void MemBlock_setCurrentMemBlock(JNIEnv* env, 
					 jobject realtimeThread,
					struct MemBlock* memBlock);
#ifdef WITH_MEMORYAREA_TAGS
void RTJ_tagObject(JNIEnv* env, jobject obj);
#endif


#endif /* RTJmalloc_h__ */
