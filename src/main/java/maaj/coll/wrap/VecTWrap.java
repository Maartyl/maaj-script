/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.coll.wrap;

import com.github.krukow.clj_lang.IPersistentVector;
import com.github.krukow.clj_lang.ITransientVector;
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
public final class VecTWrap implements VecT {

  private final ITransientVector<Term> vector;

  private VecTWrap(ITransientVector<Term> vector) {
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
  @SuppressWarnings("unchecked")
  public VecT doConj(Term t) {
    return wrap((ITransientVector<Term>) vector.conj(t));
  }

  @Override
  public VecT doAssocN(int pos, Term value) {
    return wrap(vector.assocN(pos, value));
  }

  @Override
  public Term nth(int i, Term dflt) {
    return H.wrap(vector.nth(i, dflt));
  }

  @Override
  @SuppressWarnings("unchecked")
  public Vec asPersistent() {
    return VecPWrap.of((IPersistentVector<Term>) vector.persistent());
  }

  @Override
  public boolean equals(Object obj) {
    throw new UnsupportedOperationException("TODO"); //DODO: equals
  }

  @Override
  public int hashCode() {
    throw new UnsupportedOperationException("TODO"); //DODO: hash
  }

  private static VecTWrap wrap(ITransientVector<Term> pvector) {
    return new VecTWrap(pvector);
  }

  @SuppressWarnings("unchecked") //precondition: pvector != null
  public static VecTWrap of(ITransientVector<Term> pvector) {
    //null checked in H.wrap: properly returns Nil on null
    return wrap(pvector);
  }

  @SuppressWarnings({"unchecked", "unchecked"})
  public static VecT ofNil(ITransientVector<Term> pvector) {
    //variant that can handle null
    if (pvector == null)//TODO: null-> empty vector - any other option?
      return VecH.emptyTransient();
    return of(pvector);
  }


}
