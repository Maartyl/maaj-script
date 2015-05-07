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
 * @param <VLSelf>      self
 * @param <VLTransient>
 */
//public interface VecLike<VLSelf extends VecLike<VLSelf, VLTransient>, VLTransient extends VecTLike<? extends VLSelf, ? extends VLTransient>>
public interface VecLike<VLSelf extends VecLike<VLSelf, VLTransient>, VLTransient>
        extends VecLikeBase<VLSelf>, Growable<VLSelf>, AssocUpdate<VLSelf>, Persistent<VLSelf, VLTransient>, Sequable, Reducible {

  VLSelf assocN(int pos, Term value);

  default VLSelf assocN(Int pos, Term value) {
    return assocN((pos).asInteger(), value);
  }

  @Override
  public default VLSelf assoc(Term key, Term value) {
    return assocN(H.requireInt(key), value);
  }

}
