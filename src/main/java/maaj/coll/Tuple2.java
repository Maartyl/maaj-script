/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.coll;

import maaj.coll.traits.AssocGet;
import maaj.coll.traits.KVEntry;
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
public class Tuple2 implements Vec, KVEntry {
  private final Term t0;
  private final Term t1;

  public Tuple2(Term t0, Term t1) {
    this.t0 = H.wrap(t0); //assure non-null
    this.t1 = H.wrap(t1);
  }

  @Override
  public Vec assocN(int pos, Term value) {
    switch (pos) {
    case 0: return H.tuple(value, t1);
    case 1: return H.tuple(t0, value);
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
    default:
      throw new IndexOutOfBoundsExceptionInfo(getCountAsInteger(), i);
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
    throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
  }

  @Override
  public Seq seq() {
    return H.list(t0, t1);
  }

  @Override
  public Term reduce(Term start, Invocable reducer) {
    throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
  }

  @Override
  public VecT asTransient() {
    throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
  }


  @Override
  public Term getKey() {
    return t0;
  }

  @Override
  public Term getValue() {
    return t1;
  }

  @Override
  public String toString() {
    return "[" + t0.toString() + " " + t1.toString() + "]";
  }

  @Override
  public boolean equals(Object obj) {
    //TODO: if wrapper? (meta)
    if ((obj instanceof Vec)) {
      VecLike other = (VecLike) obj;
      return getCountAsInteger() == other.getCountAsInteger() && t0.equals(other.nth(0)) && t1.equals(other.nth(1));
    }
    if ((obj instanceof KVEntry)) {
      KVEntry other = (KVEntry) obj;
      return getKey().equals(other.getKey()) && getValue().equals(other.getValue());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return VecH.computeHash(this);
  }

  private static final int TUPLE_SIZE = 2;
}
