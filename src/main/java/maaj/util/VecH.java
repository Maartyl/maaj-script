/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.util;

import maaj.coll.Tuple0;
import maaj.coll.traits.VecLike;
import maaj.term.Invocable2;
import maaj.term.Seq;
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
    //TODO: compose hash for tuples: has to work same as clj vecs
    throw new UnsupportedOperationException("not yet");
  }

  public static Vec emptyPersistent() {
    return Tuple0.EMPTY_VEC;
  }
  public static VecT emptyTransient() {
    return emptyPersistent().asTransient();
  }
}
