#ifndef INCLUDED_PRECISEC_H
#define INCLUDED_PRECISEC_H

#include "config.h"
#include "jni.h"
#include "fni-threadstate.h" /* for struct FNI_Thread_State */
#include "fni-ptroff.h" /* for ptroff_t */
#include "fni-ptrmask.h" /* for PTRMASK */
#include "compiler.h" /* for likely()/unlikely() */
#ifdef WITH_GENERATIONAL_GC
# include "write_barrier.h" /* for generational_write_barrier */
#endif /* WITH_GENERATIONAL_GC */
#ifdef WITH_DYNAMIC_SYNC_REMOVAL
# include "dynsync.h"
#endif /* WITH_DYNAMIC_SYNC_REMOVAL */
#ifdef WITH_TRANSACTIONS
# include "transact/transact-pc.h"
#endif /* WITH_TRANSACTIONS */

typedef void * jptr;
#define SHR(x,y) (((int32_t)(x))>>((y)&0x1f))
#define USHR(x,y) (((uint32_t)(x))>>((y)&0x1f))
#define LSHR(x,y) (((int64_t)(x))>>((y)&0x3f))
#define LUSHR(x,y) (((uint64_t)(x))>>((y)&0x3f))

/* thread-state optimization for single-threaded case */
#ifndef WITH_THREADS
extern JNIEnv *FNI_JNIEnv; /* single, global, JNIEnv */
extern inline JNIEnv *FNI_GetJNIEnv(void) { return FNI_JNIEnv; }
#endif

/* support for precise-gc stack */
#ifdef WITH_PRECISE_GC
# define IFPRECISE(x) x
# define FTS() ((struct FNI_Thread_State *)FNI_GetJNIEnv())
  /* push an object onto the live vars stack */
# define PUSHOBJ(t) ((FTS()->localrefs_next++)->obj=t)
  /* pop and return an object from the live vars stack */
# define POPOBJ() ((--(FTS()->localrefs_next))->obj)
  /* return the current top-of-stack pointer for the live vars stack */
# define STACKTOP() (FTS()->localrefs_next)
  /* set the current top-of-stack for the live vars stack to x */
# define SETSTACKTOP(x) (FTS()->localrefs_next=x)
#else /* !WITH_PRECISE_GC */
  /* not doing precise garbage collection, just ignore all the precise stuff */
# define IFPRECISE(x) /*no-op*/0
#endif

/* some architectures don't support section attributes */
#ifdef NO_SECTION_SUPPORT
# define SECTION(x)
#else
# define SECTION(x) __attribute__ ((section (x)))
#endif /* NO_SECTION_SUPPORT */

/* select which calling convention you'd like to use in the generated code */
#if !defined(USE_PAIR_RETURN) && !defined(USE_GLOBAL_SETJMP)
  /* default is USE_PAIR_RETURN */
# define USE_PAIR_RETURN
#endif

#ifdef USE_PAIR_RETURN /* ----------------------------------------------- */
#define FIRST_DECL_ARG(x)
#define DECLAREFUNC(rettype, funcname, args, segment, moreattrs) \
rettype ## _and_ex funcname args SECTION(segment) moreattrs
#define DEFINEFUNC(rettype, funcname, args, segment, moreattrs) \
rettype ## _and_ex funcname args
#define DECLAREFUNCV(funcname, args, segment, moreattrs) \
void * funcname args SECTION(segment) moreattrs
#define DEFINEFUNCV(funcname, args, segment, moreattrs) \
void * funcname args
#define RETURN(rettype, val)	return ((rettype ## _and_ex) { NULL, (val) })
#define RETURNV()		return NULL
#define THROW(rettype, val)	return ((rettype ## _and_ex) { (val), 0 })
#define THROWV(val)		return (val)

#define FIRST_PROTO_ARG(x)
#define FUNCPROTO(rettype, argtypes)\
rettype ## _and_ex (*) argtypes
#define FUNCPROTOV(argtypes)\
void * (*) argtypes

#define FIRST_CALL_ARG(x)
#define CALL(rettype, retval, funcref, args, exv, handler, restoreexpr)\
{ rettype ## _and_ex __r = (funcref) args;\
  restoreexpr;\
  if (unlikely(__r.ex!=NULL)) { exv = __r.ex; goto handler; }\
  else retval = __r.value;\
}
#define CALLV(funcref, args, exv, handler, restoreexpr)\
{ void * __r = (funcref) args;\
  restoreexpr;\
  if (unlikely(__r!=NULL)) { exv = __r; goto handler; }\
}
/* no-handler case is same as handler case */
#define CALL_NH(rettype, retval, funcref, args, exv, handler, restoreexpr)\
CALL(rettype, retval, funcref, args, exv, handler, restoreexpr)
#define CALLV_NH(funcref, args, exv, handler, restoreexpr)\
CALLV(funcref, args, exv, handler, restoreexpr)

/* <foo> and exception pairs */
typedef struct {
  void *ex;
  jdouble value;
} jdouble_and_ex;
typedef struct {
  void *ex;
  jfloat value;
} jfloat_and_ex;
typedef struct {
  void *ex;
  jint value;
} jint_and_ex;
typedef struct {
  void *ex;
  jlong value;
} jlong_and_ex;
typedef struct {
  void *ex;
  jptr value;
} jptr_and_ex;
#endif /* USE_PAIR_RETURN ----------------------------------------- */

#ifdef USE_GLOBAL_SETJMP /* ----------------------------------------------- */
#include <setjmp.h>
extern void *memcpy(void *dst, const void *src, size_t n);

#define FIRST_DECL_ARG(x)
#define DECLAREFUNC(rettype, funcname, args, segment, moreattrs) \
rettype funcname args SECTION(segment) moreattrs
#define DEFINEFUNC(rettype, funcname, args, segment, moreattrs) \
rettype funcname args
#define DECLAREFUNCV(funcname, args, segment, moreattrs) \
void funcname args SECTION(segment) moreattrs
#define DEFINEFUNCV(funcname, args, segment, moreattrs) \
void funcname args
#define RETURN(rettype, val)	return (val)
#define RETURNV()		return
#define THROW(rettype, val)	THROWV(val)
#define THROWV(val)\
longjmp(*(((struct FNI_Thread_State *)FNI_GetJNIEnv())->handler), (int)(val))

#define FIRST_PROTO_ARG(x)
#define FUNCPROTO(rettype, argtypes)\
rettype (*) argtypes
#define FUNCPROTOV(argtypes)\
void (*) argtypes

#define FIRST_CALL_ARG(x)
#define SETUP_HANDLER(exv, hlabel, restoreexpr)\
{ struct FNI_Thread_State *fts=(struct FNI_Thread_State *)FNI_GetJNIEnv();\
  jptr _ex_; jmp_buf _jb_, *oldhandler;\
  IFPRECISE(void *_top_=STACKTOP());\
  oldhandler=fts->handler; fts->handler=&_jb_;\
  if (unlikely((_ex_=(jptr)setjmp(_jb_))!=NULL)) {\
    exv=_ex_; fts->handler=oldhandler;\
    IFPRECISE(SETSTACKTOP(_top_)); restoreexpr; goto hlabel;\
  }
#define RESTORE_HANDLER\
  fts->handler=oldhandler;\
}
/* no-handler versions of calls */
#define CALL_NH(rettype, retval, funcref, args, exv, handler, restoreexpr)\
retval = (funcref) args; restoreexpr;
#define CALLV_NH(funcref, args, exv, handler, restoreexpr)\
(funcref) args; restoreexpr;

#define CALL(rettype, retval, funcref, args, exv, handler, restoreexpr)\
SETUP_HANDLER(exv, handler, restoreexpr)\
CALL_NH(rettype, retval, funcref, args, exv, handler, restoreexpr);\
RESTORE_HANDLER
#define CALLV(funcref, args, exv, handler, restoreexpr)\
SETUP_HANDLER(exv, handler, restoreexpr)\
CALLV_NH(funcref, args, exv, handler, restoreexpr);\
RESTORE_HANDLER

#endif /* USE_GLOBAL_SETJMP ----------------------------------------- */

#endif /* INCLUDED_PRECISEC_H */
