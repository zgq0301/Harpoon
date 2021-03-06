
// Copyright (C) 1998-1999
// Object Oriented Concepts, Inc.

// **********************************************************************
//
// Copyright (c) 1997
// Mark Spruiell (mark@intellisoft.com)
//
// See the COPYING file for more information
//
// **********************************************************************

package org.jacorb.trading.constraint;

import java.io.*;


/** Represents arithmetic multiplication */
public class MultiplyNode extends ExprNode
{
  private ExprNode m_left;
  private ExprNode m_right;


  private MultiplyNode()
  {
  }


  public MultiplyNode(ExprNode left, ExprNode right)
  {
    m_left = left;
    m_right = right;

      // the type of this expression is the "promoted" type of our
      // children
    int id = ValueType.promote(left.getType().getId(), right.getType().getId());
    setType(new ValueType(id));
  }


  public void print(PrintStream ps)
  {
    ps.println("MultiplyNode: type = " + getType());
    ps.println("Left node:");
    m_left.print(ps);
    ps.println("Right node:");
    m_right.print(ps);
  }


  public Value evaluate(PropertySource source)
    throws MissingPropertyException
  {
    Value result = null;

    int id = getType().getId();

    Value v, left, right;
    v = m_left.evaluate(source);
    left = v.convert(id);
    v = m_right.evaluate(source);
    right = v.convert(id);

    result = left.multiply(right);

    return result;
  }
}










