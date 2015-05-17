/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import maaj.coll.traits.VecLike;
import maaj.util.VecH;

/**
 * represents a structure similar to ArrayList\<Term\> but immutable.
 * <p>
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
  default public Seq seq() {
    return VecH.asSeq(this);
  }

  @Override
  default public Term reduce(Term acc, Invocable reducer) {
    int count = getCountAsInteger();
    for (int i = 0; i < count; i++)
      acc = reducer.invoke(acc, nth(i));
    return acc;
  }


}
