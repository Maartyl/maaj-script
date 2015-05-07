/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;


import maaj.coll.traits.VecTLike;

/**
 *
 * @author maartyl
 */
public interface VecT extends CollectionT<VecT>, VecBase<VecT>, VecTLike<VecT, Vec> {

  @Override
  public default VecT fromTransient(VecT v) {
    return v;
  }

  @Override
  public default VecT fromPersistent(Vec v) {
    return v.asTransient();
  }

  @Override
  public default VecT asTransient() {
    return this;
  }

  @Override
  default public VecT fmap(Invocable mapper) {
    //TODO: maybe make it mutate vect?
    throw new UnsupportedOperationException("not yet");
  }

}
