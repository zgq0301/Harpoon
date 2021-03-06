CYGWIN=$(shell grep -c 'CYGWIN' /proc/version)
JAVA_HOME=$(shell if test $(CYGWIN) -eq 0; then echo `cd /usr/java/j2s*; pwd`; \
		  else echo /cygdrive/c/j2*; \
		  fi)
JCC=$(JAVA_HOME)/bin/javac
JAVAI=$(JAVA_HOME)/bin/java
JAR=$(JAVA_HOME)/bin/jar
JAVADOC=$(JAVA_HOME)/bin/javadoc
SSH=ssh
FORTUNE=/usr/games/fortune
INSTALLMACHINE=magic@www.magic.lcs.mit.edu
INSTALLDIR=public_html/Harpoon/
SOURCES=$(wildcard src/*.java)
RELEASE=$(SOURCES) README BUILDING COPYING Makefile ChangeLog 

JAVA=$(JAVAI) -classpath .

IDL_FLAGS=-Iidl
IDLCC=$(JAVA) org.jacorb.idl.parser $(IDL_FLAGS) 

ISOURCES=src/*.idl

# figure out what the current CVS branch is, by looking at the Makefile
CVS_TAG=$(firstword $(shell cvs status Makefile | grep -v "(none)" | \
		awk '/Sticky Tag/{print $$3}'))
CVS_REVISION=$(patsubst %,-r %,$(CVS_TAG))

all:    clean doc realtime.jar # realtime.tgz

clean:
	@echo Cleaning up docs and realtime.jar.
	@rm -rf doc java javax org demo HTTPClient idl  java_cup META-INF
	@rm -f realtime.jar realtime.jar.TIMESTAMP
	@rm -f realtime.tgz realtime.tgz.TIMESTAMP
	@rm -f *.class src/*.class
	@rm -f ChangeLog

doc:	$(SOURCES)
	@echo Generating documentation...
	@$(RM) -rf doc
	@mkdir -p doc
	@javadoc -quiet -private -linksource -d doc/ $(filter-out src/Object.java,$(SOURCES)) > /dev/null
	@date '+%-d-%b-%Y at %r %Z.' > doc/TIMESTAMP
	@chmod -R a+rX doc

realtime.jar: $(SOURCES)
	@echo Generating realtime.jar file...
	@rm -rf javax java
	@mkdir -p javax/realtime java/lang java/util/concurrent/atomic
	@$(JAR) xf support/jacorb.jar
	@$(IDLCC) -d . $(ISOURCES)
	@$(JCC) -d . -g $^ javax/realtime/*.java
	@jar -cf $@ javax/realtime java/util/concurrent org
	@rm -rf javax java org demo HTTPClient idl java_cup META-INF 
	@date '+%-d-%b-%Y at %r %Z.' > $@.TIMESTAMP

realtime.tgz: $(RELEASE)
	@echo Generating $@ file.
	@tar -c $(RELEASE) | gzip -9 > $@
	@date '+%-d-%b-%Y at %r %Z.' > $@.TIMESTAMP

jar-install: realtime.jar
	@echo Installing realtime.jar.
	tar -c realtime.jar realtime.jar.TIMESTAMP | \
		$(SSH) $(INSTALLMACHINE) "tar -C $(INSTALLDIR) -x"

tar-install: realtime.tgz
	@echo Installing realtime.tgz.
	tar -c realtime.tgz realtime.tgz.TIMESTAMP | \
		$(SSH) $(INSTALLMACHINE) "tar -C $(INSTALLDIR) -x"

doc-install: doc
	@echo Installing documentation.
	tar -c doc | \
		$(SSH) $(INSTALLMACHINE) \
	"mkdir -p $(INSTALLDIR)/Realtime && /bin/rm -rf $(INSTALLDIR)/Realtime/doc && tar -C $(INSTALLDIR)/Realtime -x"

install: jar-install tar-install doc-install

ChangeLog: needs-cvs $(SOURCES) # dependency is not strictly accurate.
	@echo Generating ChangeLog...
	@-$(RM) $@
	@rcs2log | sed -e 's:/[^,]*/CVSROOT/Realtime/::g' > $@

update: needs-cvs
	cvs -q update -Pd $(CVS_REVISION)
	@-if [ -x $(FORTUNE) ]; then echo ""; $(FORTUNE); fi

# the 'cvs' rules only make sense if you've got a copy checked out from CVS
needs-cvs:
	@if [ ! -d CVS ]; then \
	  echo This rule needs CVS access to the source tree. ; \
	   exit 1; \
	fi
