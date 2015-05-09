/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.util;

import com.github.krukow.clj_lang.IPersistentVector;
import com.github.krukow.clj_lang.PersistentVector;
import maaj.coll.Tuple0;
import maaj.coll.Tuple1;
import maaj.coll.Tuple2;
import maaj.coll.Tuple3;
import maaj.coll.traits.VecLike;
import maaj.coll.wrap.VecTWrap;
import maaj.term.Invocable2;
import maaj.term.Seq;
import maaj.term.Term;
import maaj.term.Vec;
import maaj.term.VecT;

/**
 *
 * @author maartyl
 */
public class VecH {

  private VecH() {
  }

  public static Seq asSeq(VecLike<?, ?> v) {
    return H.indexed2Seq(v); //VecLike makes it safe : immutable
  }

  public static Vec fromSeq(Seq s) {
    return ((VecT) s.reduce(emptyTransient(), (Invocable2) (v, x) -> ((VecT) v).doConj(x))).asPersistent();
  }

  public static int computeHash(Vec v) {
    //TODO: compose hash for tuples: has to work the same as clj vecs
    throw new UnsupportedOperationException("not yet");
  }

  public static Vec preferTuple(Vec v) {
    switch (v.getCountAsInteger()) {
    case 0: return H.tuple();
    case 1: return v instanceof Tuple1 ? v : H.tuple(v.nth(0));
    case 2: return v instanceof Tuple2 ? v : H.tuple(v.nth(0), v.nth(1));
    case 3: return v instanceof Tuple3 ? v : H.tuple(v.nth(0), v.nth(1), v.nth(2));
    default: return v;
    }
  }

  public static Vec emptyPersistent() {
    return Tuple0.EMPTY_VEC;
  }
  public static VecT emptyTransient() {
    //return emptyPersistent().asTransient(); //doesn't work: requires this
    return VecTWrap.ofNil(emptyPersistentClj());
  }

  @SuppressWarnings("unchecked")
  static IPersistentVector<Term> emptyPersistentClj() {
    return PersistentVector.EMPTY;
  }
}
