#ifndef INCLUDED_FNI_CLASS_H
#define INCLUDED_FNI_CLASS_H

#include "config.h" /* for WITH_INIT_CHECK, WITH_TRANSACTIONS */
#include <jni.h>
#include <jni-private.h>
#include "../reflect-thunk/java_lang_reflect_Modifier.h"
#ifdef WITH_TRANSACTIONS
# include "../transact/transact.h"
#endif

#include <assert.h>
#include <string.h> /* strlen, strcpy */
#include <strings.h> /* rindex */
#include "../java.lang.reflect/reflect-util.h" /* REFLECT_* */


static inline
jboolean fni_class_isPrimitive(JNIEnv *env, jclass cls) {
    struct claz *thisclz = FNI_GetClassInfo(cls)->claz;
    /* primitives have null in the first slot of the display. */
    if (thisclz->display[0]!=NULL) return JNI_FALSE;
    /* but so do interfaces.  weed them out using the interface list. */
    if (*(thisclz->interfaces)!=NULL) return JNI_FALSE;
    return JNI_TRUE;
}


// try to wrap currently active exception as the exception specified by
// the exclsname parameter.  if this fails, just throw the original exception.
static
void wrapNthrow(JNIEnv *env, char *exclsname) {
    jthrowable ex = (*env)->ExceptionOccurred(env), nex;
    jobject descstr;
    jclass exCls, nexCls;
    jmethodID consID, toStrID;
    assert(ex); // exception set on entrance.
    (*env)->ExceptionClear(env);
    exCls = (*env)->GetObjectClass(env, ex);
    if ((*env)->ExceptionOccurred(env)) goto error;
    toStrID = (*env)->GetMethodID(env, exCls,
				  "toString", "()Ljava/lang/String;");
    if ((*env)->ExceptionOccurred(env)) goto error;
    descstr = (*env)->CallObjectMethod(env, ex, toStrID);
    if ((*env)->ExceptionOccurred(env)) goto error;
    nexCls = (*env)->FindClass(env, exclsname);
    if ((*env)->ExceptionOccurred(env)) goto error;
    consID = (*env)->GetMethodID(env, nexCls,
				 "<init>", "(Ljava/lang/String;)V");
    if ((*env)->ExceptionOccurred(env)) goto error;
    nex = (*env)->NewObject(env, nexCls, consID, descstr);
    if ((*env)->ExceptionOccurred(env)) goto error;
    (*env)->Throw(env, nex);
    return;
 error: // throw original error.
    (*env)->ExceptionClear(env);
    (*env)->Throw(env, ex);
    return;
}

static inline
jclass fni_class_forName(JNIEnv *env, jstring str) {
    const char *name = (*env)->GetStringUTFChars(env, str, NULL);
    jclass result;
    char buf[strlen(name)+1], *cp;
    /* change . to / */
    strcpy(buf, name);
    for (cp=buf; *cp != '\0'; cp++)
      if (*cp == '.') *cp = '/';
    /* now look up translated name */
    result = (*env)->FindClass(env, buf);
    /* release memory and we're done! */
    (*env)->ReleaseStringUTFChars(env, str, name);
    /* actually, we need to create and throw a ClassNotFoundException if
     * things didn't go well; FindClass makes a NoClassDefFoundError,
     * which isn't the right type for us. */
    if ((*env)->ExceptionOccurred(env)) goto error;
    return result;

  error:
#ifdef WITH_MISSING_ROOT_DEBUG
    // [AS] 01/06/06 added to help missing roots
    printf("WARNING: fni_class_forName: Failed attempt to load class %s\n\tCheck root set!", buf);
    fflush(NULL);
#endif

    wrapNthrow(env, "java/lang/ClassNotFoundException");
    return NULL;
}

#ifdef WITH_INIT_CHECK
static inline
jclass fni_class_forName_initcheck(JNIEnv *env, jstring str) {
  jclass result, clscls;
  /* initialize java.lang.Class */
  clscls = (*env)->FindClass(env, "java/lang/Class");
  if (clscls && !REFLECT_staticinit(env, clscls)) return NULL;/* failed init */
  /* invoke Class.forName() */
  result = fni_class_forName(env, str);
  /* now initialize the loaded class */
  if (result && !REFLECT_staticinit(env, result)) return NULL;/* failed init */
  return result;
}
#endif /* WITH_INIT_CHECK */

static inline
jobject fni_class_newInstance(JNIEnv *env, jclass cls) {
    jobject result;
    jmethodID methodID;
    /* okay, get constructor for this object and create it. */
    methodID=(*env)->GetMethodID(env, cls, "<init>", "()V");
    /* if methodID=NULL, throw InstantiationException */
    if ((*env)->ExceptionOccurred(env)) goto error;
    result = (*env)->NewObject(env, cls, methodID);
    if ((*env)->ExceptionOccurred(env)) goto error;
    return result;
    
  error:
    wrapNthrow(env, "java/lang/InstantiationException");
    return NULL;
}

#ifdef WITH_INIT_CHECK
static inline
jobject fni_class_newInstance_initcheck(JNIEnv *env, jclass cls) {
  /* this next static init may be redundant; class should be initialized
   * when Class object fetched via forName() or suchlike. */
  if (!REFLECT_staticinit(env, cls)) return NULL; /* init failed */
  return fni_class_newInstance(env, cls);
}
#endif /* WITH_INIT_CHECK */

#ifdef WITH_TRANSACTIONS
// XXX THIS METHOD ISN'T ACTUALLY USED ANYMORE
// we've switched to making our JNI-methods transaction-aware, so
// we can now use the 'regular' newInstance implementation.
static inline
jobject fni_class_newInstance_withtrans
  (JNIEnv *env, jclass cls, jobject commitrec) {
    jobject result;
    jmethodID methodID;
    /* okay, get constructor for this object and create it. */
    methodID=(*env)->GetMethodID(env, cls, "<init>$$withtrans",
				 "(L" TRANSPKG "CommitRecord;)V");
    /* if methodID=NULL, throw InstantiationException */
    if ((*env)->ExceptionOccurred(env)) goto error;
    result = (*env)->NewObject(env, cls, methodID, commitrec);
    if ((*env)->ExceptionOccurred(env)) goto error;
    return result;
    
  error:
    wrapNthrow(env, "java/lang/InstantiationException");
    return NULL;
}
#endif /* WITH_TRANSACTIONS */

static inline
jboolean fni_class_isInstance(JNIEnv *env, jclass cls, jobject obj) {
    assert(obj);/* May not work properly for obj==null */
    return (*env)->IsInstanceOf(env, obj, (jclass)cls);
}

// returns true if an object of cls2 can safely be cast to cls1
static inline
jboolean fni_class_isAssignableFrom(JNIEnv *env, jclass cls1, jclass cls2) {
  /* not sure if IsAssignableFrom works on primitive types */
  if (fni_class_isPrimitive(env, cls1))
    return (*env)->IsSameObject(env, cls1, cls2);
  return (*env)->IsAssignableFrom(env, cls2, cls1);
}

static inline
jboolean fni_class_isInterface(JNIEnv *env, jclass cls) {
    /* Look for the class on its own interface list */
    struct claz *thisclz = FNI_GetClassInfo(cls)->claz;
    struct claz **ilist;
    for (ilist=thisclz->interfaces; *ilist!=NULL; ilist++)
      if (*ilist == thisclz) return JNI_TRUE;
    return JNI_FALSE;
}

static inline
jboolean fni_class_isArray(JNIEnv *env, jclass cls) {
    struct claz *thisclz = FNI_GetClassInfo(cls)->claz;
    return (thisclz->component_claz==NULL) ? JNI_FALSE : JNI_TRUE;
}

/* [AS] 11/23/05

  static inline
  jboolean fni_class_isPrimitive(JNIEnv *env, jclass cls)
  
  is defined at the top of this file.  The old solution, declaring a
  "static inline" prototype there and giving the implementation here,
  was not kosher with gcc (GCC) 3.4.4 20050721 and resulted in a
  compilation error: "inlining failed in call to ...; function not
  considered for inlining".  It's unclear why an "inline" hint to the
  compiler results in an error, instead of just a warning. */

static inline
jstring fni_class_getName(JNIEnv *env, jclass cls) {
    struct FNI_classinfo *info = FNI_GetClassInfo(cls);
    /* replace / with . so that we do the same thing as Sun's JDKs */
    char buf[strlen(info->name)+1], *dst; const char *src;
    for (src=info->name, dst=buf; *src; src++, dst++)
      *dst = (*src=='/')?'.':*src;
    *dst='\0';
    /* okay, create java string and go home */
    return (*env)->NewStringUTF(env, buf);
}

static inline
jclass fni_class_getSuperclass(JNIEnv *env, jclass cls) {
  return (*env)->GetSuperclass(env, cls);
}

static inline
jobjectArray fni_class_getInterfaces(JNIEnv *env, jclass cls) {
  struct claz *thisclz = FNI_GetClassInfo(cls)->claz;
  struct claz **cp;
  jclass clscls;
  jobjectArray r;
  jsize i, ninterfaces = 0;
  /* count length of interfaces list */
  for (cp = thisclz->interfaces; *cp!=NULL; cp++)
    ninterfaces++;
  /* now create a Class array of the proper size */
  clscls = (*env)->FindClass(env, "java/lang/Class");
  if (!clscls) return NULL;
  r = (*env)->NewObjectArray(env, ninterfaces, clscls, NULL);
  if (!r) return NULL;
  /* set the elements of the Class array to the proper things. */
  for (i=0; i<ninterfaces; i++)
    (*env)->SetObjectArrayElement
      (env, r, i, FNI_WRAP(thisclz->interfaces[i]->class_object));
  /* done! */
  return r;
}

static inline
jclass fni_class_getComponentType(JNIEnv *env, jclass cls) {
  struct claz *thisclz = FNI_GetClassInfo(cls)->claz;
  struct claz *compclz = thisclz->component_claz;
  return compclz ? FNI_WRAP(compclz->class_object) : NULL;
}
#ifdef WITH_INIT_CHECK
static inline
jclass fni_class_getComponentType_initcheck(JNIEnv *env, jclass cls) {
  jclass r = fni_class_getComponentType(env, cls);
  if (!REFLECT_staticinit(env, r)) return NULL; /* init failed */
  return r;
}
#endif /* WITH_INIT_CHECK */

static inline
jint fni_class_getModifiers(JNIEnv *env, jclass cls) {
    struct FNI_classinfo *info = FNI_GetClassInfo(cls);
    assert(info);
    return info->modifiers;
}

/* XXX: we really should transition to a 'getPrimitiveClass' method which
 * takes a *char* instead of a *string*. */
static inline
jclass fni_class_getPrimitiveClass(JNIEnv *env, jstring str) {
  return fni_class_forName(env, str);
}

#ifdef DEFINE_MEMBER_FUNCTIONS
/* (often this code is unused by code which #includes class.h) */

/* member accessor functions.  some types first: */
enum _fni_class_restrictionType { NONE, ONLY_DECLARED, ONLY_PUBLIC, ONLY_PUBLIC_DECLARED };
enum _fni_class_memberType { FIELDS, METHODS, CONSTRUCTORS };

/* decision helper method */


/* Check whether the member *mptr of the class cls is of kind (which,
   type) (e.g., a public, declared (=non-inherited), field) and has
   type memberClass. 

   Arguments:

   env - JNI environment, passed around by all JNI methods

   cls - class whose members we are interested in

   which - Inappropriate name, should be called "modifier" instead;
	   Tells whether we are interested in a member declared by cls
	   (instead of just inherited), a public member, a combination
	   of the two properties, or none of them

   type - Inappropriate name, should be called "kind" instead.
          Specifies whether we are interested in a field, a method or
          a constructor.  I think this info is also redundant: it can
          be reconstructed from memberClass.
   
   memberClass - The class of the member we are interested in.  */
static /* not inline; used too often. */
jboolean _fni_class_isAptMember
    (JNIEnv *env, jclass cls, union _jmemberID *mptr,
     enum _fni_class_restrictionType which,
     enum _fni_class_memberType type,
     jclass memberClass) {
    
    /* 1. Filter out members that don't agree with memberClass. */
    if(!(*env)->IsInstanceOf(env, FNI_WRAP(mptr->m.reflectinfo->method_object),
			     memberClass)) {
	return JNI_FALSE;
    }
    
    /* The next test is irrelevant. */
    /* 2. Filter out non-public if which==ONLY_PUBLIC or ONLY_PUBLIC_DECLARED */
    /* [AS] Apparently, current implementation does not check this.  I
       would love to implement it correctly,but who knows that it is not
       used in a variety of places that rely on this "feature" */
#if 0 /* correct implementation would take into account package-visibility */
    if( (which & ONLY_PUBLIC) &&
	!(mptr->m.reflectinfo->modifiers & java_lang_reflect_Modifier_PUBLIC) ) {
	return JNI_FALSE;
    }
#endif
    
    /* 3. Filter out non-local if which==ONLY_DECLARED or ONLY_PUBLIC_DECLARED */
    if( (which & ONLY_DECLARED) &&
	(mptr->m.reflectinfo->declaring_class_object != FNI_UNWRAP_MASKED(cls)) ) {
	return JNI_FALSE;
    }

    /* [AS] why on Earth is <clinit> associated with a field? */
    /* 4. Filter out <clinit> if type!=FIELDS */
    if( (type != FIELDS) && (strcmp(mptr->m.name, "<clinit>") == 0) ) {
	return JNI_FALSE;
    }
    
#ifdef WITH_INIT_CHECK
    /* 5. Test related to our handling of the static initializers.
       There are two epochs in the execution of a program: in the
       first epoch, we run the static initializers of all relevant
       classes; this code is quite slow because it has to check before
       each access whether the declaring class has been initialized or
       not.  In the second epoch, we run the real program, without
       checking for class initialization (all classes have already
       been initialized). We should make sure that in each phase we
       return only methods that make sense in that phase. */
    if(type!=FIELDS) {
	extern int initDone;
	/* isInitCheck is true iff the name ends with "$$initcheck" */

	/* Checks whether the name of *mptr ends with $$initcheck. */
	char *substr = rindex(mptr->m.name, '$');
	int isInitCheck = 
	    ( (substr != NULL) && // '$' is present in the name
	      (substr > mptr->m.name) && // substr-1 makes sense
	      (strcmp(substr-1, "$$initcheck")==0) );

	/* filter out $$initcheck methods when we're done initializing;
	 * filter out non-$$initcheck methods when we're still initializing. */
	if(initDone) { // we finished the initialization,
	    // and don't want to hear about the $$initcheck methods any more
	    if(isInitCheck) {
		return JNI_FALSE;
	    }
	}
	else { // we're still initializing
	    // and want to consider only the $$initcheck version
	    if(!isInitCheck) {
		return JNI_FALSE;
	    }
	}
    }
#endif /* WITH_INIT_CHECK */

    /* congratulations! you've run the gauntlet! */
    return JNI_TRUE;
}



static /* used too many times to inline profitably. */
jobjectArray fni_class_getMembers
  (JNIEnv *env, jclass cls, 
   enum _fni_class_restrictionType which,
   enum _fni_class_memberType type) {
  struct FNI_classinfo *info = FNI_GetClassInfo(cls);
  union _jmemberID *ptr;
  jclass memberClass; jobjectArray result;
  jsize count=0;
  /* find class corresponing to the type of member we're interested in */
  memberClass = (*env)->FindClass(env,
				  (type==FIELDS)?"java/lang/reflect/Field":
				  (type==METHODS)?"java/lang/reflect/Method":
				  "java/lang/reflect/Constructor");
  /* count matching members */
  for (ptr=info->memberinfo; ptr < info->memberend; ptr++) {

      //printf("member %s\n", ptr->m.name);

      if (_fni_class_isAptMember(env, cls, ptr, which, type, memberClass)) {
	  count++;
      }
  }

  /* create properly-sized and -typed array */
  result = (*env)->NewObjectArray(env, count, memberClass, NULL);
  /* now put matching members in array */
  count=0;
  for (ptr=info->memberinfo; ptr < info->memberend; ptr++)
    if (_fni_class_isAptMember(env, cls, ptr, which, type, memberClass))
      (*env)->SetObjectArrayElement
	  (env, result, count++, FNI_WRAP(ptr->m.reflectinfo->method_object));

  if((count==0) && (type==CONSTRUCTORS)) {
#ifdef WITH_MISSING_ROOT_DEBUG
      printf("WARNING: fni_class_getMembers: cannot find constructors for class %s\n\tCheck root set!",
	     FNI_GetClassInfo(cls)->name);
#endif
      fflush(NULL);
  }

  /* done */
  return result;
}

/* another helper method */
static /* not inline */
jobject _fni_class_findMember
  (JNIEnv *env, jclass cls,
   enum _fni_class_restrictionType which,
   enum _fni_class_memberType type,
   int (*f)(JNIEnv *env,
	    union _jmemberID *ptr, void *cl),
   void *closure, char *exclassname) {
  struct FNI_classinfo *info = FNI_GetClassInfo(cls);
  jclass excls, memberClass;
  union _jmemberID *ptr;
  /* find class corresponing to the type of member we're interested in */
  memberClass = (*env)->FindClass(env,
				  (type==FIELDS)?"java/lang/reflect/Field":
				  (type==METHODS)?"java/lang/reflect/Method":
				  "java/lang/reflect/Constructor");
  /* search for member */
  for (ptr=info->memberinfo; ptr < info->memberend; ptr++)
    if (_fni_class_isAptMember(env, cls, ptr, which, type, memberClass) &&
	f(env, ptr, closure))
      /* found! */
      return FNI_WRAP(ptr->f.reflectinfo->field_object);
  /* not found =( */
  excls = (*env)->FindClass(env, exclassname);
  if ((*env)->ExceptionOccurred(env)) return NULL;
  (*env)->ThrowNew(env, excls, "no such member");
  return NULL;
}

struct field_closure {
  const char *cname;
};
static int field_cmp(JNIEnv *env, union _jmemberID *ptr, void *cl) {
  struct field_closure *c = (struct field_closure *) cl;
  return 0==strcmp(ptr->f.name, c->cname);
}
static inline
jobject fni_class_getField(JNIEnv *env, jclass cls, jstring name,
			   enum _fni_class_restrictionType which) {
  struct field_closure c;
  jobject result;

  c.cname = (*env)->GetStringUTFChars(env, name, NULL);

  result = _fni_class_findMember(env, cls, which, FIELDS, field_cmp, &c,
				 "java/lang/NoSuchFieldException");
  (*env)->ReleaseStringUTFChars(env, name, c.cname);
  return result;
}

struct method_closure {
  const char *cname;
  jobjectArray paramTypes;
  int nparams;
};
static int method_cmp(JNIEnv *env, union _jmemberID *ptr, void *cl) {
  struct method_closure *c = (struct method_closure *) cl;
  int i; char *desc;
  if (strcmp(ptr->m.name, c->cname)!=0) return 0;
  for (i=0, desc=ptr->m.desc+1; desc!=NULL && *desc!=')';
       desc = REFLECT_advanceDescriptor(desc), i++) {
    if (i>=c->nparams) return 0; /* too many parameters */
    if (!(*env)->IsSameObject
	(env, REFLECT_parseDescriptor(env, desc),
	 (*env)->GetObjectArrayElement(env, c->paramTypes, i)))
      return 0;
  }
  return (i==c->nparams);
}
static inline
jobject fni_class_getMethod(JNIEnv *env, jclass cls,
			    jstring name, jobjectArray paramTypes,
			    enum _fni_class_restrictionType which) {
  struct method_closure c;
  jobject result;
  c.cname = (*env)->GetStringUTFChars(env, name, NULL);
  c.paramTypes = paramTypes;
  c.nparams = paramTypes ? (*env)->GetArrayLength(env, paramTypes) : 0;

  result = _fni_class_findMember(env, cls, which, METHODS, method_cmp, &c,
				 "java/lang/NoSuchMethodException");
  (*env)->ReleaseStringUTFChars(env, name, c.cname);
  return result;
}

struct constructor_closure {
  jobjectArray paramTypes;
  int nparams;
};
static int constructor_cmp(JNIEnv *env, union _jmemberID *ptr, void *cl) {
  struct constructor_closure *c = (struct constructor_closure *) cl;
  int i; char *desc;
  for (i=0, desc=ptr->m.desc+1; desc!=NULL && *desc!=')';
       desc = REFLECT_advanceDescriptor(desc), i++) {
    if (i>=c->nparams) return 0; /* too many parameters */
    if (!(*env)->IsSameObject
	(env, REFLECT_parseDescriptor(env, desc),
	 (*env)->GetObjectArrayElement(env, c->paramTypes, i)))
      return 0;
  }
  return (i==c->nparams);
}
static inline
jobject fni_class_getConstructor(JNIEnv *env, jclass cls,
				 jobjectArray paramTypes,
				 enum _fni_class_restrictionType which) {
  struct constructor_closure c;
  /* note that paramTypes==null is equivalent to paramTypes=new Class[0] */
  c.paramTypes = paramTypes;
  c.nparams = paramTypes ? (*env)->GetArrayLength(env, paramTypes) : 0;
  
  return _fni_class_findMember(env, cls, which,
			       CONSTRUCTORS, constructor_cmp, &c,
			       "java/lang/NoSuchMethodException");
}
#endif /* DEFINE_MEMBER_FUNCTIONS */

#endif /* INCLUDED_FNI_CLASS_H */
