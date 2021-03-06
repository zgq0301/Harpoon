package org.jacorb.orb.dynany;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose.
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

import org.omg.DynamicAny.NameValuePair;
import org.omg.DynamicAny.DynAnyPackage.*;
import org.omg.DynamicAny.NameDynAnyPair;
import org.jacorb.orb.*;

/**
 * CORBA DynStruct
 *
 * @author (c) Gerald Brose, FU Berlin 1999
 * $Id: DynStruct.java,v 1.1 2003-04-03 16:53:46 wbeebee Exp $
 */

public final class DynStruct
    extends DynAny
    implements org.omg.DynamicAny.DynStructOperations
{
    private org.omg.DynamicAny.NameValuePair[] members;

    DynStruct( org.jacorb.orb.ORB orb, 
               org.omg.DynamicAny.DynAnyFactory dynFactory,
               org.jacorb.orb.Any any)
	throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, InvalidValue
    {
	super(orb,dynFactory,any);
    }

    DynStruct( org.jacorb.orb.ORB orb, 
               org.omg.DynamicAny.DynAnyFactory dynFactory, 
               org.omg.CORBA.TypeCode tc)
	throws InvalidValue, TypeMismatch
    {
	if( tc.kind().value() != org.omg.CORBA.TCKind._tk_except && 
	    tc.kind().value() != org.omg.CORBA.TCKind._tk_struct )
	    throw new TypeMismatch();

	this.orb = orb;
	this.dynFactory = dynFactory;
	type = tc;

	/* initialize for empty exceptions */
	
	try
	{
	    if( tc.kind().value()== org.omg.CORBA.TCKind._tk_except && 
		tc.member_count() == 0)
	    {
		pos = 0;
	    }

	    limit = tc.member_count();
	    members = new NameValuePair[limit];
	    for( int i = 0 ; i < limit; i++ )
	    {		
		members[i]= 
                    new NameValuePair(
                          tc.member_name(i),
                          dynFactory.create_dyn_any_from_type_code(
                                         tc.member_type(i)).to_any());
	    }
	}
	catch( org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode itc )
	{
	    itc.printStackTrace();
	}
	catch( org.omg.CORBA.TypeCodePackage.BadKind bk )
	{
	    bk.printStackTrace();
	}
	catch( org.omg.CORBA.TypeCodePackage.Bounds b )
	{
	    b.printStackTrace();
	}   	
    }


    public void from_any(org.omg.CORBA.Any value) 
	throws InvalidValue, TypeMismatch
    {
	if( !value.type().equal( type() ))
	    throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();

	type = value.type();

	try
	{
	    limit = type().member_count();
	    members = new NameValuePair[limit];
	    org.omg.CORBA.portable.InputStream is = 
                value.create_input_stream();
	    for( int i = 0 ; i < limit; i++ )
	    {
		try
		{
		    Any a = (org.jacorb.orb.Any)orb.create_any();
		    a.read_value(is, type().member_type(i));		   
		    members[i]= new NameValuePair( type().member_name(i), a);
		}
		catch( org.omg.CORBA.TypeCodePackage.Bounds b )
		{
		    b.printStackTrace();
		}   	
	    }	
	}
	catch( org.omg.CORBA.TypeCodePackage.BadKind bk )
	{
	    // should not happen anymore
	    bk.printStackTrace();
	}
    }

    public org.omg.CORBA.Any to_any() 
    {
	org.jacorb.orb.Any out_any = 
            (org.jacorb.orb.Any)orb.create_any();
	out_any.type( type());

	CDROutputStream os = new CDROutputStream();

	for( int i = 0; i < members.length; i++)
	{
	    os.write_value( members[i].value.type(), 
			    (CDRInputStream)members[i].value.create_input_stream());
	}

	CDRInputStream is = new CDRInputStream(orb, os.getBufferCopy());
	out_any.read_value( is, type());
	return out_any;
    }

    /* DynStruct specials */

    public java.lang.String current_member_name()
    {
	return members[pos].id;	
    }

    public org.omg.CORBA.TCKind current_member_kind()
    {
	return members[pos].value.type().kind();
    }

    public NameValuePair[] get_members()
    {
	return members;
    }

    public void set_members(NameValuePair[] nvp)
	throws InvalidValue, TypeMismatch
    {
	if( nvp.length != limit )
	    throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
 
	for( int i = 0; i < limit; i++ )
	{
	    if(! nvp[i].value.type().equal(members[i].value.type() ))
	    {
		System.err.println("expected tc kind " + 
                                   members[i].value.type().kind().value()
				   + ", got " + 
                                   nvp[i].value.type().kind().value() );
		throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
	    }

	    if(! (nvp[i].id.equals("") || nvp[i].id.equals( members[i].id )))
		throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
	}
	members = nvp;	
    }

    public org.omg.DynamicAny.NameDynAnyPair[] get_members_as_dyn_any()
    {
	NameDynAnyPair[] result = new NameDynAnyPair[limit];
	try
	{
	    for( int i = 0; i < limit; i++ )
	    {
		result[i] = new NameDynAnyPair( members[i].id, 
					   dynFactory.create_dyn_any( members[i].value )); 
	    }	
	}
	catch( org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode itc )
	{
	    itc.printStackTrace();
	}
	return result ;
    }

    public void set_members_as_dyn_any(org.omg.DynamicAny.NameDynAnyPair[] nvp) 
	throws TypeMismatch, InvalidValue
    {
	if( nvp.length != limit )
	    throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();

	for( int i = 0; i < limit; i++ )
	{
	    if(! nvp[i].value.type().equal(members[i].value.type() ))
		throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
	    
	    if(! (nvp[i].id.equals("") || nvp[i].id.equals( members[i].id )))
		throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
	    
	}
	members = new NameValuePair[nvp.length];
	for( int i = 0; i < limit; i++ )
	{
	    members[i] = new NameValuePair(nvp[i].id, nvp[i].value.to_any());
	}	
    }

    public void destroy()
    {
	super.destroy();
	members = null;
    }

    /* iteration interface */

    public org.omg.DynamicAny.DynAny current_component()
    {	
	if( pos == -1 )
	    return null;
	try
	{
	    return dynFactory.create_dyn_any( members[pos].value );
	}
	catch( org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode itc )
	{
	    itc.printStackTrace();
	}
	return null;
    }
   


}








