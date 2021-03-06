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
 * @param <MP> persistent variant of self
 */
public interface MapTLike<M extends MapTLike<M, MP>, MP> extends GrowableT<M>, AssocUpdateT<M>, DissocT<M>, Transient<MP, M> {

  @Override
  public default M doConj(Term term) {
    return MapH.coerceConj(term, this::doAssoc);
  }

}
