/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import maaj.coll.traits.GrowableT;
import maaj.term.visitor.Visitor;

/**
 * transient Collection of Terms.
 * <p>
 * @author maartyl
 * @param <C> self : final type of collection
 */
public interface CollectionT<C extends CollectionT<C>> extends CollectionBase<C>, GrowableT<C> {

  @Override
  public default Term visit(Visitor v) {
    return v.collT(this);
  }

}
