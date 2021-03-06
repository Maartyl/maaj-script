/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.coll;

import maaj.coll.traits.Counted;
import maaj.coll.traits.Growable;
import maaj.coll.traits.Reducible;
import maaj.coll.traits.Seqable;
import maaj.lang.Context;
import maaj.util.H;
import maaj.exceptions.InvalidOperationException;
import maaj.util.SeqH;
import maaj.term.*;

/**
 *
 * @author maartyl
 */
public class Sexp extends Cons implements Counted, Growable<Seq>, Seqable, Reducible {
  private final Int size;

  public Sexp(Int size, Term head, Seq tail) {
    super(head, tail);
    this.size = size;
  }

  public Sexp(int size, Term head, Seq tail) {
    this(Int.of(size), head, tail);
  }

  public Sexp(Term head, Seq tail) {
    this(tail.count().inc(), head, tail);
  }


  @Override
  public Int count() {
    return size;
  }

  @Override
  public boolean isCounted() {
    return true;
  }

  @Override
  public Sexp retM(Term contents) {
    return Sexp.retM1(contents);
  }

  @Override
  public Seq bindM(Invocable fn2Monad) {
//    if (size.asLong() < 5)
//      return SeqH.concatSexp(this.fmap(fn2Monad));
    
    return super.bindM(fn2Monad);
  }

  @Override
  public Seq fmap(Invocable mapper) {
//    if (size.asLong() < 10) //these numbers are just guessed: could be too much, too little... (more likely too much)
//      return SeqH.mapSexp(this, mapper);

    return super.fmap(mapper);
  }

  @Override
  public Term apply(Context cxt, Seq args) {
    throw new InvalidOperationException("List cannot be used as function."); //TODO: implement
  }

  @Override
  public Int getCount() {
    return size;
  }

  @Override
  public Seq seq() {
    return this;
  }

  @Override
  public Sexp conj(Term t) {
    return SeqH.sexp(t, this);
  }

  @Override //required: diamond inheritance (unrelated defaults)
  public Term reduce(Term start, Invocable reducer) {
    return super.reduce(H.ret1(start, start = null), H.ret1(reducer, reducer = null));
  }

  @Override //required: diamond inheritance (unrelated defaults)
  public Term eval(Context c) {
    return super.eval(H.ret1(c, c = null));
  }

  @Override //required: diamond inheritance (unrelated defaults)
  public Term evalMacros(Context c) {
    return super.evalMacros(H.ret1(c, c = null));
  }



  //--- STATIC
  public static Sexp retM1(Term t) {
    return new Sexp(1, t, H.END);
  }
}
