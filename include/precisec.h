#ifndef INCLUDED_PRECISEC_H
#define INCLUDED_PRECISEC_H

#include "jni.h"

typedef void * jptr;
#define SHR(x,y) (((int32_t)(x))>>((y)&0x1f))
#define USHR(x,y) (((u_int32_t)(x))>>((y)&0x1f))
#define LSHR(x,y) (((int64_t)(x))>>((y)&0x3f))
#define LUSHR(x,y) (((u_int64_t)(x))>>((y)&0x3f))

#ifdef WITH_PRECISE_GC
# error WITH_PRECISE_GC not yet implemented.
# define IFPRECISE(x) x
  /* push an object onto the live vars stack */
# define PUSHOBJ(t)
  /* pop and return an object from the live vars stack */
# define POPOBJ()
  /* return the current top-of-stack pointer for the live vars stack */
# define STACKTOP()
  /* set the current top-of-stack for the live vars stack to x */
# define SETSTACKTOP(x)
#else /* !WITH_PRECISE_GC */
  /* not doing precise garbage collection, just ignore all the precise stuff */
# define IFPRECISE(x) /*no-op*/0
#endif

/* select which calling convention you'd like to use in the generated code */
#if !defined(USE_PAIR_RETURN) && !defined(USE_GLOBAL_SETJMP)
  /* default is USE_GLOBAL_SETJMP */
# define USE_GLOBAL_SETJMP
#endif

#ifdef USE_PAIR_RETURN /* ----------------------------------------------- */
#define FIRST_DECL_ARG(x)
#define DECLAREFUNC(rettype, funcname, args, segment) \
rettype ## _and_ex funcname args __attribute__ ((section (segment)))
#define DEFINEFUNC(rettype, funcname, args, segment) \
rettype ## _and_ex funcname args
#define DECLAREFUNCV(funcname, args, segment) \
void * funcname args __attribute__ ((section (segment)))
#define DEFINEFUNCV(funcname, args, segment) \
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
  if (__r.ex) { exv = __r.ex; goto handler; }\
  else retval = __r.value;\
}
#define CALLV(funcref, args, exv, handler, restoreexpr)\
{ void * __r = (funcref) args;\
  restoreexpr;\
  if (__r) { exv = __r; goto handler; }\
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
#include "fni-threadstate.h" /* for struct FNI_Thread_State */
#include <setjmp.h>
extern void *memcpy(void *dst, const void *src, size_t n);

#define FIRST_DECL_ARG(x)
#define DECLAREFUNC(rettype, funcname, args, segment) \
rettype funcname args __attribute__ ((section (segment)))
#define DEFINEFUNC(rettype, funcname, args, segment) \
rettype funcname args
#define DECLAREFUNCV(funcname, args, segment) \
void funcname args __attribute__ ((section (segment)))
#define DEFINEFUNCV(funcname, args, segment) \
void funcname args
#define RETURN(rettype, val)	return (val)
#define RETURNV()		return
#define THROW(rettype, val)	THROWV(val)
#define THROWV(val)\
longjmp(((struct FNI_Thread_State *)FNI_GetJNIEnv())->handler, (int)(val))

#define FIRST_PROTO_ARG(x)
#define FUNCPROTO(rettype, argtypes)\
rettype (*) argtypes
#define FUNCPROTOV(argtypes)\
void (*) argtypes

#define FIRST_CALL_ARG(x)
#define SETUP_HANDLER(exv, hlabel, restoreexpr)\
{ struct FNI_Thread_State *fts=(struct FNI_Thread_State *)FNI_GetJNIEnv();\
  jptr _ex_; IFPRECISE(void *_top_=STACKTOP());\
  jmp_buf _jb_; memcpy(_jb_, fts->handler, sizeof(_jb_));\
  if ((_ex_=(jptr)setjmp(fts->handler))!=NULL) {\
    exv=_ex_; memcpy(fts->handler, _jb_, sizeof(_jb_));\
    IFPRECISE(SETSTACKTOP(_top_)); restoreexpr; goto hlabel;\
  }
#define RESTORE_HANDLER\
  memcpy(fts->handler, _jb_, sizeof(_jb_));\
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
