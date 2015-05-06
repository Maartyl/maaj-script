/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.util;

import com.github.krukow.clj_lang.IEditableCollection;
import com.github.krukow.clj_lang.IPersistentVector;
import com.github.krukow.clj_lang.ITransientVector;
import com.github.krukow.clj_lang.PersistentVector;
import maaj.coll.Tuple0;
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

  public static Seq asSeq(VecT v) {
    return asSeq(v.asPersistent());
  }

  public static Vec fromSeq(Seq s) {
    return ((VecT) s.reduce(emptyTransient(), (Invocable2) (v, x) -> ((VecT) v).doConj(x))).asPersistent();
  }

  public static int computeHash(Vec v) {
    //TODO: compose hash
    return 5;
  }

  public static Vec emptyPersistent() {
    return Tuple0.EMPTY_VEC;
  }
  public static VecT emptyTransient() {
    return VecTWrap.of(emptyTransientClj());
  }

  @SuppressWarnings({"unchecked"})
  public static IPersistentVector<Term> emptyPersistentClj() {
    return PersistentVector.EMPTY;
  }

  @SuppressWarnings("unchecked")
  public static ITransientVector<Term> emptyTransientClj() {
    return (ITransientVector<Term>) (((IEditableCollection) emptyPersistentClj()).asTransient());
  }

}
