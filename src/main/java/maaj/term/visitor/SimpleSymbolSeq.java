/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term.visitor;

import maaj.term.Collection;
import maaj.term.Invocable1;
import maaj.term.Monad;
import maaj.term.Nil;
import maaj.term.Seq;
import maaj.term.Symbol;
import maaj.term.Term;
import maaj.term.Unquote;
import maaj.util.H;

/**
 *
 * @author maartyl
 */
public class SimpleSymbolSeq implements VisitorRecursive<Seq, Nil> {

  private SimpleSymbolSeq() {
  }

  @Override
  public Seq id(Term t, Nil arg) {
    return H.END;
  }

  @Override
  public Seq symbolSimple(Symbol t, Nil arg) {
    return H.list(t);
  }

  @Override
  public Seq seq(Seq t, Nil arg) {
    return t.bindM((Invocable1) x -> x.unwrap().visit(this, arg));
  }

  @Override
  public Seq coll(Collection t, Nil arg) {
    return seq(t.seq(), arg);
  }

  @Override
  public Seq monad(Monad t, Nil arg) {
    return id(t, arg);
  }

  @Override
  public Seq unquote(Unquote t, Nil arg) {
    return t.getBody().unwrap().visit(this, arg);
  }

  private static final SimpleSymbolSeq SINGLETON = new SimpleSymbolSeq();

  public static SimpleSymbolSeq singleton() {
    return SINGLETON;
  }

}
