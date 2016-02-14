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
   * calls JVM method
   *
   * @param callOn     what class to look for the method on
   * @param thisPtr    ==null <=> static
   * @param methodName
   * @param args
   * @return wrapped return value; NIL for void methods
   */
  Term call(Class callOn, Object thisPtr, String methodName, Seq args);

  /**
   *
   * @param what class to create instance of
   * @param args constructor arguments
   * @return new instance of class `what`
   */
  Term ctor(Class what, Seq args);

}
