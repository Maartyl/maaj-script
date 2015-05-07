/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import maaj.coll.traits.MapTLike;

/**
 *
 * @author maartyl
 */
public interface MapT extends CollectionT<MapT>, MapBase<MapT>, MapTLike<MapT, Map> {

  @Override
  public default MapT asTransient() {
    return this;
  }

}
