/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import maaj.coll.traits.Counted;
import maaj.coll.traits.Reducible;
import maaj.coll.traits.Sequable;

/**
 * Base of Collection of Terms that does not provide modifying.
 * <p>
 * @author maartyl
 * @param <C> self : final type of collection
 */
public interface CollectionBase<C extends CollectionBase<C>> extends Monad<C>, Counted, Sequable, Reducible {

  @Override
  public default Term reduce(Term start, Invocable reducer) {
    return seq().reduce(reducer);
  }

}
