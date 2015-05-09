/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.coll;

import maaj.coll.traits.VecLike;
import maaj.exceptions.IndexOutOfBoundsExceptionInfo;
import maaj.util.H;
import maaj.term.Int;
import maaj.term.Invocable;
import maaj.term.Seq;
import maaj.term.Term;
import maaj.term.Vec;
import maaj.term.VecT;
import maaj.util.VecH;

/**
 *
 * @author maartyl
 */
public class Tuple1 implements Vec {
  private final Term t0;

  public Tuple1(Term t0) {
    this.t0 = H.wrap(t0); //assure non-null
  }

  @Override
  public Vec assocN(int pos, Term value) {
    switch (pos) {
    case 0: return H.tuple(value);
    case TUPLE_SIZE: return conj(value);
    default:
      throw new IndexOutOfBoundsExceptionInfo(getCountAsInteger(), pos);
    }
  }

  @Override
  public Term nth(int i, Term dflt) {
    switch (i) {
    case 0: return t0;
    default:
      return dflt;
    }
  }

  @Override
  public Int getCount() {
    return Int.of(TUPLE_SIZE);
  }

  @Override
  public int getCountAsInteger() {
    return TUPLE_SIZE;
  }

  @Override
  public Vec conj(Term t) {
    return H.tuple(t0, t);
  }

  @Override
  public Vec pop() {
    return H.tuple();
  }

  @Override
  public Seq seq() {
    return H.list(t0);
  }

  @Override
  public Term reduce(Term start, Invocable reducer) {
    return reducer.invoke(start, t0);
  }

  @Override
  public VecT asTransient() {
    return VecH.emptyTransient().doConj(t0);
  }

  @Override
  public String toString() {
    return "[" + t0.toString() +  "]";
  }

  @Override
  public boolean equals(Object obj) {
    //TODO: if wrapper? (meta)
    if ((obj instanceof Vec)) {
      VecLike other = (VecLike) obj;
      return getCountAsInteger() == other.getCountAsInteger() && t0.equals(other.nth(0));
    }

    return false;
  }

  @Override
  public int hashCode() {
    return VecH.computeHash(this);
  }

  private static final int TUPLE_SIZE = 1;
}
