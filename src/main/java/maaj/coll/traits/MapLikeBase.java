/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.coll.traits;

import maaj.term.KVPair;
import maaj.term.Term;
import maaj.util.H;

/**
 *
 * @author maartyl
 * @param <M> self
 */
public interface MapLikeBase<M extends MapLikeBase<M>> extends AssocGet, Counted, Iterable<KVPair> {
  @Override
  public default Term valAt(Term key) {
    return valAt(key, H.NIL);
  }

}
