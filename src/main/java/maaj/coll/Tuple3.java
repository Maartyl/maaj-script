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
public class Tuple3 implements Vec {
  private final Term t0;
  private final Term t1;
  private final Term t2;

  public Tuple3(Term t0, Term t1, Term t2) {
    this.t0 = H.wrap(t0); //assure non-null
    this.t1 = H.wrap(t1);
    this.t2 = H.wrap(t2);
  }

  @Override
  public Vec assocN(int pos, Term value) {
    switch (pos) {
    case 0: return H.tuple(value, t1, t2);
    case 1: return H.tuple(t0, value, t2);
    case 2: return H.tuple(t0, t1, value);
    case TUPLE_SIZE: return conj(value);
    default:
      throw new IndexOutOfBoundsExceptionInfo(getCountAsInteger(), pos);
    }
  }

  @Override
  public Term nth(int i, Term dflt) {
    switch (i) {
    case 0: return t0;
    case 1: return t1;
    case 2: return t2;
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
    return asTransient().doConj(t).asPersistent();
  }

  @Override
  public Vec pop() {
    return H.tuple(t0, t1);
  }

  @Override
  public Seq seq() {
    return H.list(t0, t1, t2);
  }

  @Override
  public Vec fmap(Invocable mapper) {
    return H.tuple(t0.transform(mapper), t1.transform(mapper), t2.transform(mapper));
  }

  @Override
  public VecT asTransient() {
    return VecH.emptyTransient().doConj(t0).doConj(t1).doConj(t2);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (obj instanceof Term)
      obj = ((Term) obj).unwrap();
    else return false;
    if ((obj instanceof VecLike)) {
      VecLike other = (VecLike) obj;
      return getCountAsInteger() == other.getCountAsInteger()
             && t0.equals(other.nth(0))
             && t1.equals(other.nth(1))
             && t2.equals(other.nth(2));
    }

    return false;
  }

  @Override
  public int hashCode() {
    return VecH.computeHash(this);
  }

  @Override
  public String toString() {
    return print();
  }

  private static final int TUPLE_SIZE = 3;
}
