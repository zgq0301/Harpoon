## Process this file with automake to produce Makefile.in
MAINTAINERCLEANFILES = Makefile.in
INCLUDES = @IFLAGS@
LIBS =

noinst_LTLIBRARIES = librtjperf.la

# SysInfo.h is a source, but it is generated by ./configure and so doesn't
# need to be mentioned here.
librtjperf_la_SOURCES = edu_uci_ece_ac_time_HighResClock.c \
	edu_uci_ece_ac_time_HighResClock.h
