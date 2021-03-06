#ifndef INCLUDED_GENERATIONAL_H
#define INCLUDED_GENERATIONAL_H

#ifdef WITH_GENERATIONAL_GC
void add_to_curr_obj_list(jobject_unwrapped aligned);

void find_generational_refs();

void generational_collect();

jlong generational_free_memory();

void generational_gc_init();

jlong generational_get_heap_size();

void generational_handle_reference(jobject_unwrapped *ref, int wbtype);

void *generational_malloc (size_t size_in_bytes);

void generational_register_inflated_obj(jobject_unwrapped obj);

int in_young_gen(jobject_unwrapped obj);

int in_old_gen(jobject_unwrapped obj);
#endif /* WITH_GENERATIONAL_GC */

#endif /* INCLUDED_GENERATIONAL_H */
