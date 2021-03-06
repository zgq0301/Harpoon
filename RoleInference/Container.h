#ifndef CONTAINER
#define CONTAINER
#include "RoleInference.h"

struct policyobject {
  int policy;
  /* 0=Never 
   * 1=once ever
   * 2=one at a time
   * 3=always*/
  
};

void examineheap(struct heap_state *hs, struct hashtable *ht);
void opencontainerfile(struct heap_state *hs);
void closecontainerfile(struct heap_state *hs);
void recordcontainer(struct heap_state *hs, struct heap_object *ho);
void openreadcontainer(struct heap_state *hs);
void readpolicyfile(struct heap_state *heap, char *filename);
#endif
