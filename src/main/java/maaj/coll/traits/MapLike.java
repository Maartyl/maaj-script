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
 * @param <MT> transient version of self
 */
public interface MapLike<M extends MapLike<M, MT>, MT> extends
        Growable<M>, AssocUpdate<M>, Dissoc<M>, Persistent<M, MT>, Seqable, Reducible {
  @Override
  public default M conj(Term term) {
    return MapH.coerceConj(term, this::assoc);
  }
}
