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
public class Tuple0 implements Vec {

  public Tuple0() {

  }

  @Override
  public Vec assocN(int pos, Term value) {
    switch (pos) {
    case TUPLE_SIZE: return conj(value);
    default:
      throw new IndexOutOfBoundsExceptionInfo(getCountAsInteger(), pos);
    }
  }

  @Override
  public Term nth(int i, Term dflt) {
    switch (i) {
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
    return H.tuple(t);
  }

  @Override
  public Vec pop() {
    throw new IllegalStateException("Can't pop empty vector.");
  }

  @Override
  public Seq seq() {
    return H.list();
  }

  @Override
  public Term reduce(Term start, Invocable reducer) {
    return start;
  }

  @Override
  public Vec fmap(Invocable mapper) {
    return H.tuple();
  }

  @Override
  public VecT asTransient() {
    return VecH.emptyTransient();
  }

  @Override
  public String toString() {
    return "[]";
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (obj instanceof Term)
      obj = ((Term) obj).unwrap();
    else return false;
    if ((obj instanceof VecLike)) {
      VecLike other = (VecLike) obj;
      return getCountAsInteger() == other.getCountAsInteger();
    }
    
    return false;
  }

  @Override
  public int hashCode() {
    return VecH.computeHash(this);
  }

  private static final int TUPLE_SIZE = 0;

  public static final Vec EMPTY_VEC = new Tuple0();
}
