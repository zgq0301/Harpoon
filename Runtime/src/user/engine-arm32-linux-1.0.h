/* ==== machdep.h ============================================================
 * Copyright (c) 1993 Chris Provenzano, proven@athena.mit.edu
 *
 * $Id: engine-arm32-linux-1.0.h,v 1.1 2000-12-04 19:18:02 bdemsky Exp $
 */
#ifndef MACHDEP
#define MACHDEP

#include <unistd.h>
#include <setjmp.h>
#include <sys/time.h>
#include <limits.h>

/*
 * The first machine dependent functions are the SEMAPHORES
 * needing the test and set instruction.
 */
#define SEMAPHORE_CLEAR 0
#define SEMAPHORE_SET   1

#define SEMAPHORE_TEST_AND_SET(lock)    \
({										\
volatile long temp = SEMAPHORE_SET;     \
										\
__asm__("swp %0,%0,[%2]"                 \
        :"=r" (temp)                    \
        :"0" (temp),"r" (lock):"memory");        \
temp;                                   \
})

#define SEMAPHORE_RESET(lock)           *lock = SEMAPHORE_CLEAR

/*
 * New types
 */
typedef long    semaphore;

#define SIGMAX	31

/*
 * New Strutures
 */
struct machdep_pthread {
    void        		*(*start_routine)(void *);
    void        		*start_argument;
    void        		*machdep_stack;
    void                        *hiptr;
	struct itimerval	machdep_timer;
    jmp_buf     		machdep_state;
  /*  char 	    		machdep_float_state[108];*/
    int                         started;
};

/*
 * Static machdep_pthread initialization values.
 * For initial thread only.
 */
#define MACHDEP_PTHREAD_INIT    \
{ NULL, NULL, NULL, { { 0, 0 }, { 0, 100000 } }, 0 }

/*
 * Minimum stack size
 */
#ifndef PTHREAD_STACK_MIN
#define PTHREAD_STACK_MIN	1024
#endif

/*
 * sigset_t macros
 */
#define	SIG_ANY(sig)		(sig)

/*
 * Some fd flag defines that are necessary to distinguish between posix
 * behavior and bsd4.3 behavior.
 */
#define __FD_NONBLOCK 		O_NONBLOCK

/*
 * New functions
 */

__BEGIN_DECLS


#define __machdep_stack_get(x)      (x)->machdep_stack
#define __machdep_stack_set(x, y)   (x)->machdep_stack = y
#define __machdep_stack_repl(x, y)                          \
{                                                           \
    if (stack = __machdep_stack_get(x)) {                   \
        __machdep_stack_free(stack);                        \
    }                                                       \
    __machdep_stack_set(x, y);                              \
}

void *  __machdep_stack_alloc       __P((size_t));
void    __machdep_stack_free        __P((void *));

int 	machdep_save_state      __P((void));


__END_DECLS
#endif /*MACHDEP*/
