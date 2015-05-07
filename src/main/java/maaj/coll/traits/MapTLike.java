/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.coll.traits;

import maaj.term.Term;
import maaj.util.MapH;

/**
 *
 * @author maartyl
 * @param <M> self
 */
public interface MapTLike<M extends MapTLike<M>> extends GrowableT<M>, AssocUpdateT<M> {

  @Override
  public default M doConj(Term term) {
    return MapH.coerceConj(term, this::doAssoc);
  }

}
