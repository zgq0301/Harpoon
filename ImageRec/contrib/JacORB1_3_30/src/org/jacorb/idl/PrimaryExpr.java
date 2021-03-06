package org.jacorb.idl;

/*
 *        JacORB - a free Java ORB
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

import java.io.PrintWriter;

/**
 * @author Gerald Brose
 * @version $Id: PrimaryExpr.java,v 1.1 2003-04-03 16:50:23 wbeebee Exp $
 */

class PrimaryExpr 
    extends IdlSymbol
{
    public IdlSymbol symbol;

    private boolean contained = false;
    private ConstDecl declared_in;

    public PrimaryExpr(int num)
    {
        super(num);
    }

    public void print(PrintWriter ps)
    {
        if( symbol instanceof ConstExpr)
        {
            ps.print("(");
            symbol.print(ps);
            ps.print(")");
        } 
        else if( symbol instanceof ScopedName)
        {
            ps.print(((ScopedName)symbol).resolvedName() );
            //            ps.print( ConstDecl.namedValue( (ScopedName)symbol));
        } 
        else // Literal
            symbol.print(ps);
    }

    public void parse()          
    {
        symbol.parse();
    }

    public void setDeclaration( ConstDecl declared_in )
    {        
        this.declared_in = declared_in;
        if( symbol instanceof Literal )
            ((Literal)symbol).setDeclaration( declared_in );
    }

    public void setPackage( String s)
    {
        s = parser.pack_replace(s);
        if( pack_name.length() > 0 )
            pack_name = new String( s + "." + pack_name );
        else
            pack_name = s;

        symbol.setPackage( s);
    }

    int pos_int_const()
    {
        if( symbol instanceof ConstExpr)
        {
            return ((ConstExpr)symbol).pos_int_const();
        } 
        else if( symbol instanceof ScopedName)
        {       
            return Integer.parseInt( ConstDecl.namedValue( (ScopedName)symbol));
        } 
        else 
            return Integer.parseInt( ((Literal)symbol).string );
    }

    public String value()
    {
        if( symbol instanceof ConstExpr)
        {
            return "(" + ((ConstExpr)symbol).value()+")";
        } 
        else if( symbol instanceof ScopedName)
        {
            return ConstDecl.namedValue( (ScopedName)symbol);
        } 
        else 
            return ((Literal)symbol).string;
    }

    public String toString()
    {
        if( symbol instanceof ConstExpr)
        {
            return "(" + ((ConstExpr)symbol).toString()+")";
        } 
        else if( symbol instanceof ScopedName)
        {
            //            return ConstDecl.namedValue( (ScopedName)symbol);
            return ((ScopedName)symbol).resolvedName() + 
                ( declared_in.contained() ? "" : ".value" );
        } 
        else 
            return ((Literal)symbol).string;
    }


}







