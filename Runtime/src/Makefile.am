## Process this file with automake to produce Makefile.in
## $Id: Makefile.am,v 1.53 2006-12-28 17:05:21 cananian Exp $
MAINTAINERCLEANFILES = Makefile.in
INCLUDES = @IFLAGS@
SUBDIRS = reflect-thunk jni java.lang java.lang.reflect \
	$(NATIVEDIR) \
	$(PGCDIR) $(CLHEAPDIR) $(NATIVEIODIR) stats cam servo $(USERDIR) \
	dynsync transact $(REALTIMEDIR) $(RTJPERFDIR) $(ROLEINFERDIR) \
	$(MZFDIR)
# make sure even the conditionally-compiled subdirectories are included
# in the distribution.
DIST_SUBDIRS = reflect-thunk jni java.lang java.lang.reflect \
	sunjdk classpath \
        gc clheap nativeio stats cam servo user \
        dynsync transact realtime rtjperf roles mzf
EXTRA_DIST = README

# cruft to define the conditional directories. --------------------------
# please keep these in the same order that they appear in the SUBDIRS list

# for compiling against Sun'd JDK, add sunjdk to SUBDIRS
if SUNJDK
NATIVEDIR=sunjdk
NATIVELIB=\
	sunjdk/java.lang/libjavalang.la \
	sunjdk/java.io/libjavaio.la \
	sunjdk/java.net/libjavanet.la \
	sunjdk/java.util/libjavautil.la \
	sunjdk/java.util.zip/libjavautilzip.la
else
NATIVEDIR=classpath
NATIVELIB=\
	classpath/java.io/libjavaio.la \
	classpath/java.lang/libjavalang.la \
	classpath/java.lang.reflect/libjavalangreflect.la \
	classpath/gnu.vm.stack/libgnuvmstack.la
endif

# for precise gc, add gc to SUBDIRS
if PRECISE_GC
PGCDIR=gc # rename to precisegc?
PGCLIB=gc/libgc.la
endif
# for clustered heap support, add clheap to SUBDIRS
if CLUSTERED_HEAPS
CLHEAPDIR=clheap
CLHEAPLIB=clheap/libclheap.la
endif
# for event-driven support, build nativeio libraries.
if EVENT_DRIVEN
NATIVEIODIR=nativeio # rename to eventdriven?
NATIVEIOLIB=nativeio/libnativeio.la
endif
# for statistics support, build stats library.
# (always built, sometimes library is empty)

# for camera support, build camera libraries.
# (always built, sometimes library is empty)

# for servo support, build servo libraries.
# (always built, sometimes library is empty)

# for user threads add user to SUBDIRS
if USER_THREADS
USERDIR=user
USERLIB=user/libuser.la
endif

# for dynamic synchronization removal support, build libdynsync libraries.
# (always built, sometimes library is empty)

# for transaction support, build transaction libraries.
# (always built, sometimes library is empty)

# for mostly-zero fields support, build mzf libraries.
if MZF_SUPPORT
MZFDIR=mzf
MZFLIB=mzf/libmzf.la
endif
# for role support, build role libraries
if ROLE_INFER
ROLEINFERDIR=roles
ROLEINFERLIB=roles/libroleinfer.la
endif
# for realtime java support, add realtime to SUBDIRS
if REALTIME_JAVA
REALTIMEDIR=realtime
REALTIMELIB=realtime/librealtime.la
endif
# for realtime timer support (RTJPerf adapted for FLEX)
if RTJ_PERF
RTJPERFDIR=rtjperf
RTJPERFLIB=rtjperf/librtjperf.la
endif

# see run% rule in top-level makefile to see how to generate a binary
# executable from the Java.a file and these two libraries.
lib_LTLIBRARIES = libflexc0.la libflexc1.la

libflexc0_la_SOURCES = startup.c
libflexc0_la_LIBADD = \
	jni/libjni.la \
	../arch/@ARCHDIR@/libheader.la
libflexc0_la_LDFLAGS = -version-info 0:0:0

libflexc1_la_SOURCES = config_checker.c
libflexc1_la_LIBADD = \
	../arch/@ARCHDIR@/libfooter.la \
	java.lang/libjavalang-indep.la \
	java.lang.reflect/libjavalangreflect-indep.la \
	$(NATIVELIB) \
	$(PGCLIB) $(CLHEAPLIB) $(NATIVEIOLIB) stats/libstats.la cam/libcam.la servo/libservo.la $(USERLIB) \
	dynsync/libdynsync.la transact/libtransact.la $(REALTIMELIB) $(RTJPERFLIB) $(ROLEINFERLIB) $(MZFLIB)

libflexc1_la_LDFLAGS = -version-info 0:0:0 # -lbfd -liberty
# this library depends on -lbfd and -liberty, but these ought to be in $LIBS
