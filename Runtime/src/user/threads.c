#include "config.h"
#ifdef WITH_USER_THREADS
#include <assert.h>
#include "threads.h"
#include <errno.h>
#include "memstats.h"

#ifdef WITH_REALTIME_JAVA
#include "../realtime/RTJconfig.h"
#endif
#ifdef WITH_REALTIME_THREADS
#include <jni.h>
#include <jni-private.h>
#include "../realtime/threads.h"
#include "../realtime/qcheck.h"
#include "../realtime/Scheduler.h"
#include <setjmp.h>
#include <string.h>
#include <unistd.h>
/* jump point for the final jump back to main - defined in startup.c */
extern jmp_buf main_return_jump;
#else
#include <stdlib.h> /* for exit */

#endif

#ifdef GLIBC_COMPAT4
#include <string.h> /* for memset prototype */
#include <fni-ptroff.h>
#endif

struct thread_list *gtl,*ioptr;
#ifdef WITH_REALTIME_THREADS
struct thread_queue_struct *currentThread; //pointer to the current thread
#endif
void *oldstack;

#ifdef GLIBC_COMPAT2
#ifdef errno
#undef errno
#endif
extern int errno;
int* __errno_location() { 
  return &errno; 
}
#endif

#ifdef GLIBC_COMPAT3
typedef void* pthread_descr; 

pthread_descr __pthread_find_self() { 
  return NULL; 
}

/* pthread_descr __pthread_find_self() { */

/* } */

#endif

#ifdef GLIBC_COMPAT4
void* calloc(size_t nmemb, size_t size) {
  void* foo = malloc(nmemb*size);
  memset(foo, 0, nmemb*size);
  return foo;
}

void* malloc(size_t size) {
  return (void*)(((ptroff_t)(sbrk((size+7)&(~7))+7))&(~7));
}

void free(void* ptr) {
}
#endif

#ifdef GLIBC_COMPAT5
#include <ctype.h>

#ifndef __ctype_b_loc
unsigned short int** __ctype_b_loc() {
  return (unsigned short int**)(&__ctype_b);
}

__int32_t** __ctype_tolower_loc() {
  return (__int32_t**)(&__ctype_tolower);
}
__int32_t** __ctype_toupper_loc() {
  return (__int32_t**)(&__ctype_toupper);
}

void glibc_compat5() {}
#else
unsigned short int *__ctype_b;
__int32_t *__ctype_tolower;
__int32_t *__ctype_toupper;

void glibc_compat5() {
  __ctype_b = *(unsigned short int**)__ctype_b_loc();
  __ctype_tolower = *(__int32_t**)__ctype_tolower_loc();
  __ctype_toupper = *(__int32_t**)__ctype_toupper_loc();
}
#endif
#endif

void startnext();
int * getFDsintSEL(int);
void wakeacondthread(user_cond_t *x);
void wakeallcondthread(user_cond_t *x);

void restorethread() {
  /* 
   * Restore the threads state.
   */
#ifdef WITH_REALTIME_THREADS
  settimer();
#endif
  machdep_restore_float_state();
  machdep_restore_state();
}

/* when using RealtimeThreads, this function will be called with the */
/* thread to switch to */

/* The function that calls machdep_save_state must not return until */
/* machdep_restore_state is called, or the stack frame may be trashed. */
#ifndef WITH_REALTIME_THREADS
void transfer()
#else
void transfer(struct thread_queue_struct * mthread)
#endif
{
#ifndef WITH_REALTIME_THREADS
  struct thread_list *tl;
  machdep_save_float_state(&(gtl->mthread));
  if (machdep_save_state(&(gtl->mthread))) {
    return;
  }
#else
  /* if the transfer thread is the current thread, just return */
#ifdef RTJ_DEBUG_THREADS
  printf("\nTransfer from %p to %p", currentThread, mthread);
  fflush(NULL);
#endif
  if(mthread == currentThread) {
    settimer();
    return;
  }

  /* if there is currently a thread running, save it's state */
  if(currentThread != NULL) {
    machdep_save_float_state(currentThread->mthread);
    if (machdep_save_state(currentThread->mthread)) {
#ifdef RTJ_DEBUG_THREADS
      printf(" returning directly!");
      fflush(NULL);
#endif
      return;
    }
  }
#endif

#ifndef WITH_REALTIME_THREADS
  tl=gtl;
  tl->prev->next=tl->next;
  tl->next->prev=tl->prev;
  if (tl!=tl->next) {
    gtl=gtl->next;
  } else {
    gtl=NULL;
  }
  
  if (ioptr!=NULL) {
    tl->next=ioptr;
    tl->prev=ioptr->prev;
    tl->next->prev=tl;
    tl->prev->next=tl;
  } else {
    ioptr=tl;
    tl->next=tl;
    tl->prev=tl;
  }
  startnext();
#else
  currentThread = mthread; //set the transfer thread to be the current thread
  restorethread(); //switch to it#
#endif
}

#ifndef WITH_REALTIME_THREADS
void context_switch() {
  machdep_save_float_state(&(gtl->mthread));
  if (machdep_save_state(&(gtl->mthread))) {
    return;
  }
  if (ioptr!=NULL)
    {
      doFDs();
    }
  gtl=gtl->next;

  startnext();
}
#endif

void startnext() {
#ifndef WITH_REALTIME_THREADS
  /* Moved threads...*/
  /*  if (gtl==NULL) */
#ifdef WITH_EVENT_DRIVEN
  static int count=0;
#endif

  while(1) {
    if ((gtl==NULL)&&(ioptr==NULL))
      exit(0);
#ifdef WITH_EVENT_DRIVEN
    count++;
    if ((gtl==NULL)||((ioptr!=NULL)&&(count==10))) { 
      count=0;
      doFDs();
    } 
#endif
    if (gtl!=NULL)
      restorethread();
  }
#endif
}

#ifdef WITH_EVENT_DRIVEN
void SchedulerAddRead(int fd) {
  /* Move current thread to IO queue */
  /* Lock on GTL */
  Java_java_io_NativeIO_registerRead(NULL,NULL, fd);
  gtl->syncrdwr=1;
  gtl->fd=fd;
#ifndef WITH_REALTIME_THREADS  
  transfer();
#else
  transfer(currentThread); //just transfer to self
#endif
}

void SchedulerAddWrite(int fd) {
  /* Move current thread to IO queue */
  /* Lock on GTL */
  Java_java_io_NativeIO_registerWrite(NULL,NULL, fd);
  gtl->syncrdwr=2;
  gtl->fd=fd;
#ifndef WITH_REALTIME_THREADS  
  transfer();
#else
  transfer(currentThread); //just transfer to self
#endif
}

void doFDs() {
  int * fd=(gtl==NULL)?getFDsintSEL(1):getFDsintSEL(0);/*1 for no timeout*/
  struct thread_list *tl=ioptr,*tmp;
  int start=0;
  while((tl!=ioptr)||(start==0)) {
    int filestat=(tl->syncrdwr>0)?fd[tl->fd]:0;
    start=1;
    if (((filestat&1)&&(tl->syncrdwr==1))||
      ((filestat&2)&&(tl->syncrdwr==2))) {
      /*Make it active!*/
      /*Remove from io queue*/
      tmp=tl->next;
      tl->next->prev=tl->prev;
      tl->prev->next=tmp;

      /* Add to execute queue */
      if (gtl==NULL) {
	tl->next=tl;
	tl->prev=tl;
	gtl=tl;
      } else {
	tl->next=gtl;
	tl->prev=gtl->prev;
	tl->next->prev=tl;
	tl->prev->next=tl;
      }
     
      if (ioptr==tl) {
	if (ioptr==tmp) {
	  /*Only element removed*/
	  ioptr=NULL;
	  break;
	} else {
	  /*First element removed*/
	  ioptr=tmp;
	  start=0;
	}
      }
      tl=tmp;
    } else
      tl=tl->next;
  }
#ifdef WITH_REALTIME_JAVA
  RTJ_FREE(fd);
#else
  free(fd);
#endif
}
#endif

extern void FNI_DestroyThreadState(JNIEnv* env);

void exitthread() {
#ifndef WITH_REALTIME_THREADS
  struct thread_list *tl;
  /*LOCK ON GTL*/
  if (gtl!=gtl->next) {
    FNI_DestroyThreadState(gtl->jnienv);
    tl=gtl;
    gtl = gtl->next;
    gtl->prev=tl->prev;
    gtl->prev->next=gtl;

    /*perhaps free structures if we didn't have GC*/

    /*NEED LOCK AROUND THIS*/
    if (oldstack!=NULL) {
      __machdep_stack_free(oldstack);
      oldstack=NULL;
    }
    oldstack=tl->mthread.machdep_stack;

#ifdef WITH_REALTIME_JAVA
    RTJ_FREE(tl);
#else
    free(tl);
#endif
    DECREMENT_MEM_STATS(sizeof (struct thread_list));

    restorethread();
    return;
  } else {
    FNI_DestroyThreadState(gtl->jnienv);
    if (oldstack!=NULL) {
      __machdep_stack_free(oldstack);
      oldstack=NULL;
    }
    oldstack=gtl->mthread.machdep_stack;

#ifdef WITH_REALTIME_JAVA
    RTJ_FREE(gtl);
#else
    free(gtl);
#endif

    DECREMENT_MEM_STATS(sizeof (struct thread_list));
    gtl=NULL;
    startnext();
  }

#else // WITH_REALTIME_THREADS
  /* if no new thread has been set (by the end of thread_startup_routine) */
  //  puts("----------EXITING-------------");
  //  printf("CurrentThread is %p\n", currentThread);
  if(currentThread == NULL)
    longjmp(main_return_jump, 1); //jump back to main to clean up and end
  else {
#ifdef RTJ_DEBUG_THREADS
    printf("\nPrevious thread is dead, switching to %p", currentThread);
    fflush(NULL);
#endif
    restorethread(); //otherwise, restore the new thread
  }
#endif

}

void inituser(int *bottom) {
#ifdef WITH_REALTIME_JAVA
  struct thread_list * tl = 
    (struct thread_list *)RTJ_CALLOC_UNCOLLECTABLE(sizeof(struct thread_list), 1);
#else
  struct thread_list * tl =
    (struct thread_list *)malloc(sizeof(struct thread_list));
#endif  
#ifdef GLIBC_COMPAT5
  glibc_compat5();
#endif
#ifndef WITH_REALTIME_THREADS
  INCREMENT_MEM_STATS(sizeof(struct thread_list));
  /*build stack and stash it*/
  __machdep_pthread_create(&(tl->mthread), NULL, NULL,STACKSIZE, 0,0);
  /*stash hiptr*/
  tl->mthread.hiptr=bottom;
#else
  /*build stack and stash it*/
  currentThread = 
    (struct thread_queue_struct *)RTJ_CALLOC_UNCOLLECTABLE(sizeof(struct thread_queue_struct),1);
  INCREMENT_MEM_STATS(sizeof(struct thread_queue_struct));
  currentThread->mthread =
    (struct machdep_pthread*)RTJ_CALLOC_UNCOLLECTABLE(sizeof(struct machdep_pthread),1);
  __machdep_pthread_create(currentThread->mthread, NULL, NULL,STACKSIZE, 0,0);
  /*stash hiptr*/
  currentThread->mthread->hiptr=bottom;
#endif
  gtl=tl;
  tl->next=tl;
  tl->prev=tl;
  ioptr=NULL; /* Initialize ioptr */
  oldstack=NULL;

#ifdef WITH_EVENT_DRIVEN
  Java_java_io_NativeIO_initScheduler(NULL,NULL,/*MOD_SELECT*/0);
#endif
}

int user_mutex_init(user_mutex_t *x, void * v) {
#ifdef WITH_REALTIME_THREADS
  if (handlerMask&javax_realtime_Scheduler_HANDLE_MUTEX_INIT) {
    JNIEnv* env = FNI_GetJNIEnv();
    (*env)->CallStaticVoidMethod(env, SchedulerClaz, Scheduler_handle_mutex_init);
#ifdef WITH_REALTIME_THREADS_DEBUG
    fflush(NULL);
    assert(!((*env)->ExceptionOccurred(env)));
#endif
  }
#endif
  x->mutex=SEMAPHORE_CLEAR;
#ifndef WITH_REALTIME_THREADS
  x->tl=NULL;
#else
  x->queue = x->endQueue = NULL;
#endif
  return 0;
}

void addwaitthread(user_mutex_t *x) {
#ifndef WITH_REALTIME_THREADS  
  gtl->syncrdwr=0;
  gtl->lnext=x->tl;
  x->tl=gtl;
  transfer();
#else
  if(currentThread->threadID != 0) {
    struct thread_queue_struct* q = RTJ_CALLOC_UNCOLLECTABLE(sizeof(struct thread_queue_struct),1);
#ifdef WITH_REALTIME_THREADS_DEBUG
    print_queue(thread_queue, "addwaitthread queue");
    fflush(NULL);
    print_queue(x->queue, "BEG mutex");
    fflush(NULL);
#endif

    q->threadID = currentThread->threadID;
    q->jthread = currentThread->jthread;
    q->next = x->queue;
    x->queue = q;

#ifdef WITH_REALTIME_THREADS_DEBUG
    print_queue(x->queue, "END mutex");    
    fflush(NULL);
#endif
    DisableThread(currentThread);
  }
#endif
}

void wakewaitthread(user_mutex_t *x) {
#ifndef WITH_REALTIME_THREADS
  struct thread_list *tl;

  tl=x->tl;
  if (tl!=NULL) {
    x->tl=tl->lnext;
    /*Remove from previous list*/
    tl->prev->next=tl->next;
    tl->next->prev=tl->prev;
    if (ioptr==tl) {
      if (tl->next==tl)
	ioptr=NULL;
      else
	ioptr=tl->next;
    }
    tl->next=gtl->next;
    tl->prev=gtl;
    tl->prev->next=tl;
    tl->next->prev=tl;
  }
#else
  if(x->queue != NULL) {
    struct thread_queue_struct* q = x->queue;
#ifdef WITH_REALTIME_THREADS_DEBUG
    print_queue(thread_queue, "wakewaitthread queue");
    fflush(NULL);
    print_queue(x->queue, "BEG mutex");
    fflush(NULL);
#endif

    EnableThread(q);
    x->queue = x->queue->next;
    RTJ_FREE(q);
#ifdef WITH_REALTIME_THREADS_DEBUG
    print_queue(x->queue, "END mutex");
    fflush(NULL);
#endif
    
  }
#endif  
}

int user_mutex_lock(user_mutex_t *x) {
#ifdef WITH_REALTIME_THREADS
  if (handlerMask&javax_realtime_Scheduler_HANDLE_MUTEX_LOCK) {
    JNIEnv* env = FNI_GetJNIEnv();
    (*env)->CallStaticVoidMethod(env, SchedulerClaz, Scheduler_handle_mutex_lock);
#ifdef WITH_REALTIME_THREADS_DEBUG
    fflush(NULL);
    assert(!((*env)->ExceptionOccurred(env)));
#endif
  }
#endif
  while(SEMAPHORE_TEST_AND_SET(&(x->mutex))==SEMAPHORE_SET) {
    addwaitthread(x);
  }
  return 0;
}

int user_mutex_trylock(user_mutex_t *x) {
#ifdef WITH_REALTIME_THREADS
  if (handlerMask&javax_realtime_Scheduler_HANDLE_MUTEX_TRYLOCK) {
    JNIEnv* env = FNI_GetJNIEnv();
    (*env)->CallStaticVoidMethod(env, SchedulerClaz, Scheduler_handle_mutex_trylock);
#ifdef WITH_REALTIME_THREADS_DEBUG
    fflush(0);
    assert(!((*env)->ExceptionOccurred(env)));
#endif
  }
#endif
  if ((SEMAPHORE_TEST_AND_SET(&(x->mutex)))==SEMAPHORE_CLEAR) {
    return 0;
  } else {
#ifndef WITH_REALTIME_THREADS
    // This is a tricky one -
    // Without this, bdemsky's user threads can livelock in the
    // following code: 
    // while (try_lock) do stuff;
    context_switch();
#else
    // But with RTJ, we need to guarantee a thread's quanta as much as
    // possible.  Therefore, RTJ may need to switch when the quanta is up or 
    // when the thread is blocked - but at NO OTHER TIME.
    //
    // Perhaps the scheduler should be informed of this to make
    // wise scheduling policy decisions - but I'm gonna wait until 
    // I put the scheduler lock informing code in place...

#endif
    return EBUSY;
  }
}

int user_mutex_unlock(user_mutex_t *x) {
#ifdef WITH_REALTIME_THREADS
  if (handlerMask&javax_realtime_Scheduler_HANDLE_MUTEX_UNLOCK) {
    JNIEnv* env = FNI_GetJNIEnv();
    (*env)->CallStaticVoidMethod(env, SchedulerClaz, Scheduler_handle_mutex_unlock);
#ifdef WITH_REALTIME_THREADS_DEBUG
    fflush(NULL);
    assert(!((*env)->ExceptionOccurred(env)));
#endif
  }
#endif
  SEMAPHORE_RESET(&(x->mutex));
  wakewaitthread(x);
  return 0;
}

int user_mutex_destroy(user_mutex_t *x) {
#ifdef WITH_REALTIME_THREADS
  if (handlerMask&javax_realtime_Scheduler_HANDLE_MUTEX_DESTROY) {
    JNIEnv* env = FNI_GetJNIEnv();
    (*env)->CallStaticVoidMethod(env, SchedulerClaz, Scheduler_handle_mutex_destroy);
#ifdef WITH_REALTIME_THREADS_DEBUG
    fflush(NULL);
    assert(!((*env)->ExceptionOccurred(env)));
#endif
  }
#endif
  return 0;
}

int user_cond_init(user_cond_t *x, void * v) {
#ifdef WITH_REALTIME_THREADS
  if (handlerMask&javax_realtime_Scheduler_HANDLE_COND_INIT) {
    JNIEnv* env = FNI_GetJNIEnv();
    (*env)->CallStaticVoidMethod(env, SchedulerClaz, Scheduler_handle_cond_init);
#ifdef WITH_REALTIME_THREADS_DEBUG
    fflush(NULL);
    assert(!((*env)->ExceptionOccurred(env)));
#endif
  }
#endif
  x->counter=0;
  return 0;
}

int user_cond_broadcast(user_cond_t *x) {
#ifdef WITH_REALTIME_THREADS
  if (handlerMask&javax_realtime_Scheduler_HANDLE_COND_BROADCAST) {
    JNIEnv* env = FNI_GetJNIEnv();
    (*env)->CallStaticVoidMethod(env, SchedulerClaz, Scheduler_handle_cond_broadcast);
#ifdef WITH_REALTIME_THREADS_DEBUG
    fflush(NULL);
    assert(!((*env)->ExceptionOccurred(env)));
#endif
  }
#endif
  wakeallcondthread(x);
  return 0;
}

int user_cond_signal(user_cond_t *x) {
#ifdef WITH_REALTIME_THREADS
  if (handlerMask&javax_realtime_Scheduler_HANDLE_COND_SIGNAL) {
    JNIEnv* env = FNI_GetJNIEnv();
    (*env)->CallStaticVoidMethod(env, SchedulerClaz, Scheduler_handle_cond_signal);
#ifdef WITH_REALTIME_THREADS_DEBUG
    fflush(NULL);
    assert(!((*env)->ExceptionOccurred(env)));
#endif
  }
#endif
  wakeacondthread(x);
  return 0;
}

void addcontthread(user_cond_t *x) {
#ifndef WITH_REALTIME_THREADS  
  gtl->syncrdwr=0;
  gtl->lnext=x->tl;
  x->tl=gtl;
  transfer();
#else
  if(currentThread->threadID != 0) {
    struct thread_queue_struct* q = RTJ_CALLOC_UNCOLLECTABLE(sizeof(struct thread_queue_struct),1);
#ifdef WITH_REALTIME_THREADS_DEBUG
    print_queue(thread_queue, "addcontthread queue");
    fflush(NULL);
    print_queue(x->queue, "BEG cond");
    fflush(NULL);
#endif

    q->threadID = currentThread->threadID;
    q->jthread = currentThread->jthread;
    q->next = x->queue;
    x->queue = q;
    
#ifdef WITH_REALTIME_THREADS_DEBUG
    print_queue(x->queue, "END cond");
    fflush(NULL);
#endif
    DisableThread(currentThread);
  }
#endif
}

void wakeacondthread(user_cond_t *x) {
#ifndef WITH_REALTIME_THREADS
  struct thread_list *tl;
  tl=x->tl;
  if (tl!=NULL) {
    x->tl=tl->lnext;
    /*Remove from previous list*/
    tl->prev->next=tl->next;
    tl->next->prev=tl->prev;
    if (ioptr==tl) {
      if (tl->next==tl)
	ioptr=NULL;
      else
	ioptr=tl->next;
    }

    tl->next=gtl->next;
    tl->prev=gtl;
    tl->prev->next=tl;
    tl->next->prev=tl;
  }
#else
  if(x->queue != NULL) {
    struct thread_queue_struct* q = x->queue;
#ifdef WITH_REALTIME_THREADS_DEBUG
    print_queue(thread_queue, "wakeacondthread queue");
    fflush(NULL);
    print_queue(x->queue, "BEG cond");
    fflush(NULL);
#endif

    EnableThread(q);
    x->queue = x->queue->next;
    RTJ_FREE(q);
    
#ifdef WITH_REALTIME_THREADS_DEBUG
    print_queue(x->queue, "END cond");
    fflush(NULL);
#endif
  }
#endif
}

void wakeallcondthread(user_cond_t *x) {
#ifndef WITH_REALTIME_THREADS
  struct thread_list *tl;
  tl=x->tl;
  while (tl!=NULL) {
    x->tl=tl->lnext;
    /*Remove from previous list*/
    tl->prev->next=tl->next;
    tl->next->prev=tl->prev;
    if (ioptr==tl) {
      if (tl->next==tl)
	ioptr=NULL;
      else
	ioptr=tl->next;
    }

    tl->next=gtl->next;
    tl->prev=gtl;
    tl->prev->next=tl;
    tl->next->prev=tl;
    tl=tl->lnext;
  }
#else
  if(x->queue != NULL) {
    struct thread_queue_struct* q;
#ifdef WITH_REALTIME_THREADS_DEBUG
    print_queue(thread_queue, "wakeallcondthread queue");
    fflush(NULL);
    print_queue(x->queue, "BEG cond");
    fflush(NULL);
#endif
  
    EnableThreadList(x->queue);
    while ((q = x->queue) != NULL) {
      x->queue = x->queue->next;
      RTJ_FREE(q);
    }

#ifdef WITH_REALTIME_THREADS_DEBUG
    print_queue(x->queue, "END cond");
    fflush(NULL);
#endif
  }
#endif
}

int user_cond_wait(user_cond_t *cond, user_mutex_t *mutex) {
#ifdef WITH_REALTIME_THREADS
  if (handlerMask&javax_realtime_Scheduler_HANDLE_COND_WAIT) {
    JNIEnv* env = FNI_GetJNIEnv();
    (*env)->CallStaticVoidMethod(env, SchedulerClaz, Scheduler_handle_cond_wait);
#ifdef WITH_REALTIME_THREADS_DEBUG
    fflush(NULL);
    assert(!((*env)->ExceptionOccurred(env)));
#endif
  }
#endif
  /*Only one thread so this will work...*/
  user_mutex_unlock(mutex);
  /*Previous 2 lines need to be atomic!!!!*/
  addcontthread(cond);
  /*grab the lock again*/
  user_mutex_lock(mutex);
  return 0;
}

int user_cond_timedwait(user_cond_t *cond, user_mutex_t *mutex, const struct timespec *abstime) {
#ifdef WITH_REALTIME_THREADS
  if (handlerMask&javax_realtime_Scheduler_HANDLE_COND_TIMEDWAIT) {
    JNIEnv* env = FNI_GetJNIEnv();
    (*env)->CallStaticVoidMethod(env, SchedulerClaz, Scheduler_handle_cond_timedwait);
#ifdef WITH_REALTIME_THREADS_DEBUG
    fflush(NULL);
    assert(!((*env)->ExceptionOccurred(env)));
#endif
  }
#endif
  return user_cond_wait(cond,mutex);
}

int user_cond_destroy(user_cond_t *cond) {
#ifdef WITH_REALTIME_THREADS
  if (handlerMask&javax_realtime_Scheduler_HANDLE_COND_DESTROY) {
    JNIEnv* env = FNI_GetJNIEnv();
    (*env)->CallStaticVoidMethod(env, SchedulerClaz, Scheduler_handle_mutex_lock);
#ifdef WITH_REALTIME_THREADS_DEBUG
    fflush(NULL);
    assert(!((*env)->ExceptionOccurred(env)));
#endif
  }
#endif
  return 0;
}
#endif
