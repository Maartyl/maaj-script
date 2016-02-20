/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.interop;

import maaj.term.Seq;
import maaj.term.Term;

/**
 *
 * @author maartyl
 */
public interface Interop {

  /**
   * Calls native method.
   * <p>
   * @param callOn     what class to look for the method on
   * @param thisPtr    ==null <=> static
   * @param methodName
   * @param args
   * @return wrapped return value; NIL for void methods
   */
  Term call(Class callOn, Object thisPtr, String methodName, Seq args);

  /**
   * Constructs new instance.
   * <p>
   * @param what class to create instance of
   * @param args constructor arguments
   * @return wrapped new instance of class `what`
   */
  Term ctor(Class what, Seq args);

  /**
   * Assigns value to a field.
   * <p>
   * @param onType    what type (same as thisPtr if not static)
   * @param thisPtr   ==null <=> static
   * @param fieldName which field
   * @param value     wrapped value to assign
   */
  void fieldSet(Class onType, Object thisPtr, String fieldName, Term value);

  /**
   * Reads value from a field.
   * <p>
   * @param onType    what type (same as thisPtr if not static)
   * @param thisPtr   ==null <=> static
   * @param fieldName which field
   * @return wrapped value read from the field
   */
  Term fieldGet(Class onType, Object thisPtr, String fieldName);

}
