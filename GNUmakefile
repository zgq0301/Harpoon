# $Id: GNUmakefile,v 1.72 2002-05-02 22:17:41 salcianu Exp $
# CAUTION: this makefile doesn't work with GNU make 3.77
#          it works w/ make 3.79.1, maybe some others.

empty:=
space:= $(empty) $(empty)

JAVA:=java
JFLAGS=-d . -g
JFLAGSVERB=#-verbose -J-Djavac.pipe.output=true
JIKES:=jikes $(JIKES_OPT)
JCC5:=$(JSR14DISTR)/scripts/javac -source 1.4 -gj #-warnunchecked
JCC:=javac -J-mx64m -source 1.4
JDOC:=javadoc
JAR=jar
JDOCFLAGS:=-J-mx128m -version -author -breakiterator \
  -overview harpoon/overview.html -doctitle "FLEX API documentation"
JDOCGROUPS:=\
  -group "Basic Class/Method/Field handling" "harpoon.ClassFile*" \
  -group "Intermediate Representations" "harpoon.IR*:harpoon.Temp*" \
  -group "Interpreters for IRs" "harpoon.Interpret*" \
  -group "Analyses and Transformations" "harpoon.Analysis*:harpoon.Runtime*" \
  -group "Backends (including code selection and runtime support)" "harpoon.Backend*" \
  -group "Tools and Utilities" "harpoon.Tools*:harpoon.Util*" \
  -group "Contributed packages" "gnu.*"
JDOCIMAGES=/usr/local/jdk/docs/api/images
SSH=ssh
#SCP=scp -q # not needed anymore, and deprecated.
MUNGE=bin/munge
UNMUNGE=bin/unmunge
FORTUNE=/usr/games/fortune
INSTALLMACHINE=magic@www.magic.lcs.mit.edu
INSTALLDIR=public_html/Harpoon/
JDKDOCLINK = http://java.sun.com/products/jdk/1.2/docs/api
#JDKDOCLINK = http://palmpilot.lcs.mit.edu/~pnkfelix/jdk-javadoc/java.sun.com/products/jdk/1.2/docs/api
CVSWEBLINK = http://flexc.lcs.mit.edu/Harpoon/cvsweb.cgi

# make this file work with make version less than 3.77
ifndef CURDIR
	CURDIR := $(shell pwd)
endif
# Add "-link" to jdk's javadoc if we are using a javadoc that supports it
JDOCFLAGS += \
	$(shell if $(JDOC) -help 2>&1 | grep -- "-link " > /dev/null ; \
	then echo -link $(JDKDOCLINK) ; fi)
# Add "-group" options to JDOCFLAGS if we're using a javadoc that supports it
JDOCFLAGS += \
	$(shell if $(JDOC) -help 2>&1 | grep -- "-group " > /dev/null ; \
	then echo '$(JDOCGROUPS)' ; fi)

SUPPORT := Support/Lex.jar Support/CUP.jar Support/jasmin.jar \
	   $(wildcard SupportNP/ref.jar) $(wildcard SupportNP/collections.jar)
SUPPORTP := $(filter-out SupportNP/%,$(SUPPORT))
# filter out collections.jar if we don't need it.
ifeq (0, ${MAKELEVEL})
SUPPORTC:= $(filter-out \
   $(shell chmod u+x bin/test-collections;\
           if bin/test-collections; then echo SupportNP/collections.jar; fi),\
   $(SUPPORT))
CLASSPATH:=$(subst $(space),:,$(addprefix $(CURDIR)/,$(SUPPORTC))):$(CLASSPATH)
CLASSPATH:=.:$(CLASSPATH)
endif
# cygwin compatibility
ifdef CYGWIN
CLASSPATH:=$(shell cygpath -w -p $(CLASSPATH))
JAVA:=$(JAVA) -cp "$(CLASSPATH)"
JIKES:=$(JIKES) -classpath "$(CLASSPATH);c:/jdk1.3.0_01/jre/lib/rt.jar"
else
	export CLASSPATH
endif

CVS_TAG=$(firstword $(shell cvs status GNUmakefile | grep -v "(none)" | \
			awk '/Sticky Tag/{print $$3}'))
CVS_BRANCH=$(firstword $(shell cvs status GNUmakefile | grep -v "(none)" |\
			awk '/Sticky Tag/{print $$5}' | sed -e 's/[^0-9.]//g'))
CVS_REVISION=$(patsubst %,-r %,$(CVS_TAG))

# skip these definitions if we invoke make recursively; use parent's defs.
ifeq (0, ${MAKELEVEL})
BUILD_IGNORE := $(strip $(shell if [ -f .ignore ]; then cat .ignore; fi))

ALLPKGS := $(shell find . -type d | grep -v CVS | grep -v AIRE | \
		$(patsubst %,egrep -v "%" |,$(BUILD_IGNORE)) \
		egrep -v "^[.]/(harpoon|silicon|gnu|(src)?doc|NOTES|bin|jdb)"|\
		egrep -v "^[.]/(as|java_cup)" | \
		egrep -v "/doc-files" | \
		sed -e "s|^[.]/*||")

SCRIPTS := bin/test-collections bin/annotate.perl $(MUNGE) $(UNMUNGE) \
	   bin/source-markup.perl bin/cvsblame.pl

MACHINE_SRC := Tools/PatMat/Lexer.jlex Tools/PatMat/Parser.cup \
               Tools/Annotation/Java12.cup \
	       $(wildcard Runtime/AltArray/*JavaType.jt)
MACHINE_GEN := Tools/PatMat/Lexer.java Tools/PatMat/Parser.java \
               Tools/Annotation/Java12.java \
               Tools/PatMat/Sym.java Tools/Annotation/Sym.java \
	       $(foreach file, $(patsubst %JavaType.jt,%,\
				$(wildcard Runtime/AltArray/*JavaType.jt)),\
		$(file)Boolean.java $(file)Byte.java $(file)Short.java\
		$(file)Int.java $(file)Long.java $(file)Float.java\
		$(file)Double.java $(file)Char.java $(file)Object.java)

CGSPECS:=$(foreach dir, $(ALLPKGS), $(wildcard $(dir)/*.spec))
CGJAVA :=$(patsubst %.spec,%.java,$(CGSPECS))
MACHINE_SRC+=$(CGSPECS)
MACHINE_GEN+=$(filter-out Tools/PatMat/% Backend/Jouette/%,$(CGJAVA))
# apply .ignore to MACHINE_GEN
MACHINE_GEN := $(filter-out .%.java $(patsubst %,\%%,$(BUILD_IGNORE)),\
		$(MACHINE_GEN))

ALLSOURCE :=  $(MACHINE_GEN) $(filter-out $(MACHINE_GEN), \
		$(filter-out .%.java \#% $(patsubst %,\%%,$(BUILD_IGNORE)),\
		$(foreach dir, $(ALLPKGS), $(wildcard $(dir)/*.java))))
TARSOURCE := $(filter-out $(MACHINE_GEN), $(filter-out JavaChip%, \
	        $(filter-out Test%,$(ALLSOURCE)))) GNUmakefile $(MACHINE_SRC)
JARPKGS := $(subst harpoon/Contrib,gnu, \
		$(foreach pkg, $(filter-out JavaChip%, \
			$(filter-out Test%,$(ALLPKGS))), harpoon/$(pkg)))
PROPERTIES:=Contrib/getopt/MessagesBundle.properties \
	    Support/nativecode-makefile.template \
	    Support/precisec-makefile.template \
	    Support/mipsda-makefile.template \
	    $(wildcard Backend/Runtime1/*.properties)
PKGDESC:=$(wildcard overview.html) $(wildcard README) \
	 $(foreach dir, $(ALLPKGS),\
	    $(wildcard $(dir)/package.html) $(wildcard $(dir)/README))

NONEMPTYPKGS := $(shell ls  $(filter-out GNUmakefile,$(TARSOURCE))  | \
		sed -e 's|/*[A-Za-z0-9_]*\.[A-Za-z0-9_]*$$||' | sort -u)

PKGSWITHJAVASRC := $(shell ls  $(filter-out GNUmakefile,$(TARSOURCE))  | \
		   sed -ne 's|/*[A-Za-z0-9_]*\.java$$||p' | sort -u)	
endif
# list all the definitions in the above block for export to children.
export BUILD_IGNORE ALLPKGS MACHINE_SRC MACHINE_GEN CGSPECS CGJAVA
export SCRIPTS ALLSOURCE TARSOURCE JARPKGS PROPERTIES PKGDESC
export NONEMPTYPKGS PKGSWITHJAVASRC

all:	java

list:
	@echo $(filter-out GNUmakefile,$(TARSOURCE))
list-source:
	@echo $(ALLSOURCE)
list-packages:
	@echo $(filter-out Test,$(ALLPKGS))
list-nonempty-packages:
	@echo $(filter-out Test,$(NONEMPTYPKGS))
list-packages-with-java-src:
	@echo $(filter-out Test,$(PKGSWITHJAVASRC))

# hack to let people build stuff w/o gj compiler until it stabilizes.
Support/gjlib.jar: $(shell grep -v ^\# gj-files ) gj-files
	$(RM) -rf gjlib
	mkdir gjlib
	@${JCC5} -d gjlib -g $(shell grep -v "^#" gj-files)
	${JAR} cf $@ -C gjlib .
	$(RM) -rf gjlib

java:	PASS = 1
java:	$(ALLSOURCE) $(PROPERTIES) gj-files
# some folk might not have the GJ compiler; use the pre-built gjlib for them.
# also use gjlib when building the first time from scratch, to work around
# two-compiler dependency problems.
	@if [ -d harpoon -a -x $(firstword ${JCC5}) ]; then \
	  echo Building with $(firstword ${JCC5}). ;\
	  ${JCC5} ${JFLAGS} $(shell grep -v "^#" gj-files) ; \
	else \
	  echo "**** Using pre-built GJ classes (in Support/gjlib.jar) ****" ;\
	  echo "See http://www.flex-compiler.lcs.mit.edu/Harpoon/jsr14.txt"  ;\
	  echo " for information on how to install/use the GJ compiler." ; \
	  ${JAR} xf Support/gjlib.jar harpoon ; \
	fi
	if [ ! -d harpoon ]; then \
	  $(MAKE) first; \
	fi
	@echo Compiling...
	@${JCC} ${JFLAGS} $(filter-out $(shell grep -v "^#" gj-files), $(ALLSOURCE))
	@if [ -f stubbed-out ]; then \
	  $(RM) `sort -u stubbed-out`; \
	  $(MAKE) --no-print-directory PASS=2 `sort -u stubbed-out` || exit 1; \
	  echo Rebuilding `sort -u stubbed-out`; \
	  ${JCC} ${JFLAGS} `sort -u stubbed-out` || exit 1; \
	  $(RM) stubbed-out; \
	fi 
	@$(MAKE) --no-print-directory properties
	touch java

jikes:	PASS = 1
jikes: 	$(MACHINE_GEN)
	@echo JIKES DOESNT YET SUPPORT JAVA 1.5
	@echo YOU MUST USE "'make java'"
	@exit 1
	@if [ ! -d harpoon ]; then $(MAKE) first; fi
	@${JCC5} ${JFLAGS} $(shell grep -v "^#" gj-files)
	@echo -n Compiling... ""
	@${JIKES} ${JFLAGS} $(filter-out $(shell grep -v "^#" gj-files), $(ALLSOURCE))
	@echo done.
	@if [ -f stubbed-out ]; then \
	  $(RM) `sort -u stubbed-out`; \
	  $(MAKE) --no-print-directory PASS=2 `sort -u stubbed-out` || exit 1; \
	  echo Rebuilding `sort -u stubbed-out`; \
	  ${JIKES} ${JFLAGS} `sort -u stubbed-out` || exit 1; \
	  $(RM) stubbed-out; \
	fi 
	@$(MAKE) --no-print-directory properties
	@touch java

properties:
	@echo -n Updating properties... ""
	@-mkdir -p gnu/getopt harpoon/Support harpoon/Backend/Runtime1
	@cp Contrib/getopt/MessagesBundle.properties gnu/getopt
	@cp Support/nativecode-makefile.template harpoon/Support
	@cp Support/precisec-makefile.template harpoon/Support
	@cp Backend/Runtime1/*.properties harpoon/Backend/Runtime1
	@echo done.

first:
	mkdir -p harpoon silicon gnu
	for pkg in $(sort \
	  $(filter-out Contrib%,$(filter-out JavaChip%,$(ALLPKGS)))); do \
		mkdir -p harpoon/$$pkg; \
	done

# the package-by-package hack below is made necessary by brain-dead shells
# that don't accept arbitrarily-large argument lists.
Harpoon.jar Harpoon.jar.TIMESTAMP: java COPYING VERSIONS
	@echo -n "Building JAR file..."
	@${JAR} c0f Harpoon.jar COPYING VERSIONS
	@for pkg in $(JARPKGS); do \
	  for suffix in class properties; do\
	    if echo $$pkg/*.$$suffix | grep -vq '/[*][.]' ; then \
	      ${JAR} u0f Harpoon.jar $$pkg/*.$$suffix ;\
	      echo -n . ;\
	    fi;\
	  done;\
	done
	@echo " done."
	date '+%-d-%b-%Y at %r %Z.' > Harpoon.jar.TIMESTAMP

JARFILES=Harpoon.jar Harpoon.jar.TIMESTAMP $(SUPPORTP)
jar:	$(JARFILES)
jar-install: $(JARFILES)
	$(RM) -rf jar-install
	mkdir jar-install
	cp $^ jar-install
	chmod a+r jar-install/*
	cd jar-install ; tar c . | \
		$(SSH) $(INSTALLMACHINE) tar -C $(INSTALLDIR) -xv
	$(RM) -rf jar-install

VERSIONS: $(TARSOURCE) # collect all the RCS version ID tags.
	@echo -n Compiling VERSIONS... ""
	@grep -Fh ' $$I''d: ' \
		$(filter-out %.jar, $(TARSOURCE)) > VERSIONS
	@echo done.

ChangeLog: needs-cvs $(TARSOURCE) # not strictly accurate anymore.
	-$(RM) ChangeLog
# used to include TARSOURCE on rcs2log cmdline
	rcs2log | sed -e 's:/[^,]*/CVSROOT/Code/::g' > ChangeLog

cvs-add: needs-cvs
	-for dir in $(filter-out Test,$(ALLPKGS)); do \
		(cd $$dir; cvs add *.java 2>/dev/null); \
	done
cvs-commit: needs-cvs cvs-add
	cvs -q diff -u | tee cvs-tmp # view changes we are committing.
	cvs -q commit $(patsubst %,-r %,$(CVS_BRANCH))
	$(RM) cvs-tmp
commit: cvs-commit # convenient abbreviation
update: needs-cvs
	cvs -q update -Pd $(CVS_REVISION)
	@-if [ -x $(FORTUNE) ]; then echo ""; $(FORTUNE); fi

# CodeGeneratorGenerator
%.java : %.spec $(filter Tools/PatMat/%,$(ALLSOURCE)) Support/NullCodeGen.template
	@if [ "PASS $(PASS)" = "PASS 1" ]; then \
	  echo PASS $(PASS): stubbing out $@; \
	  sed -e 's/PACKAGE/$(subst /,.,$(shell dirname $@))/g' \
	      -e 's/NullCodeGen/$(basename $(notdir $@))/g' \
	      < Support/NullCodeGen.template > $@; \
	  touch stubbed-out; echo $@ >> stubbed-out; \
	else \
	  echo PASS $(PASS): generating $@ from $<; \
	  $(JAVA) harpoon.Tools.PatMat.Main $< $(notdir $*) > $@; \
	fi

# JLex
%.java : %.jlex
	$(JAVA) JLex.Main $< && mv $<.java $@
# CUP
%.java : %.cup
	cd `dirname $@` && \
	$(JAVA) java_cup.Main -parser `basename $@ .java` -symbols Sym \
	< `basename $<`

# don't know how to automagically generate this dependency.
Tools/PatMat/Sym.java : Tools/PatMat/Parser.java

# Generic Types
%Boolean.java: %JavaType.jt
	@echo Specializing $@.
	@sed -e 's/JavaType/Boolean/g' -e 's/javaType/boolean/g' < $< > $@
%Byte.java: %JavaType.jt
	@echo Specializing $@.
	@sed -e 's/JavaType/Byte/g' -e 's/javaType/byte/g' < $< > $@
%Short.java: %JavaType.jt
	@echo Specializing $@.
	@sed -e 's/JavaType/Short/g' -e 's/javaType/short/g' < $< > $@
%Int.java: %JavaType.jt
	@echo Specializing $@.
	@sed -e 's/JavaType/Int/g' -e 's/javaType/int/g' < $< > $@
%Long.java: %JavaType.jt
	@echo Specializing $@.
	@sed -e 's/JavaType/Long/g' -e 's/javaType/long/g' < $< > $@
%Float.java: %JavaType.jt
	@echo Specializing $@.
	@sed -e 's/JavaType/Float/g' -e 's/javaType/float/g' < $< > $@
%Double.java: %JavaType.jt
	@echo Specializing $@.
	@sed -e 's/JavaType/Double/g' -e 's/javaType/double/g' < $< > $@
%Char.java: %JavaType.jt
	@echo Specializing $@.
	@sed -e 's/JavaType/Char/g' -e 's/javaType/char/g' < $< > $@
%Object.java: %JavaType.jt
	@echo Specializing $@.
	@sed -e 's/JavaType/Object/g' -e 's/javaType/Object/g' < $< > $@

# print graphs
%.ps : %.vcg
	@if [ -f $@ ]; then rm $@; fi # xvcg won't overwrite an output file.
	xvcg -psoutput $@ -paper 8x11 -color $(VCG_OPT) $<
	@echo "" # xvcg has a nasty habit of forgetting the last newline.

harpoon.tgz harpoon.tgz.TIMESTAMP: $(TARSOURCE) COPYING ChangeLog $(SUPPORTP) $(PROPERTIES) $(PKGDESC) Support/NullCodeGen.template $(wildcard Support/*-root-set) $(SCRIPTS) mark-executable
	tar czf harpoon.tgz COPYING $(TARSOURCE) ChangeLog $(SUPPORTP) $(PROPERTIES) $(PKGDESC) Support/NullCodeGen.template $(wildcard Support/*-root-set) $(SCRIPTS)
	date '+%-d-%b-%Y at %r %Z.' > harpoon.tgz.TIMESTAMP

tar:	harpoon.tgz harpoon.tgz.TIMESTAMP
tar-install: tar
	chmod a+r harpoon.tgz harpoon.tgz.TIMESTAMP
	tar c harpoon.tgz harpoon.tgz.TIMESTAMP | \
		$(SSH) $(INSTALLMACHINE) tar -C $(INSTALLDIR) -xv

srcdoc/harpoon/%.html: %.java
	mkdir -p $(dir $@); bin/source-markup.perl \
		`if [ -f $*.spec ]; then echo -j ; fi` \
		-u $(CVSWEBLINK) $< > $@
srcdoc/gnu/%.html: Contrib/%.java
	mkdir -p $(dir $@); bin/source-markup.perl -u $(CVSWEBLINK) $< > $@
srcdoc/silicon/%.html: %.java
	mkdir -p $(dir $@); bin/source-markup.perl -u $(CVSWEBLINK) $< > $@
srcdoc/Code/%.html: %
	mkdir -p $(dir $@); bin/source-markup.perl -u $(CVSWEBLINK) $< > $@
srcdoc: $(patsubst srcdoc/harpoon/Contrib/%,srcdoc/gnu/%,\
	$(patsubst srcdoc/harpoon/JavaChip/%,srcdoc/silicon/JavaChip/%,\
	$(addprefix srcdoc/harpoon/,\
	$(patsubst %.java,%.html,$(filter-out Test/%,$(ALLSOURCE)))))) \
	$(addprefix srcdoc/Code/,$(addsuffix .html,\
	$(MACHINE_SRC) $(SCRIPTS) GNUmakefile))
srcdoc/java srcdoc/sun srcdoc/sunw: Support/stdlibdoc.tgz
	mkdir -p srcdoc
	$(RM) -r $@
	if [ -r $< ]; then tar -C srcdoc -xzf $<; fi
	-(echo "order deny,allow"; echo "deny from all"; \
	 echo "allow from .mit.edu" ) > srcdoc/java/.htaccess && \
	 cp -f srcdoc/java/.htaccess srcdoc/sun/.htaccess && \
	 cp -f srcdoc/java/.htaccess srcdoc/sunw/.htaccess
	touch $@
srcdoc-clean:
	-${RM} -r srcdoc
srcdoc-install: srcdoc srcdoc/java
	chmod -R a+rX srcdoc
	tar c srcdoc | $(SSH) $(INSTALLMACHINE) \
		"/bin/rm -rf $(INSTALLDIR)/srcdoc ; tar -C $(INSTALLDIR) -xv"

doc:	doc/TIMESTAMP

doc/TIMESTAMP:	$(ALLSOURCE) mark-executable
# check for .*.java and #*.java files that will give javadoc headaches
	@badfiles="$(strip $(foreach dir,\
			$(filter-out Test JavaChip,$(PKGSWITHJAVASRC)),\
			$(wildcard $(dir)/[.\#]*.java)))"; \
	if [ ! "$$badfiles" = "" ]; then \
	  echo "Please remove $$badfiles before running 'make doc'";\
	  exit 1;\
	fi
# okay, safe to make doc.
	$(RM) -rf doc doc-link
	mkdir doc doc-link
	cd doc-link; ln -s .. harpoon ; ln -s .. silicon ; ln -s ../Contrib gnu
	-${JDOC} ${JDOCFLAGS} -d doc -sourcepath doc-link \
		$(subst harpoon.Contrib,gnu, \
		$(foreach dir, $(filter-out Test, \
			  $(filter-out JavaChip,$(PKGSWITHJAVASRC))), \
			  harpoon.$(subst /,.,$(dir))) silicon.JavaChip) | \
		grep -v "^@see warning:"
	$(RM) -r doc-link
	$(MUNGE) doc | \
	  sed -e 's/<\([a-z]\+\)@\([-A-Za-z.]\+\).\(edu\|EDU\)>/\&lt;\1@\2.edu\&gt;/g' \
	      -e 's/<dd> "The,/<dd> /g' -e 's/<body>/<body bgcolor=white>/' | \
		bin/annotate.perl -link $(JDKDOCLINK) -u $(CVSWEBLINK) | \
		$(UNMUNGE)
	cd doc; if [ -e $(JDOCIMAGES) ]; then ln -s $(JDOCIMAGES) images; fi
	cd doc; if [ ! -f index.html ]; then ln -s packages.html index.html; fi
	cd doc; if [ ! -f API_users_guide.html ]; then ln -s index.html API_users_guide.html; fi
	date '+%-d-%b-%Y at %r %Z.' > doc/TIMESTAMP
	chmod -R a+rX doc

doc-install: doc/TIMESTAMP mark-executable
	# only include ChangeLog if we've got CVS access
	if [ -d CVS ]; then \
          $(MAKE) ChangeLog; \
	  cp ChangeLog doc/ChangeLog.txt; \
	  chmod a+r doc/ChangeLog.txt; \
	fi
	tar -c doc | $(SSH) $(INSTALLMACHINE) \
		"/bin/rm -rf $(INSTALLDIR)/doc ; tar -C $(INSTALLDIR) -x"

doc-clean:
	-${RM} -r doc

mark-executable:
	@chmod a+x bin/*
find-no-copyright:
	@find . -name "*.java" \( ! -exec grep -q "GNU GPL" "{}" ";" \) -print
find-import-star:
	@grep -l "import.*\*;" $(filter-out GNUmakefile,$(TARSOURCE)) || true

wc:
	@echo Top Five:
	@wc -l $(filter-out Contrib/%,$(TARSOURCE)) \
		| sort -n | tail -6 | head -5
	@echo Total lines of source:
	@cat $(filter-out Contrib/%,$(TARSOURCE)) | wc -l

clean:
	-${RM} -r harpoon silicon gnu Harpoon.jar* harpoon.tgz* \
		VERSIONS ChangeLog $(MACHINE_GEN)
	-${RM} java `find . -name "*.class"`

polish: clean
	-${RM} *~ */*~ `find . -name "*.java~"` core

wipe:	clean doc-clean

NONLOCAL=$(shell if [ ! `hostname` = "lesser-magoo.lcs.mit.edu" ]; then echo $(SSH) cananian@lesser-magoo.lcs.mit.edu ; fi)
backup: only-me # SLOW ON NON-LOCAL MACHINES
	$(NONLOCAL) tar -C ~/Harpoon -c CVSROOT | \
	   $(SSH) catfish.lcs.mit.edu \
	      "gzip -9 -c > public_html/Projects/Harpoon/flex-backup.tar.gz"

# some rules only make sense if you're me.
only-me:
	if [ ! `whoami` = "cananian" ]; then exit 1; fi

# the 'cvs' rules only make sense if you've got a copy checked out from CVS
needs-cvs:
	@if [ ! -d CVS ]; then \
	  echo This rule needs CVS access to the source tree. ; \
	   exit 1; \
	fi

install: jar-install tar-install doc-install

# [AS + Ovy] - recompile only one class:
# Usage: harpoon/[somepath]/[someclass.class]
# Warning: don't use it if you change some API. 
harpoon/%.class: %.java
	${JCC} ${JFLAGS} $*.java
