/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-200  Gerald Brose.
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

package org.jacorb.idl;

/**
 * @author Gerald Brose
 * @version $Id: LongType.java,v 1.1 2003-04-03 16:50:22 wbeebee Exp $
 */

class LongType 
    extends IntType 
{
    public LongType(int num)
    {
	super(num);
    }

    public Object clone()
    {
	return new LongType( new_num());
    }

    public TypeSpec typeSpec()
    {
	return this;
    }

    public String typeName()
    {
	return "int";
    }

    public boolean basic()
    {
	return true;
    } 

    public int getTCKind()
    {
        if( unsigned )
            return 5; // tk_ulong
        else
            return 3;
    }

    public String toString()
    {
	return typeName();
    }

    public String signature()
    {
	return "I";
    }

    public String holderName()
    {
	return "org.omg.CORBA.IntHolder";
    }


    public String printReadExpression(String ps)
    {
        if( unsigned )
            return ps + ".read_ulong()";
        else
            return ps + ".read_long()";
    }


    public String printWriteStatement(String var_name, String ps)
    {
        if( unsigned )
            return ps + ".write_ulong(" + var_name+ ");";
        else
            return ps + ".write_long(" + var_name+ ");";
    }

    public String printInsertExpression()
    {
        if( unsigned )
            return "insert_ulong";
        else
            return "insert_long";
    }

    public String printExtractExpression()
    {
        if( unsigned )
            return "extract_ulong";
        else
            return "extract_long";
   }

}





















