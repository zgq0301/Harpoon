// RawP.java, created by cananian
// Copyright (C) 2002 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.ClassFile.Loader;
import harpoon.IR.RawClass.AccessFlags;
import harpoon.IR.RawClass.Attribute;
import harpoon.IR.RawClass.AttributeCode;
import harpoon.IR.RawClass.AttributeExceptions;
import harpoon.IR.RawClass.AttributeLineNumberTable;
import harpoon.IR.RawClass.AttributeLocalVariableTable;
import harpoon.IR.RawClass.AttributeSignature;
import harpoon.IR.RawClass.AttributeSourceFile;
import harpoon.IR.RawClass.ClassFile;
import harpoon.IR.RawClass.ConstantClass;
import harpoon.IR.RawClass.FieldInfo;
import harpoon.IR.RawClass.LineNumberTable;
import harpoon.IR.RawClass.LocalVariableTable;
import harpoon.IR.RawClass.MethodInfo;
import harpoon.Util.Util;

import java.io.InputStream;
/**
 * <code>Javap</code> is a low-level clone of javap that supports
 * GJ signatures.
 * 
 * @author  C. Scott Ananian <cananian@lesser-magoo.lcs.mit.edu>
 * @version $Id: Javap.java,v 1.14 2003-09-05 21:49:52 cananian Exp $
 */
public abstract class Javap /*extends harpoon.IR.Registration*/ {
    /** Print out disassembled code. */
    public static boolean DISASSEMBLE=false;
    /** Print out help message. */
    public static boolean HELP=false;
    /** Indent members (fields/methods). */
    public static boolean INDENT=true;
    /** Print out line number tables. */
    public static boolean LINE_NUMBER_TABLE=false;
    /** Print out local variable tables. */
    public static boolean LOCAL_VARIABLE_TABLE=false;
    /** Show only public classes and members. */
    public static boolean PUBLIC_ONLY=false;
    /** Show protected/public classes and members. */
    public static boolean PUBLIC_PROTECTED_ONLY=false;
    /** Show package/protected/public classes and members. */
    public static boolean PUBLIC_PROTECTED_PACKAGE_ONLY=false;//default
    /** Show all classes and members. */
    public static boolean PUBLIC_PROTECTED_PACKAGE_PRIVATE=false;
    /** Print internal type signatures. */
    public static boolean SIGNATURES=false;
    /** Print stack size, number of locals, and number of method args. */
    public static boolean MORE_INFO=false;

    public static final void main(String[] args) {
	String[] classes = parseOpts(args);
	for (int i=0; i<classes.length; i++)
	    doClass(classes[i]);
    }

    public static String[] parseOpts(String[] opts) {
	int optNum = 0;
	while (optNum < opts.length && opts[optNum].startsWith("-")) {
	    String opt = opts[optNum++].intern();
	    if (false) ;
	    else if (opt=="-c") DISASSEMBLE=true;
	    else if (opt=="-help") HELP=true;
	    else if (opt=="-l") { LINE_NUMBER_TABLE=LOCAL_VARIABLE_TABLE=true;}
	    else if (opt=="-public") PUBLIC_ONLY=true;
	    else if (opt=="-protected") PUBLIC_PROTECTED_ONLY=true;
	    else if (opt=="-package") PUBLIC_PROTECTED_PACKAGE_ONLY=true;
	    else if (opt=="-private") PUBLIC_PROTECTED_PACKAGE_PRIVATE=true;
	    else if (opt=="-s") SIGNATURES=true;
	    else if (opt=="-verbose") {
		DISASSEMBLE=true;
		LINE_NUMBER_TABLE=LOCAL_VARIABLE_TABLE=true;
		SIGNATURES=true;
		MORE_INFO=true;
	    } else {
		System.err.println("Unrecognized option: "+opt);
		HELP=true;
	    }
	}
	// don't indent if we're printing out additional per-member info:
	INDENT = !
	    (DISASSEMBLE || LINE_NUMBER_TABLE || LOCAL_VARIABLE_TABLE ||
	     SIGNATURES || MORE_INFO);
	// normalize public/protected/package/private
	if (PUBLIC_ONLY)
	    PUBLIC_PROTECTED_ONLY=false;
	if (PUBLIC_ONLY||PUBLIC_PROTECTED_ONLY)
	    PUBLIC_PROTECTED_PACKAGE_ONLY=false;
	if (PUBLIC_ONLY||PUBLIC_PROTECTED_ONLY||PUBLIC_PROTECTED_PACKAGE_ONLY)
	    PUBLIC_PROTECTED_PACKAGE_PRIVATE=false;
	if (!(PUBLIC_ONLY||PUBLIC_PROTECTED_ONLY||
	      PUBLIC_PROTECTED_PACKAGE_ONLY||PUBLIC_PROTECTED_PACKAGE_PRIVATE))
	    PUBLIC_PROTECTED_PACKAGE_ONLY=true; // default.
	// handle 'help' option.
	if (HELP) {
	    System.out.println
		("Usage: java "+Javap.class+" <options> <classes>...\n"+
		 "\n"+
		 "where options include:\n"+
		 /*
		 "   -c         Disassemble the code\n"+
		 */
		 "   -help      Print this usage message\n"+
		 "   -l         Print line number and local variable tables\n"+
		 "   -public    Show only public classes and members\n"+
		 "   -protected Show protected/public classes and members\n"+
		 "   -package   Show package/protected/public classes\n"+
		 "              and members (default)\n"+
		 "   -private   Show all classes and members\n"+
		 "   -s         Print internal type signatures\n"+
		 /*
                 "   -verbose   Print stack size, number of locals and args\n"+
		 "              for methods\n"+
		 */
		 // also to do: translate <clinit> and <init> method names.
		 "");
	    return new String[0];
	}
	// okay, return the non-option arguments.
	String[] classes = new String[opts.length-optNum];
	System.arraycopy(opts, optNum, classes, 0, opts.length - optNum);
	return classes;
    }

    public static void doClass(String classname) {
	InputStream is = 
	    Loader.getResourceAsStream(Loader.classToResource(classname));
	if (is==null) throw new NoClassDefFoundError(classname);
	ClassFile raw = new ClassFile(is);
	try { is.close(); } catch (java.io.IOException ex) { /* ignore */ }

	// print "Compiled from"
	AttributeSourceFile asf = (AttributeSourceFile)
	    findAttribute(raw, AttributeSourceFile.ATTRIBUTE_NAME);
	if (asf!=null)
	    System.out.println("Compiled from \""+asf.sourcefile()+"\"");
	// get generic signature.
	AttributeSignature asig = (AttributeSignature)
	    findAttribute(raw, AttributeSignature.ATTRIBUTE_NAME);
	String gjsig = (asig==null) ? null : asig.signature();
	// print class modifiers.
	System.out.print(modString(raw.access_flags, true));
	// now print class name
	System.out.print(desc2name("L"+raw.this_class().name()+";"));
	// print formal type parameters.
	if (gjsig!=null && gjsig.charAt(0)=='<') {
	    OffsetAndString oas = munchParamPart(gjsig);
	    System.out.print(oas.string);
	    gjsig = gjsig.substring(oas.offset);
	}
	// supertype.
	if (raw.super_class()!=null) { // only null for java.lang.Object.
	    System.out.print(" extends ");
	    if (gjsig==null)
		System.out.print(desc2name("L"+raw.super_class().name()+";"));
	    else {
		OffsetAndString oas = munchClassTypeSig(gjsig);
		System.out.print(oas.string);
		gjsig = gjsig.substring(oas.offset);
	    }
	}
	// interfaces
	if (raw.interfaces_count() > 0) {
	    System.out.print(" implements ");
	    for (int i=0; i<raw.interfaces_count(); i++) {
		if (gjsig==null)
		    System.out.print
			(desc2name("L"+raw.interfaces(i).name()+";"));
		else {
		    OffsetAndString oas = munchClassTypeSig(gjsig);
		    System.out.print(oas.string);
		    gjsig = gjsig.substring(oas.offset);
		}
		if (i+1 < raw.interfaces_count())
		    System.out.print(", ");
	    }
	}
	System.out.println();
	if (MORE_INFO) {
	    if (asf!=null)
		System.out.println("  SourceFile: \""+asf.sourcefile()+"\"");
	    System.out.println("  minor version: "+raw.minor_version);
	    System.out.println("  major version: "+raw.major_version);
	    System.out.println("  Constant pool:");
	    for (int i=1; i<raw.constant_pool.length; ) {
		System.out.println("const #"+i+" = "+raw.constant_pool[i]);
		i+=raw.constant_pool[i].entrySize();
	    }
	}
	System.out.println("{");
	// fields.
	for (int i=0; i<raw.fields_count(); i++) {
	    FieldInfo fi = raw.fields[i];
	    AttributeSignature fis = (AttributeSignature)
		findAttribute(fi.attributes,AttributeSignature.ATTRIBUTE_NAME);
	    if (!access_flags_okay(fi.access_flags)) continue; // skip
	    if (INDENT) System.out.print("    "); // indent.
	    // access flags
	    System.out.print(modString(fi.access_flags, false));
	    // type/signature.
	    if (fis==null)
		System.out.print(desc2name(fi.descriptor()));
	    else
		System.out.print(munchTypeSig(fis.signature()).string);
	    System.out.print(" ");
	    // field name.
	    System.out.print(fi.name());
	    System.out.print(";");
	    System.out.println();
	    if (SIGNATURES) {
		System.out.println("  Signature: "+fi.descriptor());
		if (fis!=null)
		    System.out.println("  Generic Signature: "+fis.signature());
	    }
	    if (!INDENT) System.out.println(); // match Sun javap spacing
	}
	// methods.
	for (int i=0; i<raw.methods_count(); i++) {
	    MethodInfo mi = raw.methods[i];
	    AttributeSignature mis = (AttributeSignature)
		findAttribute(mi.attributes,AttributeSignature.ATTRIBUTE_NAME);
	    if (!access_flags_okay(mi.access_flags)) continue; // skip
	    // assign descriptor.
	    String md;
	    if (mis!=null) md = mis.signature();
	    else {
		md = mi.descriptor();
		// add throws clauses.
		AttributeExceptions ae = (AttributeExceptions)
		    findAttribute(mi.attributes,
				  AttributeExceptions.ATTRIBUTE_NAME);
		for (int j=0; ae!=null && j<ae.number_of_exceptions(); j++) {
		    ConstantClass cc = ae.exception_index_table(j);
		    if (cc==null) continue;
		    md+="^L"+cc.name()+";";
		}
	    }
	    // is this a varargs method?
	    // XXX the "Varargs" attribute is just temporary for the
	    // prototype compilers.  Java 1.5 will introduce a mask bit of
	    // some kind to indicate varargs.
	    boolean isVarArgs= (null!=findAttribute(mi.attributes, "Varargs"));
	    // indent.
	    if (INDENT) System.out.print("    ");
	    // access flags
	    System.out.print(modString(mi.access_flags, false));
	    // type formal parameters
	    if (md.charAt(0)=='<') {
		OffsetAndString oas = munchParamPart(md);
		System.out.print(oas.string);
		System.out.print(" ");
		md = md.substring(oas.offset);
	    }
	    // return type (skip to the end...)
	    OffsetAndString ret_oas = munchTypeSig
		(md.substring(md.indexOf(')')+1));
	    System.out.print(ret_oas.string);
	    System.out.print(" ");
	    // method name!
	    System.out.print(mi.name());
	    // parameters.
	    System.out.print('(');
	    assert md.charAt(0)=='(';
	    for (int off=1; md.charAt(off)!=')'; ) {
		OffsetAndString param_oas = munchTypeSig(md.substring(off));
		if (off!=1) System.out.print(", ");
		if (isVarArgs &&
		    md.charAt(off+param_oas.offset)==')' /* last arg */) {
		    // print var arg parameter, omitting outermost array spec
		    assert md.charAt(off)=='[';
		    System.out.print(munchTypeSig(md.substring(off+1)).string);
		    System.out.print("...");
		} else // ordinary, non-vararg parameter
		    System.out.print(param_oas.string);
		off += param_oas.offset;
	    }
	    System.out.print(')');
	    // 'throws' list.
	    md = md.substring(md.indexOf(')')+1+ret_oas.offset);
	    for (int off=0; off < md.length() && md.charAt(off)=='^'; ) {
		off++;
		OffsetAndString thr_oas = munchTypeSig(md.substring(off));
		if (off==1) System.out.print(" throws ");
		else System.out.print(", ");
		System.out.print(thr_oas.string);
		off += thr_oas.offset;
	    }
	    // done!
	    System.out.print(';');
	    System.out.println();
	    // print signatures
	    if (SIGNATURES) {
		System.out.println("  Signature: "+mi.descriptor());
		if (mis!=null)
		    System.out.println("  Generic Signature: "+mis.signature());
	    }
	    // print code
	    AttributeCode ac = (AttributeCode)
		findAttribute(mi.attributes, AttributeCode.ATTRIBUTE_NAME);
	    if (DISASSEMBLE && ac!=null) {
		System.out.println("  Code:");
		if (MORE_INFO) {
		    System.out.println("Stack="+ac.max_stack+", "+
				       "Locals="+ac.max_locals+", "+
				       "Args_size=?");
		}
		System.out.println("   0:   aload_0");
 		assert false : "unimplemented";
	    }
	    // print line number table
	    AttributeLineNumberTable alnt = (AttributeLineNumberTable)
		(ac==null ? null :
		 findAttribute(ac.attributes,
			       AttributeLineNumberTable.ATTRIBUTE_NAME));
	    if (LINE_NUMBER_TABLE && alnt!=null) {	
		LineNumberTable[] lnt = alnt.line_number_table;
		System.out.println("  LineNumberTable:");
		for (int j=0; j<lnt.length; j++)
		    System.out.println("   line "+lnt[j].line_number+": "+
				       lnt[j].start_pc);
	    }
	    // print local variable table
	    AttributeLocalVariableTable alvt = (AttributeLocalVariableTable)
		(ac==null ? null :
		 findAttribute(ac.attributes,
			       AttributeLocalVariableTable.ATTRIBUTE_NAME));
	    if (LOCAL_VARIABLE_TABLE && alvt!=null) {
		LocalVariableTable[] lvt = alvt.local_variable_table;
		System.out.println("  LocalVariableTable:");
		System.out.println("   Start  Length  Slot  Name   Signature");
		for (int j=0; j<lvt.length; j++)
		    System.out.println("   "+lvt[j].start_pc+
				       "      "+lvt[j].length+
				       "      "+lvt[j].index+
				       "    "+lvt[j].name()+
				       "       "+lvt[j].descriptor());
	    }
	    if (!INDENT) System.out.println(); // match Sun javap spacing
	}
	System.out.println("}");
    }

    static Attribute findAttribute(ClassFile cf, String name) {
	return findAttribute(cf.attributes, name);
    }
    static Attribute findAttribute(Attribute[] attributes, String name) {
	for (int i=0; i<attributes.length; i++)
	    if (attributes[i].attribute_name().equals(name))
		return attributes[i];
	return null;
    }

    static boolean access_flags_okay(AccessFlags af) {
	if (PUBLIC_ONLY)
	    return af.isPublic();
	else if (PUBLIC_PROTECTED_ONLY)
	    return af.isPublic() || af.isProtected();
	else if (PUBLIC_PROTECTED_PACKAGE_PRIVATE)
	    return true;
	else assert PUBLIC_PROTECTED_PACKAGE_ONLY;
	return !af.isPrivate();
    }
    static String modString(AccessFlags af, boolean isClass) {
	StringBuffer sb = new StringBuffer();
	if (af.isPrivate()) sb.append("private ");
	if (af.isProtected()) sb.append("protected ");
	if (af.isPublic()) sb.append("public ");
	if (af.isAbstract() && !af.isInterface())
	    sb.append("abstract ");
	if (af.isStatic()) sb.append("static ");
	if (af.isFinal()) sb.append("final ");
	if (af.isTransient()) sb.append("transient ");
	if (af.isVolatile()) sb.append("volatile ");
	if (af.isSynchronized() && !isClass) sb.append("synchronized ");
	if (af.isNative()) sb.append("native ");
	if (af.isStrict()) sb.append("strict ");
	if (af.isInterface()) sb.append("interface ");
	else if (isClass) sb.append("class ");
	return sb.toString();
    }
    static String desc2name(String descriptor) {
	switch(descriptor.charAt(0)) {
	case '[': // arrays
	    return desc2name(descriptor.substring(1))+"[]";
	 case 'L': // object type.
	     return descriptor
		 .substring(1, descriptor.indexOf(';'))
		 .replace('/','.');
	    // primitive types
	case 'B': return "byte";
	case 'C': return "char";
	case 'D': return "double";
	case 'F': return "float";
	case 'I': return "int";
	case 'J': return "long";
	case 'S': return "short";
	case 'Z': return "boolean";
	case 'V': return "void";
	default:
	    assert false : "bad descriptor: "+descriptor;
	    return "<unknown>";
	}
    }
    // more sophisticated parser, for gj sigs.
    static class OffsetAndString {
	final String string;
	final int offset;
	OffsetAndString(String string, int offset) {
	    this.string = string; this.offset = offset;
	}
    }
    static OffsetAndString munchParamPart(String psig) {
	assert psig.charAt(0)=='<' && psig.indexOf('>')>0;
	StringBuffer sb = new StringBuffer("<");
	int off = 1;
	boolean first = true;
	while (psig.charAt(off)!='>') {
	    int colon = psig.indexOf(':', off);
	    String name = psig.substring(off, colon);
	    off = colon;
	    // make sb.
	    if (first) first=false;
	    else sb.append(", ");
	    sb.append(name);
	    // back to parsing.
	    boolean firstbound=true;
	    // note that bounds of
	    //    ':Ljava/lang/Object/Object;:Ljava/lang/Comparable;'
	    // is different from (has a different erasure than)
	    //    '::Ljava/lang/Comparable;'
	    // [The first is declared as 'extends Object & Comparable'
	    //  while the second is declared as 'extends Comparable' ]
	    while (psig.charAt(off)==':') {
		off++;
		if (psig.charAt(off)==':') {
		    // no class type specified.
		    continue;
		}
		OffsetAndString oas = munchTypeSig(psig.substring(off));
		off += oas.offset;
		// suppress 'extends java.lang.Object' with no other bounds.
		if (oas.string.equals("java.lang.Object") &&
		    psig.charAt(off)!=':') continue;
		if (firstbound) { sb.append(" extends "); firstbound=false; }
		else sb.append(" & ");
		sb.append(oas.string);
	    }
	}
	off++;
	sb.append(">");
	return new OffsetAndString(sb.toString(), off);
    }
    static OffsetAndString munchClassTypeSig(String descriptor) {
	assert descriptor.charAt(0)=='L';
	StringBuffer sb = new StringBuffer();
	int off;
	int semi = descriptor.indexOf(';');
	int brack= descriptor.indexOf('<');
	if (brack > semi) brack=-1;
	String id = ((brack!=-1) ?
		     descriptor.substring(1, brack) :
		     descriptor.substring(1, semi))
	    .replace('/','.');
	sb.append(id);
	if (brack!=-1) {
	    // ooh, ooh, type parameters!
	    off = brack;
	    OffsetAndString oas = munchTypeArguments
		(descriptor.substring(off));
	    sb.append(oas.string);
	    off+=oas.offset;
	    assert descriptor.charAt(off)==';';
	    off++;
	} else {
	    off = semi+1;
	}
	// optional '.L ID<type args>;'
	while (off < descriptor.length() && descriptor.charAt(off)=='.') {
	    assert descriptor.charAt(off+1)=='L';
	    off+=2;
	    int semi2 = descriptor.indexOf(';', off);
	    int brack2= descriptor.indexOf('<', off);
	    // find the proper name.
	    if (brack2 > semi2) brack2=-1;
	    String id2 = ((brack2!=-1) ?
			  descriptor.substring(off, brack2) :
			  descriptor.substring(off, semi2))
		.replace('/','.');
	    // remove bits we've already emitted & emit the rest.
	    assert id2.startsWith(id);
	    assert id2.substring(id.length()).startsWith("$");
	    sb.append('.');
	    sb.append(id2.substring(1+id.length()));
	    id=id2;
	    if (brack2!=-1) {
		// optional type parameters
		off = brack2;
		OffsetAndString oas = munchTypeArguments
		    (descriptor.substring(off));
		sb.append(oas.string);
		off+=oas.offset;
		assert descriptor.charAt(off)==';';
		off++;
	    } else {
		off = semi2+1;
	    }
	}
	// okay, finally done.
	return new OffsetAndString(sb.toString(), off);
    }
    static OffsetAndString munchTypeArguments(String descriptor) {
	assert descriptor.charAt(0)=='<';
	assert descriptor.indexOf('>')!=-1;
	StringBuffer sb = new StringBuffer();
	int off = 1;
	sb.append('<');
	boolean first=true;
	while (descriptor.charAt(off)!='>') {
	    OffsetAndString oas =
		munchVariantTypeSig(descriptor.substring(off));
	    if (first) first=false;
	    else sb.append(", ");
	    sb.append(oas.string);
	    off+=oas.offset;
	}
	assert descriptor.charAt(off)=='>';
	sb.append('>');
	off++;
	return new OffsetAndString(sb.toString(), off);
    }
    static OffsetAndString munchMethodTypeSig(String descriptor) {
	assert descriptor.charAt(0)=='<' || descriptor.charAt(0)=='(';
	StringBuffer sb = new StringBuffer();
	int off = 0;
	if (descriptor.charAt(off)=='<') {
	    OffsetAndString oas = munchParamPart(descriptor);
	    sb.append(oas.string);
	    off+=oas.offset;
	}
	assert descriptor.charAt(off)=='(';
	off++;
	while (descriptor.charAt(off)!=')') {
	    // method parameters
	    OffsetAndString oas = munchTypeSig(descriptor.substring(off));
	    off+=oas.offset;
	}
	off++;
	// return value type.
	{
	    OffsetAndString oas = munchTypeSig(descriptor.substring(off));
	    off+=oas.offset;
	}
	// optional throws signatures.
	while (off < descriptor.length() && descriptor.charAt(off)=='^') {
	    off++;
	    OffsetAndString oas = munchTypeSig(descriptor.substring(off));
	    off+=oas.offset;
	}
	// done.
	return new OffsetAndString(sb.toString(), off);
    }
    static OffsetAndString munchVariantTypeSig(String descriptor) {
	StringBuffer sb = new StringBuffer();
	int off=0;
	// first characters in signature could be variance specifiers -/+/=/*
	switch(descriptor.charAt(off)) {
	case '*':
	    return new OffsetAndString("?", 1);
	case '-':
	    sb.append("? super "); off++; break;
	case '+':
	    sb.append("? extends "); off++; break;
	case '=':
	    assert false : "= is no longer supported in GJ prototype 2.2";
	    sb.append(descriptor.charAt(off++));
	    break;
	}
	// rest is a standard type signature
	OffsetAndString oas = munchTypeSig(descriptor.substring(off));
	sb.append(oas.string);
	off+=oas.offset;
	return new OffsetAndString(sb.toString(), off);
    }
    static OffsetAndString munchArrayTypeSig(String descriptor) {
	assert descriptor.charAt(0)=='[';
	StringBuffer sb = new StringBuffer();
	int off=0;
	while (descriptor.charAt(off)=='[') {
	    sb.append('[');
	    // there could be variance specifiers -/+/=
	    switch(descriptor.charAt(++off)) { // no '*' allowed!
	    case '-':
	    case '+':
	    case '=':
		assert false : "array variance no longer supported";
		sb.append(descriptor.charAt(off++));
		break;
	    }
	    sb.append(']');
	}
	// okay, parse the rest
	OffsetAndString oas = munchTypeSig(descriptor.substring(off));
	sb.insert(0, oas.string); // reverse the pieces.
	off+=oas.offset;
	// done!
	return new OffsetAndString(sb.toString(), off);
    }
    static OffsetAndString munchTypeSig(String descriptor) {
	switch(descriptor.charAt(0)) {
	case '[': // arrays
	    return munchArrayTypeSig(descriptor);
	 case 'L': // object type.
	     return munchClassTypeSig(descriptor);
	case 'T': // type variable signature
	    return new OffsetAndString
		(descriptor.substring(1, descriptor.indexOf(';')),
		 descriptor.indexOf(';')+1);
	case '<':
	case '(': // method type signature
	    return munchMethodTypeSig(descriptor);
	    // primitive types
	case 'B': return new OffsetAndString("byte", 1);
	case 'C': return new OffsetAndString("char", 1);
	case 'D': return new OffsetAndString("double", 1);
	case 'F': return new OffsetAndString("float", 1);
	case 'I': return new OffsetAndString("int", 1);
	case 'J': return new OffsetAndString("long", 1);
	case 'S': return new OffsetAndString("short", 1);
	case 'Z': return new OffsetAndString("boolean", 1);
	case 'V': return new OffsetAndString("void", 1);
	default:
	    assert false : "bad descriptor: "+descriptor;
	    return new OffsetAndString("<unknown>", 1);
	}
    }
}
