/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import java.io.IOException;
import java.io.Writer;
import maaj.lang.Context;

/**
 *
 * @author maartyl
 */
public interface Nil extends Ground {

  @Override
  default Term eval(Context c) {
    //TODO: or throw exception? - null in evaluation chain?
    return this;
  }

  @Override
  default Term evalMacros(Context c) {
    return this;
  }

  @Override
  default Term apply(Context cxt, Seq args) {
    throw new UnsupportedOperationException("Nil cannot be used as function.");
  }

  @Override
  default Term invokeMethod(Str methodName, Seq args) {
    throw new NullPointerException("Cannot invoke methods on nil.");
  }

  @Override
  default boolean isNil() {
    return true;
  }

  @Override
  default Object getContent() {
    return null;
  }

  @Override
  public default void show(Writer w) throws IOException {
    w.append("nil");
  }



  /**
   * Singleton representing null; null is not allowed in Maaj.
   * All nils are equal.
   */
  public static final Nil NIL = new Nil() {

    @Override
    public String toString() {
      return "()";
    }

    @Override
    public boolean equals(Object obj) {
      return /*obj instanceof Nil ||*/ (obj instanceof Term && ((Term) obj).isNil());
    }

    @Override
    public int hashCode() {
      return 0;
    }

  };
}
