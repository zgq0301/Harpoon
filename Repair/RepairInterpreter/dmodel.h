// defines the sets and the relations used

#ifndef DMODEL_H
#define DMODEL_H

#include "common.h"
#include "classlist.h"

#define DOMAINSET_SUBSET 1
#define DOMAINSET_PARTITION 2
#define DOMAINSET_TYPED 0x100

// represents a set
class DomainSet {
 public:
  DomainSet(char *name);
  void settype(char *type);
  void setsubsets(char **subsets, int numsubsets);
  void setpartition(char **subsets, int numsubsets);
  void print();
  char *getname();
  WorkSet *getset();
  int getnumsubsets();
  char *getsubset(int i);
  int gettype();
  char * getelementtype();
  void reset();
 private:
  WorkSet *set;   // the set itself
  char *type;     // the type of the elements in the set 
  char *setname;  // the name of the set
  int flag; 
  char ** subsets;// the subsets
  int numsubsets; 
};




#define DRELATION_SINGDOMAIN 0x1
#define DRELATION_MANYDOMAIN 0x2
#define DRELATION_SINGRANGE 0x10
#define DRELATION_MANYRANGE 0x20

// represents a relation
class DRelation {
 public:
  DRelation(char *name, char *d, char *r, int t, bool);
  void print();
  char *getname();
  WorkRelation *getrelation();
  char *getdomain();
  char *getrange();
  WorkSet *gettokenrange();
  void settokenrange(WorkSet *ws);
  bool isstatic();
  void reset();
 private:
  bool staticrel;
  char *name;
  char *domain;
  char *range;
  WorkSet *tokenrange; // the actual range, if the range is of type token
  int type;
  WorkRelation *relation;
};



// manages the entire collection of sets and relations
class DomainRelation {
 public:
  DomainRelation(DomainSet **s, int ns, DRelation **r,int nr);
  void print();
  DomainSet * getset(char * setname);
  DRelation * getrelation(char * relationname);
  ~DomainRelation();
  WorkSet * conflictdelsets(char *setname, char *boundset);
  WorkSet * conflictaddsets(char *setname, char *boundset, model *m);
  WorkSet * removeconflictdelsets(char *setname);
  WorkSet * removeconflictaddsets(char *setname, model *m);
  DomainSet * getsuperset(DomainSet *);
  DomainSet * getsource(DomainSet *);
  /* Tells what set we might get objects from for a given set */
  void delfromsetmovetoset(Element *e,DomainSet *deletefromset,model *m);
  void abstaddtoset(Element *e,DomainSet *addtoset,model *m);
  void addtoset(Element *e,DomainSet *addtoset,model *m);
  bool issupersetof(DomainSet *sub,DomainSet *super);
  int getnumrelation();
  DRelation * getrelation(int i);
  bool fixstuff();
  void reset();

 private:
  bool checkrelations(DRelation *dr);
  bool checksubset(DomainSet *ds);
  void addallsubsets(DomainSet *ds, WorkSet *ws);
  void removefromthisset(Element *ele, DomainSet *ds,model *m);
  Hashtable *settable, *relationtable;
  DomainSet ** sets;
  int numsets;
  DRelation **relations;
  int numrelations;
};
#endif
