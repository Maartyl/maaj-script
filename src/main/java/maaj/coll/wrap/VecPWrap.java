/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.coll.wrap;

import com.github.krukow.clj_lang.IEditableCollection;
import com.github.krukow.clj_lang.IPersistentVector;
import com.github.krukow.clj_lang.ITransientVector;
import com.github.krukow.clj_lang.LazilyPersistentVector;
import maaj.term.Int;
import maaj.term.Term;
import maaj.term.Vec;
import maaj.term.VecT;
import maaj.util.H;
import maaj.util.VecH;

/**
 *
 * @author maartyl
 */
public final class VecPWrap implements Vec {

  private final IPersistentVector<Term> vector;

  public VecPWrap(IPersistentVector<Term> vector) {
    this.vector = vector;
  }

  @Override
  public Int getCount() {
    return Int.of(vector.count());
  }

  @Override
  public int getCountAsInteger() {
    return vector.count();
  }

  @Override
  public Vec conj(Term t) {
    return wrap(vector.cons(t));
  }

  @Override
  public Vec assocN(int pos, Term value) {
    return wrap(vector.assocN(pos, value));
  }

  @Override
  public Term nth(int i, Term dflt) {
    return H.wrap(vector.nth(i, dflt));
  }

  @Override
  @SuppressWarnings({"unchecked", "unchecked"})
  public VecT asTransient() {
    return VecTWrap.of((ITransientVector<Term>) ((IEditableCollection) vector).asTransient());
  }

  private static VecPWrap wrap(IPersistentVector<Term> pvector) {
    return new VecPWrap(pvector);
  }

  @SuppressWarnings("unchecked") //precondition: pvector != null
  public static VecPWrap of(IPersistentVector<Term> pvector) {
    //null checked in H.wrap: properly returns Nil on null
    return wrap(pvector);
  }

  @SuppressWarnings("unchecked")
  public static Vec ofNil(IPersistentVector<Term> pvector) {
    //variant that can handle null
    if (pvector == null)//null-> empty vector - any other option?
      return VecH.emptyPersistent();
    return new VecPWrap(pvector);
  }
}
