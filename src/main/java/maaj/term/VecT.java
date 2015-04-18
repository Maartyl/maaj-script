/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;


import maaj.coll.traits.VecTLike;
import maaj.util.VecH;

/**
 *
 * @author maartyl
 */
public interface VecT extends CollectionT<VecT>, VecBase<VecT>, VecTLike<VecT, Vec> {

  @Override
  public default VecT asTransient() {
    return this;
  }

  @Override
  default public VecT fmap(Invocable mapper) {
    //TODO: after transient
    throw new UnsupportedOperationException("not yet");
  }

  @Override
  default public VecT bindM(Invocable fn2Monad) {
    //TODO: after transient
    throw new UnsupportedOperationException("not yet");
  }

  @Override
  default public VecT retM(Term contents) {
    //TODO: after Tuple1
    throw new UnsupportedOperationException("not yet");
  }

  @Override
  default public Seq seq() {
    return VecH.asSeq(this.asPersistent());
  }
}
