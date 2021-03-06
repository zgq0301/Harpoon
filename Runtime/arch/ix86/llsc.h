/* Macros to access the load linked and store conditional instructions
 * on this architecture.
 *  C. Scott Ananian <cananian@alumni.princeton.edu>
 */
#ifndef INCLUDED_LLSC_H
#define INCLUDED_LLSC_H

#warning x86 does not have load linked/store conditional instructions
#warning generated code will NOT be thread safe.

static inline int32_t load_linked(volatile int32_t *ptr) {
  return *ptr;
}
static inline int store_conditional(volatile int32_t *ptr, int32_t val) {
  *ptr = val;
  return 1;
}
static inline int64_t load_linked_double(volatile int64_t *ptr) {
  return *ptr;
}
static inline int store_conditional_double(volatile int64_t *ptr, int64_t val){
  *ptr = val;
  return 1;
}
static inline void sync(void) {
  /* do nothing */
}

#define LL(x) \
({ typeof(x) _ptr =  (x); typeof(*_ptr) _result;			\
   (typeof(_result))							\
     __builtin_choose_expr(sizeof(_result)<=sizeof(int32_t),		\
			   load_linked((volatile int32_t*)_ptr),	\
			   load_linked_double((volatile int64_t*)_ptr));\
})
// __builtin_choose_expr still (as of gcc 3.3) gives warnings on the
// unevaluated portion of the function, which means that a SC() function
// written like the LL() function above gives warnings on every use
// about the pointer-to-integer conversion on the "wrong" side of the
// branch. <grumble>  So let the architecture define SC_PTR appropriately.
#define SC(x,y) store_conditional(x,y)
#define SC_PTR(x,y) \
({ typeof(x) _ptr =  (x); typeof(*_ptr) _val = (y);			\
   store_conditional((volatile int32_t*)_ptr, (int32_t)_val);		\
})
#define SC_D(x,y) store_conditional_double(x,y)


#endif /* INCLUDED_LLSC_H */
