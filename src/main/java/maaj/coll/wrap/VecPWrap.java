/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.coll.wrap;

import com.github.krukow.clj_lang.IPersistentVector;
import maaj.coll.traits.VecLike;
import maaj.term.Int;
import maaj.term.Term;
import maaj.term.Vec;
import maaj.term.VecT;
import maaj.util.H;
import maaj.util.VecH;

/**
 * wraps functionality of ~Clojure IPersistentVector&lt;Term&gt;, making it a term.
 * - for small vectors, I use maaj.coll.Tuple0-3
 * <p>
 * @author maartyl
 */
public final class VecPWrap implements Vec {

  private final IPersistentVector<Term> vector;

  private VecPWrap(IPersistentVector<Term> vector) {
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
  public Vec pop() {
    return wrap((IPersistentVector<Term>) vector.pop());
  }

  @Override
  public Term nth(int i, Term dflt) {
    return H.wrap(vector.nth(i, dflt));
  }

  @Override
  @SuppressWarnings({"unchecked", "unchecked"})
  public VecT asTransient() {
    return VecTWrap.of(vector);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (obj instanceof Term)
      obj = ((Term) obj).getContent();
    if (obj instanceof VecPWrap)
      return vector.equiv(((VecPWrap) obj).vector);
    if (obj instanceof VecLike) {
      VecLike<?, ?> v = (VecLike) obj;
      int count = getCountAsInteger();
      for (int i = 0; i < count; ++i)
        if (!nth(i).equals(v.nth(i)))
          return false;
      return true;
    }
    return false;
  }

  @Override
  public int hashCode() {
    //CORRECTNESS: !! - I have to make sure, tuples use the same algorithm...
    return vector.hashCode();
  }

  private static VecPWrap wrap(IPersistentVector<Term> pvector) {
    return new VecPWrap(pvector);
  }

  @SuppressWarnings("unchecked")
  static VecPWrap of(IPersistentVector<Term> pvector) {
    return wrap(pvector);
  }

  @SuppressWarnings("unchecked")
  public static Vec ofNil(IPersistentVector<Term> pvector) {
    //variant that can handle null
    if (pvector == null)//null-> empty - any other option?
      return VecH.emptyPersistent();
    return new VecPWrap(pvector);
  }

}
