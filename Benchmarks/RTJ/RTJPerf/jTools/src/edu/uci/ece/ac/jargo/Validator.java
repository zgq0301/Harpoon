// ************************************************************************
//    $Id: Validator.java,v 1.1 2002-07-02 15:35:26 wbeebee Exp $
// ************************************************************************
//
//                               jTools
//
//               Copyright (C) 2001-2002 by Angelo Corsaro.
//                         <corsaro@ece.uci.edu>
//                          All Rights Reserved.
//
//   Permission to use, copy, modify, and distribute this software and
//   its  documentation for any purpose is hereby  granted without fee,
//   provided that the above copyright notice appear in all copies and
//   that both that copyright notice and this permission notice appear
//   in  supporting  documentation. I don't make  any  representations
//   about the  suitability  of this  software for any  purpose. It is
//   provided "as is" without express or implied warranty.
//
//
// *************************************************************************
//  
// *************************************************************************
package edu.uci.ece.ac.jargo;

public interface Validator {

    /**
     * Validates a string and returns an instance of a type associated
     * with the value.
     *
     * @param value a <code>String</code> representing the value to be
     * validated
     * @return an <code>Object</code> representing the value as a Java
     * Object i.e. if the value field was an integer than the return
     * value would be and <code>Integer</code> instance
     */
    Object validate(String value)  throws InvalidArgumentValueException;
}
