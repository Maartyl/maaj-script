/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import maaj.coll.traits.Growable;
import maaj.coll.traits.Reducible;
import maaj.coll.traits.Seqable;
import maaj.term.visitor.Visitor;

/**
 * Collection of Terms.
 * <p>
 * @author maartyl
 * @param <C> self : final type of collection
 */
public interface Collection<C extends Collection<C>> extends CollectionBase<C>, Growable<C>, Seqable, Reducible {

  @Override
  public default Term reduce(Term start, Invocable reducer) {
    return seq().reduce(reducer);
  }

  @Override
  public default <TR, TA> TR visit(Visitor<TR, TA> v, TA arg) {
    return v.coll(this, arg);
  }

}
