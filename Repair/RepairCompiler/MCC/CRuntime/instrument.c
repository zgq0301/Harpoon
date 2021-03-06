/* Defines interfaces for the applications and exports function calls that
   the applications should use instead of the standard ones. */

#include <stdlib.h>
#include <sys/time.h>

#include "instrument.h"
#include <stdio.h>
#include "tmap.h"
#include "size.h"
#include <string.h>

struct typemap * memmap;

void *ourcalloc(size_t nmemb, size_t size) {
  void *oc=calloc(nmemb,size);
  typemapallocate(memmap, oc,size*nmemb);
  return oc;
}

void *ourmalloc(size_t size) {
  void *oc=malloc(size);
  typemapallocate(memmap, oc,size);
  return oc;
}

void ourfree(void *ptr) {
  if (ptr!=NULL) {
    typemapdeallocate(memmap, ptr);
  }
  free(ptr);
}

void *ourrealloc(void *ptr, size_t size) {
  void *orr=realloc(ptr,size);
  if (size==0) {
    typemapdeallocate(memmap, ptr);
    return orr;
  }
  if (orr==NULL) {
    return orr;
  }
  typemapdeallocate(memmap, ptr);
  typemapallocate(memmap, orr,size);
  return orr;
}

char *ourstrdup(const char *ptr) {
  char *new_ptr=strdup(ptr);
  int length=strlen(ptr)+1;
  typemapallocate(memmap,new_ptr,length);
  return new_ptr;
}


void alloc(void *ptr,int size) {
  typemapallocate(memmap, ptr,size);
}

void dealloc(void *ptr) {
  typemapdeallocate(memmap, ptr);
}

void initializemmap() {
  memmap=allocatetypemap();
}

void resettypemap() {
  typemapreset(memmap);
}

bool assertvalidtype(int ptr, int structure) {
  return typemapasserttype(memmap, (void *)ptr, structure);
}
bool assertvalidmemory(int ptr, int structure) {
  return typemapassertvalidmemory(memmap, (void *)ptr, structure);
}

bool assertexactmemory(int ptr, int structure) {
  return typemapassertexactmemory(memmap, (void *)ptr, structure);
}

void * getendofblock(int ptr) {
  return typemapgetendofblock(memmap, (void *)ptr);
}

void initializestack(void *high) {
  initializetypemapstack(memmap, high);
}
