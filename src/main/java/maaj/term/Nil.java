/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import java.io.IOException;
import java.io.Writer;
import maaj.exceptions.InvalidOperationException;
import maaj.lang.Context;
import maaj.term.visitor.Visitor;
import maaj.util.H;

/**
 * Singleton representing null; null is not allowed in MaajScript and is always wrapped* in a Nil.
 * (* replaced with)
 * Empty seq. Used as seq terminator: normal seq segment is [head and rest], last rest must be something special; empty.
 * <p>
 * @author maartyl
 */
public interface Nil extends Seq, Ground {

  @Override
  default boolean isCounted() {
    return true;
  }

  @Override
  default Class getType() {
    return Nil.class;
  }

  @Override
  default Object getContent() {
    return null;
  }

  @Override
  default boolean isNil() {
    return true;
  }

  @Override
  public default Term first() {
    throw new InvalidOperationException("Head of NilSeq."); //TODO: implement
  }

  @Override
  public default Seq rest() {
    throw new InvalidOperationException("Rest of NilSeq."); //TODO: implement
  }

  @Override
  public default Term firstOrNil() {
    return H.NIL;
  }

  @Override
  public default Seq restOrNil() {
    return H.END;
  }

  @Override
  public default Int count() {
    return Int.of(0);
  }


  @Override
  public default Seq bindM(Invocable fn2Monad) {
    return this;
  }

  @Override
  public default Seq fmap(Invocable mapper) {
    return this;
  }

  @Override
  public default void show(Writer w) throws IOException {
    w.append("()");
  }

  @Override
  public default Term eval(Context c) {
    return this;
  }

  @Override
  public default Term evalMacros(Context c) {
    return this;
  }

  @Override
  default Term apply(Context cxt, Seq args) {
    throw new UnsupportedOperationException("Nil cannot be used as function.");
  }

  @Override
  public default Monad unquoteTraverse(Context c) {
    return H.tuple(this);
  }

  @Override
  public default <TR, TA> TR visit(Visitor<TR, TA> v, TA arg) {
    return v.nil(this, arg);
  }


  public static final Nil END = new Nil() {

    @Override
    public String toString() {
      return "()";
    }

    @Override
    public boolean equals(Object obj) {
      return obj instanceof Term && ((Term) obj).isNil();
    }

    @Override
    public int hashCode() {
      return 0;
    }

  };
}
