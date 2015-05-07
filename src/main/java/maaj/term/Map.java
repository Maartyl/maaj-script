/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import maaj.coll.traits.MapLike;

/**
 *
 * @author maartyl
 */
public interface Map extends Collection<Map>, MapBase<Map>, MapLike<Map, MapT> {

  @Override
  public default Map asPersistent() {
    return this;
  }

}
