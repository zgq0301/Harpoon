#ifndef SET_H
#define SET_H

#include "classlist.h"

class WorkSet {
 public:
  WorkSet();
  WorkSet(unsigned int (*hashf)(void *),int (*equals)(void *,void *));
  WorkSet(bool);
  ~WorkSet();
  void addobject(void *obj);
  void removeobject(void *obj);
  bool contains(void *obj);
  Iterator * getiterator();
  void *firstelement();
  void * getnextelement(void *src);
  int size();
  void * getelement(int i);  
  bool isEmpty();
  void print();

 private:
  struct genhashtable *ght;
};

class Iterator {
 public:
  Iterator(struct genhashtable *ght);
  ~Iterator();
  void * next();

 private:
  struct geniterator * gi;
};

#endif

