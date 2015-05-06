/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import maaj.coll.traits.VecLike;
import maaj.util.VecH;

/**
 *
 * @author maartyl
 */
public interface Vec extends Collection<Vec>, VecBase<Vec>, VecLike<Vec, VecT> {

  @Override
  public default Vec fromTransient(VecT v) {
    return v.asPersistent();
  }

  @Override
  public default Vec fromPersistent(Vec v) {
    return v;
  }

  @Override
  public default Vec asPersistent() {
    return this;
  }

  @Override
  public default Vec retM(Term contents) {
    throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
  }

  @Override
  public default Vec bindM(Invocable fn2Monad) {
    throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
  }

  @Override
  public default Vec fmap(Invocable mapper) {
    throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
  }

  @Override
  default public Seq seq() {
    return VecH.asSeq(this);
  }

}
