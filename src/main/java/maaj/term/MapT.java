/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import java.io.IOException;
import java.io.Writer;
import maaj.coll.traits.MapTLike;
import maaj.term.visitor.Visitor;

/**
 * transient counterpart of Map
 * <p>
 * @author maartyl
 */
public interface MapT extends CollectionT<MapT>, MapBase<MapT>, MapTLike<MapT, Map> {

  @Override
  public default MapT asTransient() {
    return this;
  }

  @Override
  public default void show(Writer w) throws IOException {
    w.append("#<transient map>");
  }

  @Override
  public default Term visit(Visitor v) {
    return v.mapT(this);
  }

}
