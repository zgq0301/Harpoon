# makefile.
INSTALLMACHINE=magic@www.magic.lcs.mit.edu
INSTALLDIR=public_html/Harpoon/

all: design.ps bibnote.ps readnote.ps quads.dvi
preview: quads-xdvi

# bibtex dependencies
quads.dvi: harpoon.bib
design.dvi: harpoon.bib
bibnote.dvi: harpoon_.bib
readnote.dvi: unread_.bib

# Tex rules. [have to add explicit dependencies on appropriate bibtex files.]
%.dvi : %.tex
	latex $(basename $<)
	bibtex $(basename $<)
	latex $(basename $<)
	latex $(basename $<)
# Make annotation-visible versions of bibtex files.
%_.bib : %.bib
	sed -e "s/^  note =/  Xnote =/" -e "s/^  annote =/  note =/" \
		$< > $@
# dvi-to-postscript-to-acrobat chain.
%.ps : %.dvi
	dvips -o $@ $<
%.pdf : %.ps
	ps2pdf $< $@
%-xdvi : %.dvi
	@if ps | grep -v grep | grep -q "xdvi $*.dvi" ; then \
		echo "Xdvi already running." ; \
	else \
		(xdvi $*.dvi &) ; \
	fi

# latex2html
html: design.dvi bibnote.dvi quads.dvi
	$(RM) -r html ; mkdir html
	latex2html -local_icons -dir html/design design
	latex2html -local_icons -dir html/biblio bibnote
	latex2html -local_icons -dir html/quads  quads
	date '+%-d-%b-%Y at %r %Z.' > html/design/TIMESTAMP
	date '+%-d-%b-%Y at %r %Z.' > html/biblio/TIMESTAMP
	date '+%-d-%b-%Y at %r %Z.' > html/quads/TIMESTAMP
html-pdf: html design.pdf bibnote.pdf quads.pdf
	$(RM) html/design/design.pdf \
	      html/biblio/bibnote.pdf \
              html/quads/quads.pdf
	ln design.pdf html/design
	ln bibnote.pdf html/biblio 
	ln quads.pdf html/quads
html-install: html-pdf
	chmod a+r html/*/* ; chmod a+rx html/*
	ssh $(INSTALLMACHINE) /bin/rm -rf \
		$(INSTALLDIR)/design \
		$(INSTALLDIR)/biblio \
		$(INSTALLDIR)/quads
	cd html; scp -r design biblio quads \
		$(INSTALLMACHINE):$(INSTALLDIR)

install: html-install

clean:
	$(RM) *.dvi *.log *.aux *.bbl *.blg
	$(RM) design.ps design.pdf \
	      bibnote.ps bibnote.pdf \
	      readnote.ps readnote.pdf \
	      quads.ps quads.pdf
	$(RM) harpoon_.bib unread_.bib
	$(RM) -r html

wipe: clean
	$(RM) *~ core

backup: only-me # DOESN'T WORK ON NON-LOCAL MACHINES
	if [ ! `hostname` = "lesser-magoo.lcs.mit.edu" ]; then exit 1; fi
	$(RM) ../harpoon-backup.tar.gz
	cd ..; tar czvf harpoon-backup.tar.gz CVSROOT
	scp ../harpoon-backup.tar.gz \
		miris.lcs.mit.edu:public_html/Projects/Harpoon
	$(RM) ../harpoon-backup.tar.gz

# some rules only makes sense if you're me.
only-me:
	if [ ! `whoami` = "cananian" ]; then exit 1; fi

# Try to convince make to delete these sometimes.
.INTERMEDIATE: %.aux %.log
.INTERMEDIATE: %_.bib
