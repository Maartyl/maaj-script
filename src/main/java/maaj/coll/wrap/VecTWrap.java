/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.coll.wrap;

import com.github.krukow.clj_lang.IEditableCollection;
import com.github.krukow.clj_lang.IPersistentVector;
import com.github.krukow.clj_lang.ITransientVector;
import maaj.term.Int;
import maaj.term.Term;
import maaj.term.Vec;
import maaj.term.VecT;
import maaj.util.H;
import maaj.util.VecH;

/**
* wraps functionality of ~Clojure ITransientVector&lt;Term&gt;, making it a term.
 * <p>
 * @author maartyl
 */
public final class VecTWrap implements VecT {

  private ITransientVector<Term> vector;

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
    //transient vector always returns itself, but... (map doesn't / impl. could potentailly change...)
    vector = (ITransientVector<Term>) vector.conj(t);
    return this;
  }

  @Override
  public VecT doAssocN(int pos, Term value) {
    vector = vector.assocN(pos, value);
    return this;
  }

  @Override
  public VecT doPop() {
    vector = vector.pop();
    return this;
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
  public String toString() {
    return "#<transient vec; size: " + getCountAsInteger() + ">";
  }

//  @Override
//  public boolean equals(Object obj) {
//    return this == obj;
//  }
//
//  @Override
//  public int hashCode() {
//    return -1;
//  }

  @SuppressWarnings("unchecked") //precondition: pvector != null
  static VecTWrap of(IPersistentVector<Term> pvector) {
    return new VecTWrap((ITransientVector<Term>) ((IEditableCollection) pvector).asTransient());
  }

  @SuppressWarnings({"unchecked", "unchecked"})
  public static VecT ofNil(IPersistentVector<Term> pvector) {
    //variant that can handle null
    if (pvector == null)//TODO: null-> empty vector - any other option?
      return VecH.emptyTransient();
    return of(pvector);
  }


}
