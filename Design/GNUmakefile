# makefile.
LATEX=latex
BIBTEX=bibtex -min-crossrefs=9999
MAKEINDEX=makeindex
INSTALLMACHINE=magic@www.magic.lcs.mit.edu
INSTALLDIR=public_html/Harpoon/
export TEXINPUTS=/home/cananian/src/tex4ht//:

ALLDOCS=design bibnote readnote quads proposal thesis exec pldi99 pldi02 \
	pldi03 oopsla02 oqe lctes03 pldi04 ecoop04 popl05 hpec \
	oxygen3-talk phd-proposal phd-thesis

default: phd-thesis.ps # all
.PRECIOUS: %.dvi

## funky stuff for martin.
put:
	@echo I am assuming you have checked everything in.
	cd ~/Harpoon/oopsla ; cvs -f update
	make -C ~/Harpoon/oopsla put

get:
	make -C ~/Harpoon/oopsla get
	cd ~/Harpoon/oopsla ; cvs -f update
	@echo OK, now you have to review these changes and commit them.
### end martin's funky stuff.

all: $(ALLDOCS:=.ps)
preview: thesis-xdvi

# comdef dependencies
exec.dvi thesis.dvi proposal.dvi: comdef.sty
pldi99.dvi pldi99-outline.dvi: comdef.sty
pldi02.dvi: comdef.sty
pldi03.dvi: comdef.sty
lctes03.dvi: comdef.sty
p072-ananian.dvi: comdef.sty

# bibtex dependencies
exec.dvi quads.dvi design.dvi thesis.dvi: harpoon.bib
pldi99.dvi pldi99-outline.dvi: harpoon.bib
pldi02.dvi: harpoon.bib
pldi03.dvi: harpoon.bib
lctes03.dvi: harpoon.bib
p072-ananian.dvi: harpoon.bib
bibnote.dvi: harpoon_.bib
readnote.dvi: unread_.bib
phd-thesis.dvi: xaction.bib flex.bib

# lots of dependencies for the pldi99 paper
pldi99.dvi: pldi99-intro.tex pldi99-abstract.tex pldi99-tech.tex
pldi99.dvi: pldi99-example.tex pldi99-results.tex
pldi99.dvi: Figures/THussi.tex Figures/THv0.tex Figures/THscccomp.eps
pldi99.dvi: Figures/THex1ssi.tex Figures/THex1ssiPr.tex
pldi99.dvi: Figures/THlat1.tex Figures/THlat2.tex Figures/THlat3.tex
pldi99.dvi: Figures/THlat4.tex Figures/THlat5.tex Figures/THlat6.tex
pldi99.dvi: Figures/evil.tex

# more dependencies for the pldi02 paper.
pldi02.dvi: Figures/ptrsize.tex Figures/THlat6.tex
pldi02.dvi: Figures/standardAlignment.eps Figures/byteAlignment.eps
pldi02.dvi: Figures/bitAlignment.eps
pldi02.dvi: Figures/spec-space.eps Figures/spaceopt.eps

oopsla02.dvi:	Figures/THlat1b.tex Figures/THlat6b.tex Figures/spec-space.eps\
	Figures/oopsla-objalloc.eps Figures/oopsla-ttlalloc.eps \
	Figures/oopsla-ttllive.eps Figures/oopsla-speed.eps \
	Figures/oopsla-lat.tex

# oqe presentation dependencies.
oqe.dvi oqe-notes.tex oqe-adobe.tex \
lctes03-talk.dvi lctes03-talk-notes.tex lctes03-talk-adobe.tex:	\
	 PPRoqe.sty				\
	 Figures/Kontour/structure-bbox.eps	\
	 Figures/Kontour/strategy-bbox.eps	\
	 Figures/Kontour/how-none-bbox.eps	\
	 Figures/Kontour/how-fieldcomp-bbox.eps	\
	 Figures/Kontour/how-fieldelim-bbox.eps	\
	 Figures/Kontour/how-header-bbox.eps	\
	 Figures/Kontour/bwfieldcomp-bbox.eps	\
	 Figures/alignment2.eps			\
	 Figures/extmap1.eps Figures/extmap2.eps\
	 Figures/Kontour/hashcomp-bbox.eps	\
	 Figures/specclaz.eps			\
	 Figures/oopsla-ttllive-color.eps	\
	 Figures/oopsla-ttlalloc-color.eps	\
	 Figures/oopsla-objalloc-color.eps	\
	 Figures/oopsla-speed-color.eps		\
	 Figures/spec-space-2.eps		\
	 Figures/Kontour/classcomp-bbox.eps	\
	 Figures/THlat1b.tex			\
	 Figures/THlat6c.tex			\
	 Figures/THlat6c2.tex			\
	 Figures/Images/model-t-black.eps	\
	 Figures/Images/model-t-redblue.eps	\

# scool05 talk dependencies
SCOOL05: \
	$(foreach f, \
	  funarr-0 funarr-1 funarr-2 funarr-3 funarr-4 funarr-5 funarr-6 \
	  shallow-0 shallow-1 shallow-2 shallow-3 shallow-4 shallow-5 \
	  shallow-6 shallow-7 multi-o2 single-o2 \
	  scool-multi-obj-1 scool-multi-obj-2 scool-multi-obj-3 \
	  scool-multi-obj-4 scool-multi-obj-5 scool-multi-obj-6 \
	  scool-multi-obj-7 scool-multi-obj-8 scool-multi-obj-9 \
	  scool-multi-obj-10,\
	  Figures/$(f).emf)
# scool05 paper dependencies
scool05.dvi: \
	Figures/sean_lie_6b.eps Figures/nb-single-obj.eps \
	Figures/nb-multi-obj.eps Figures/chuang.eps Figures/funarr.eps \
	Figures/tr-sz-all.eps Figures/tr-w-all.eps Figures/bloat.eps \
	$(foreach f, tr-multi-obj tr-quad2,\
	  Figures/$(f).pstex Figures/$(f).pstex_t)
# pldi04 paper dependencies
pldi04.dvi ecoop04.dvi popl05.dvi: \
	Figures/nb-single-obj.eps Figures/nb-multi-obj.eps \
	Figures/chuang.eps Figures/funarr.eps Figures/bloat.eps \
	Figures/tr-w-all.eps Figures/tr-w-ten.eps Figures/tr-sz-all.eps \
	$(foreach f, tr-multi-obj tr-quad,\
	  Figures/$(f).pstex Figures/$(f).pstex_t)
ecoop04.dvi: csallncs.cls
popl05.dvi: csaacmconfbig.cls acmconfbig.cls

# Phd Thesis dependencies
phd-thesis.dvi: phd-thesis.cls phd-titlepage.tex phd-abstract.tex phd-ack.tex \
	phd-intro.tex phd-stm.tex phd-htm.tex phd-hybrid.tex \
	phd-chall.tex phd-related.tex phd-concl.tex phd-ver.tex phd-cmd.tex \
	phd-config.txt
#    figure dependencies
phd-thesis.dvi: \
	$(foreach f, \
	uvsimCounterRuntime \
	nb-single-obj nb-multi-obj \
	chuang-trim funarr bloat-color \
	tr-sz-all-1 tr-w-all-1 sean_lie_6b \
	phd-counter phd-counter-instr phd-arrayperf \
	phd-spec-nt phd-spec-nt-rate phd-spec-nt-pred phd-spec-full,\
	Figures/$(f).eps Figures/$(f).pdf) \
	$(foreach f, \
	cachemods snapshot datastruct-entry tr-quad2 tr-multi-obj-big,\
	Figures/$(f).pstex Figures/$(f).pstex_t \
	Figures/$(f).pdf Figures/$(f).eps)
phd-thesis.pdf: phd-thesis.dvi
	pdflatex phd-thesis && pdflatex phd-thesis && pdflatex phd-thesis
phd-thesis.munged: phd-thesis.tex phd-titlepage.tex phd-abstract.tex phd-ack.tex phd-intro.tex phd-stm.tex phd-htm.tex \
        phd-hybrid.tex phd-chall.tex phd-related.tex phd-concl.tex phd-ver.tex phd-cmd.tex
	munge $^ > $@
phd-thesis-unmunge:
	unmunge phd-thesis.munged
phd-defense: $(foreach f, snapshot-color-2 cachemods-color-2 \
	$(foreach n, 1 2 3 4 5 6 7 8 9 10, phd-multi-obj-$(n)), \
	Figures/$(f).emf)
.PHONY: phd-defense

martin-happy: phd-thesis.pdf
	rsync -avz phd-thesis.pdf vhost.cscott.net:web/cscott.net/Publications/
	mkdir -p phd-bak
	cp phd-thesis.pdf phd-bak/phd-thesis-`date "+%Y%m%d-%H%M"`.pdf

# masters thesis figure dependencies
export THESIS_FIGURES=\
	Figures/THundir.fig \
	Figures/THcqdata.tex Figures/THcqalg.tex Figures/THcqex.fig \
	Figures/THsesedata.tex Figures/THsesealg.tex \
	Figures/THpst.fig Figures/evil.fig \
	Figures/THdeaddata.tex Figures/THdeadalg.tex Figures/THlattice.fig \
	Figures/THlat1.fig Figures/THlat2.fig \
	Figures/THlat3.fig Figures/THlat4.fig Figures/THlat5.fig \
	Figures/THsccalg1.tex Figures/THsccalg2.tex Figures/THsccssi.tex \
	Figures/THscctyped.tex Figures/THsptc.tex \
	Figures/THssiren1.tex Figures/THssiren2.tex Figures/THssirend.tex \
	Figures/THmorephi.fig Figures/THssi2ssa.tex \
	Figures/THlat6.fig
thesis.dvi: $(patsubst %.fig,%.tex,$(THESIS_FIGURES)) \
	Figures/THex1base.tex \
	Figures/THex1ssa.tex Figures/THex1ssaPr.tex \
	Figures/THex1ssi.tex Figures/THex1ssiPr.tex \
	Figures/THcqex2.tex Figures/phisig.tex \
	Figures/THussi.tex Figures/THv0.tex Figures/THscccomp.eps

# masters thesis figure rules
Figures/%: always
	@$(MAKE) --no-print-directory -C Figures $*
always:

# change prosper options.
%-adobe.tex: %.tex
	sed -e 's.\\documentclass\[.\\documentclass[distiller,.' < $< > $@
%-notes.tex: %.tex
	sed -e 's.\\documentclass\[.\\documentclass[ps,slideBW,nocolorBG,accumulate,.' -e '/documentclass/,/prosper/{' -e 's/pdf,/ps,/' -e 's/slideColor,/slideBW,/' -e 's/^colorBG,/nocolorBG,/' -e '}' < $< > $@
%-4.ps: %.ps
	psnup -4 -Pletter -ld < $< > $@

# distill on catfish. (has to precede normal dvi-to-pdf rules)
%-adobe.pdf: %-adobe.dvi
# prosper wants -t a4 here, but we've made PPRoqe look best in letter.
	dvips -t letter -e 0 -f < $< \
	| ssh catfish.lcs.mit.edu \
	"mkdir csa-foo ; cd csa-foo ; cat > $*.ps ; distill $*.ps ; acroexch $*.pdf ; cat $*.pdf ; cd .. ; /bin/rm -rf csa-foo" \
	> $@

# Tex rules. [have to add explicit dependencies on appropriate bibtex files.]
%.dvi %.aux: %.tex
	$(LATEX) $*
	if egrep -s '^[^%]*\\bibliography' $< ; then $(BIBTEX) $*; fi
	if egrep -s '^[^%]*\\makeindex' $< ; then $(MAKEINDEX) $*; fi
	if egrep -s 'Rerun to get cross-r|Citation.*undefined' $*.log; then \
		$(LATEX) $*; fi
	if egrep -s 'Rerun to get cross-r' $*.log; then $(LATEX) $*; fi
	@if egrep -s 'undefined references|Citation.*undefined' $*.log; then \
		grep undefined $*.log; fi

# Make annotation-visible versions of bibtex files.
%_.bib : %.bib
	sed -e "s/^  note =/  Xnote =/" -e "s/^  annote =/  note =/" \
		$< > $@
# dvi-to-postscript-to-acrobat chain.
%.ps : %.dvi
	dvips -P cmz -t letter -e 0 -o $@ $<
# XXX dvipdf runs the dvips | ps2pdf chain, but without the required
#     arguments to dvips.  For our LCTES paper, this made the margins
#     screwy.  So just run the chain manually ourselves.
#%.pdf : %.dvi
#	dvipdf -dCompatibilityLevel=1.3 -dDoThumbnails=true $< $@
%.pdf : %.ps
	ps2pdf14 -dDoThumbnails=true $< $@
%-xdvi : %.dvi
	@if ps w | grep -v grep | grep -q "xdvi $*.dvi" ; then \
		echo "Xdvi already running." ; \
	else \
		(xdvi $< &) ; \
	fi

# rules for LCTES camera-ready copy.
p072-ananian.tex: lctes03.tex lctes03.dvi GNUmakefile
	sed -e '/^\\bibliographystyle/d' -e '/^\\bibliography{/,$$ d' < $< > $@
	echo '\balancecolumns' | sed -e '92 r /dev/stdin' lctes03.bbl >> $@
	sed -e '0,/^\\bibliography{/ d' < $< >> $@

# progress graphs.
%.stats: %.tex
	scripts/make.stats $< > $@
%.gif: %.stats
	scripts/make.graph $*

allthesis.stats: thesis.stats Figures/THex1.fig.stats \
		$(patsubst %,%.stats,$(THESIS_FIGURES))
	(head -1 thesis.stats ; (cat $^ | sed -e '/^[^0-9]/d' \
	  -e 's.\([0-9][0-9]/[0-9][0-9]\)/\([0-9][0-9][0-9][0-9]\).\2/\1.g' \
	| sort -r | sed \
	  -e 's.\([0-9][0-9][0-9][0-9]\)/\([0-9][0-9]/[0-9][0-9]\).\2/\1.g' \
	) ) > $@

# latex2html

html/% : %.dvi %.aux %.ps %.pdf
	if [ ! -d html ]; then mkdir html; fi
	-$(RM) -r $@
	latex2html -local_icons -dir $@ $*
	ln $*.ps $@
	ln $*.pdf $@
	date '+%-d-%b-%Y at %r %Z.' > $@/TIMESTAMP

html: $(foreach doc,$(ALLDOCS),html/$(doc))

html-install: html
	chmod a+r html/*/* ; chmod a+rx html/*
	ssh $(INSTALLMACHINE) /bin/rm -rf \
		$(foreach doc,$(ALLDOCS),$(INSTALLDIR)/$(doc))
	cd html; scp -r $(ALLDOCS) $(INSTALLMACHINE):$(INSTALLDIR)

install: html-install

update:
	cvs update -Pd

clean:
	$(RM) *.aux *.bbl *.blg *.dvi *.glo *.gls *.idx *.ilg *.ind *.lis \
	      *.loa *.lof *.log *.lot *.toc
	$(RM) $(foreach doc,$(ALLDOCS),$(doc).ps $(doc).pdf $(doc).gif $(doc).stats) allthesis.stats allthesis.gif
	$(RM) harpoon_.bib unread_.bib
	$(RM) -r html
	$(MAKE) -C Figures clean

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

# performance:
.PHONY: clean wipe backup only-me update install
.PHONY: html html-pdf html-install
.PHONY: preview all
.PHONY: always

# Try to convince make to delete these sometimes.
.INTERMEDIATE: $(ALLDOCS:=.log)
.INTERMEDIATE: %_.bib
.SECONDARY: $(ALLDOCS:=.dvi) $(ALLDOCS:=.aux)
