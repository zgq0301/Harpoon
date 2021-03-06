#include "config.h"

#ifdef WITH_LIVE_HEAP_STATISTICS

#ifndef BDW_CONSERVATIVE_GC
# error heapstats should only be compiled with BDW_CONSERVATIVE_GC
#endif
#ifndef WITH_STATISTICS
# error heapstats should only be compiled WITH_STATISTICS
#endif

#include <jni.h>
#include <jni-private.h> /* for ptroff_t */
#include <stdlib.h>
#ifdef BDW_CONSERVATIVE_GC
#include "gc.h"
#endif
#include "flexthread.h" /* for stand-alone locks */
#include "fni-stats.h" /* for stats-related macros */

DECLARE_STATS_EXTERN(heap_current_live_arr_bytes)
DECLARE_STATS_EXTERN(heap_max_live_arr_bytes)
DECLARE_STATS_EXTERN(heap_total_alloc_arr_bytes)
DECLARE_STATS_EXTERN(heap_total_alloc_arr_count)

DECLARE_STATS_EXTERN(heap_current_live_obj_bytes)
DECLARE_STATS_EXTERN(heap_max_live_obj_bytes)
DECLARE_STATS_EXTERN(heap_total_alloc_obj_bytes)
DECLARE_STATS_EXTERN(heap_total_alloc_obj_count)

/* how many allocations to allow before a GC */
static int GC_FREQUENCY=100;

/* allow GC_FREQUENCY to be modified by an env. variable.  You can also
 * reset GC_FREQUENCY in a debugger. MAX_LIVE_ESTIMATE can also be
 * set by an env variable, for faster statistics when a valid
 * conservatively-low estimate of heap usage is known. */
static void init_parameters(void) __attribute__ ((constructor));
static void init_parameters(void) {
  char *freq, *est; int e=0;
  /* set GC_FREQUENCY from environment. */
  freq = getenv("GC_FREQUENCY");
  if (freq!=NULL) GC_FREQUENCY=atoi(freq);
  if (GC_FREQUENCY<=0) GC_FREQUENCY=1;
  /* set 'estimated max_live_bytes' from environment.  Make sure you
   * err on the 'too low' side!  This eliminates all GC until you
   * reach the 'estimated' heap size, for faster runs. */
  est = getenv("MAX_LIVE_ESTIMATE");
  if (est!=NULL) e=atoi(est);
  if (e<=0) e=0;
  UPDATE_STATS(heap_max_live_arr_bytes, e);
  UPDATE_STATS(heap_max_live_obj_bytes, e);
}

static void heapstats_finalizer_object(GC_PTR obj, GC_PTR _size_) {
  ptroff_t size = (ptroff_t) _size_;
  /* decrease the size of the live set */
  INCREMENT_STATS(heap_current_live_obj_bytes, -size);
}
static void heapstats_finalizer_array(GC_PTR obj, GC_PTR _size_) {
  ptroff_t size = (ptroff_t) _size_;
  /* decrease the size of the live set */
  INCREMENT_STATS(heap_current_live_arr_bytes, -size);
}

void *heapstats_alloc2(jsize length, jint isArray) {
  void *result;
  stat_t ttl;
  static int skipped=0;
  FLEX_MUTEX_DECLARE_STATIC(skipped_lock);
  /* do the allocation & register finalizer */
  result = GC_malloc(length);
  GC_register_finalizer_no_order(result, isArray ?
				 heapstats_finalizer_array :
				 heapstats_finalizer_object,
				 (GC_PTR) ((ptroff_t) length), NULL, NULL);
  /* (sometimes) collect all dead objects */
  FLEX_MUTEX_LOCK(&skipped_lock);
  if (skipped ||
      0 == ((FETCH_STATS(heap_total_alloc_obj_count) +
	     FETCH_STATS(heap_total_alloc_arr_count)) % GC_FREQUENCY))
    if (isArray ?
	(FETCH_STATS(heap_current_live_arr_bytes)+length)
	> FETCH_STATS(heap_max_live_arr_bytes) :
	(FETCH_STATS(heap_current_live_obj_bytes)+length)
	> FETCH_STATS(heap_max_live_obj_bytes)) {
      GC_gcollect(); skipped = 0;
    } else skipped = 1;
  FLEX_MUTEX_UNLOCK(&skipped_lock);
  if (isArray) {
    /* update total and current live */
    INCREMENT_STATS(heap_total_alloc_arr_count, 1);
    INCREMENT_STATS(heap_total_alloc_arr_bytes, length);
    INCREMENT_STATS(heap_current_live_arr_bytes, length);
    /* update max_live */
    ttl = FETCH_STATS(heap_current_live_arr_bytes);
    UPDATE_STATS(heap_max_live_arr_bytes,
		 ttl > _old_value_ ? ttl : _old_value_);
  } else {
    /* update total and current live */
    INCREMENT_STATS(heap_total_alloc_obj_count, 1);
    INCREMENT_STATS(heap_total_alloc_obj_bytes, length);
    INCREMENT_STATS(heap_current_live_obj_bytes, length);
    /* update max_live */
    ttl = FETCH_STATS(heap_current_live_obj_bytes);
    UPDATE_STATS(heap_max_live_obj_bytes,
		 ttl > _old_value_ ? ttl : _old_value_);
  }
  /* done */
  return result;
}

/* for backwards-compatibility */
void *heapstats_alloc(jsize length) {
  return heapstats_alloc2(length, 0);
}
#endif /* WITH_LIVE_HEAP_STATISTICS */
