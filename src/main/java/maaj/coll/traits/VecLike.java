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
 * @param <V>  self
 * @param <VT>
 */
//public interface VecLike<VLSelf extends VecLike<VLSelf, VLTransient>, VLTransient extends VecTLike<? extends VLSelf, ? extends VLTransient>>
public interface VecLike<V extends VecLike<V, VT>, VT>
        extends VecLikeBase<V>, Growable<V>, AssocUpdate<V>, Persistent<V, VT>, StackLike<V>, Seqable, Reducible {

  V assocN(int pos, Term value);

  default V assocN(Int pos, Term value) {
    return assocN((pos).asInteger(), value);
  }

  @Override
  public default V assoc(Term key, Term value) {
    return assocN(H.requireInt(key), value);
  }

}
