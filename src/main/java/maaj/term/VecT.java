/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;


import java.io.IOException;
import java.io.Writer;
import maaj.coll.traits.VecTLike;
import maaj.term.visitor.Visitor;

/**
 * transient counterpart of Vec
 * <p>
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

  @Override
  public default void show(Writer w) throws IOException {
    w.append("#<transient vector>");
  }

  @Override
  public default Term visit(Visitor v) {
    return v.vecT(this);
  }

}
