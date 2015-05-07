/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.coll.traits;

import maaj.util.H;
import maaj.term.Int;
import maaj.term.Term;

/**
 *
 * @author maartyl
 * @param <V>
 * @param <VP>
 */
//public interface VecTLike<VLSelf extends VecTLike<VLSelf, VLPersistent>, VLPersistent extends VecLike<? extends VLPersistent, ? extends VLSelf>>
public interface VecTLike<V extends VecTLike<V, VP>, VP>
        extends VecLikeBase<V>, GrowableT<V>, AssocUpdateT<V>, Transient<VP, V>, StackTLike<V> {

  V doAssocN(int pos, Term value);

  default V doAssocN(Int pos, Term value) {
    return doAssocN((pos).asInteger(), value);
  }

  @Override
  public default V doAssoc(Term key, Term value) {
    return doAssocN(H.requireInt(key), value);
  }

}
